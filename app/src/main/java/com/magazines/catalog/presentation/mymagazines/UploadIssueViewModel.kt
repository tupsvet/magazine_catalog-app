package com.magazines.catalog.presentation.mymagazines

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.magazines.catalog.data.remote.ApiResult
import com.magazines.catalog.data.remote.UriFileReader
import com.magazines.catalog.domain.model.FileData
import com.magazines.catalog.domain.model.UploadIssueRequest
import com.magazines.catalog.domain.usecase.issue.UploadIssueUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UploadIssueUiState(
    val selectedPdfUri: Uri? = null,
    val selectedPdfName: String? = null,
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class UploadIssueViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val uploadIssueUseCase: UploadIssueUseCase,
) : ViewModel() {

    private val magazineId: String = savedStateHandle.get<String>("magazineId").orEmpty()

    private val _uiState = MutableStateFlow(UploadIssueUiState())
    val uiState: StateFlow<UploadIssueUiState> = _uiState.asStateFlow()

    fun pickPdf(uri: Uri) {
        val name = UriFileReader.read(
            context = context,
            uri = uri,
            defaultMimeType = "application/pdf",
            defaultFileName = "issue.pdf",
        )?.fileName ?: "issue.pdf"

        _uiState.update {
            it.copy(selectedPdfUri = uri, selectedPdfName = name, error = null)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun submit(issueNumber: Int, publicationDate: String?) {
        val pdfUri = _uiState.value.selectedPdfUri
        if (pdfUri == null) {
            _uiState.update { it.copy(error = "Выберите PDF-файл") }
            return
        }

        if (magazineId.isBlank()) {
            Log.e("Issues", "uploadIssue: magazineId is blank")
            _uiState.update { it.copy(error = "Не указан идентификатор журнала") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null, isSuccess = false) }

            val pdfFile = try {
                val bytes = context.contentResolver.openInputStream(pdfUri)!!.use { it.readBytes() }
                val mimeType = context.contentResolver.getType(pdfUri) ?: "application/pdf"
                val fileName = "issue.pdf"
                Log.d("Issues", "uploadIssue: magazineId=$magazineId, pdfSize=${bytes.size}")
                FileData(bytes = bytes, mimeType = mimeType, fileName = fileName)
            } catch (e: Exception) {
                Log.e("Issues", "uploadIssue: failed to read pdf", e)
                _uiState.update {
                    it.copy(isSubmitting = false, error = "Не удалось прочитать PDF-файл")
                }
                return@launch
            }

            val request = UploadIssueRequest(
                magazineId = magazineId,
                issueNumber = issueNumber,
                publicationDate = publicationDate?.trim()?.takeIf { it.isNotEmpty() },
            )

            when (val result = uploadIssueUseCase(request, pdfFile)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isSubmitting = false, isSuccess = true) }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(isSubmitting = false, error = result.message)
                    }
                }
                ApiResult.NetworkError -> {
                    _uiState.update {
                        it.copy(isSubmitting = false, error = NETWORK_ERROR)
                    }
                }
            }
        }
    }

    companion object {
        private const val NETWORK_ERROR = "Нет подключения к сети"
    }
}
