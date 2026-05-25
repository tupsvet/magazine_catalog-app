package com.magazines.catalog.presentation.detail

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.magazines.catalog.data.local.prefs.TokenStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import javax.inject.Inject

data class PdfViewerUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isReady: Boolean = true,
)

@HiltViewModel
class PdfViewerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val tokenStorage: TokenStorage,
) : ViewModel() {

    val pdfUrl: String = savedStateHandle.get<String>("pdfUrl").orEmpty()

    val title: String = savedStateHandle.get<String>("title")
        .orEmpty()
        .ifBlank { "Выпуск" }

    private val _uiState = MutableStateFlow(PdfViewerUiState())
    val uiState: StateFlow<PdfViewerUiState> = _uiState.asStateFlow()

    fun downloadAndOpenPdf(context: Context, pdfUrl: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val token = tokenStorage.getToken() ?: run {
                    _uiState.update { it.copy(isLoading = false, error = "Не авторизован") }
                    return@launch
                }
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url(pdfUrl)
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                val response = withContext(Dispatchers.IO) { client.newCall(request).execute() }
                if (!response.isSuccessful) {
                    _uiState.update { it.copy(isLoading = false, error = "Ошибка загрузки PDF") }
                    return@launch
                }
                val bytes = withContext(Dispatchers.IO) { response.body?.bytes() }
                if (bytes == null) {
                    _uiState.update { it.copy(isLoading = false, error = "PDF пустой") }
                    return@launch
                }
                val file = File(context.cacheDir, "issue.pdf")
                withContext(Dispatchers.IO) { file.writeBytes(bytes) }
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file,
                )
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Ошибка: ${e.message}") }
            }
        }
    }
}
