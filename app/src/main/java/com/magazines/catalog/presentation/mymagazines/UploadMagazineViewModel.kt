package com.magazines.catalog.presentation.mymagazines

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.magazines.catalog.data.remote.ApiResult
import com.magazines.catalog.data.remote.ignoreUnauthorized
import com.magazines.catalog.data.remote.UriFileReader
import com.magazines.catalog.domain.model.Category
import com.magazines.catalog.domain.model.CreateMagazineRequest
import com.magazines.catalog.domain.model.FileData
import com.magazines.catalog.domain.model.UploadIssueRequest
import com.magazines.catalog.domain.usecase.category.GetCategoriesUseCase
import com.magazines.catalog.domain.usecase.issue.UploadIssueUseCase
import com.magazines.catalog.domain.usecase.magazine.UploadCoverUseCase
import com.magazines.catalog.domain.usecase.magazine.UploadMagazineUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UploadMagazineUiState(
    val categories: List<Category> = emptyList(),
    val selectedImageUri: Uri? = null,
    val isLoadingCategories: Boolean = false,
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val selectedPdfUri: Uri? = null,
    val selectedPdfName: String? = null,
    val pdfSizeMb: String? = null,
    val isIssueSubmitting: Boolean = false,
)

@HiltViewModel
class UploadMagazineViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val uploadMagazineUseCase: UploadMagazineUseCase,
    private val uploadCoverUseCase: UploadCoverUseCase,
    private val uploadIssueUseCase: UploadIssueUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UploadMagazineUiState())
    val uiState: StateFlow<UploadMagazineUiState> = _uiState.asStateFlow()

    private val _currentStep = MutableStateFlow(1)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    private val _createdMagazineId = MutableStateFlow<String?>(null)
    val createdMagazineId: StateFlow<String?> = _createdMagazineId.asStateFlow()

    init {
        loadCategories()
    }

    fun pickImage(uri: Uri) {
        _uiState.update { it.copy(selectedImageUri = uri, error = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun onMagazineCreated(magazineId: String) {
        _createdMagazineId.value = magazineId
        _currentStep.value = 2
    }

    fun onSkipIssue() {
        _uiState.update { it.copy(isSuccess = true) }
    }

    fun pickPdf(uri: Uri) {
        val resolver = context.contentResolver
        val name = runCatching {
            resolver.query(uri, null, null, null, null)?.use { cursor ->
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && idx >= 0) cursor.getString(idx) else null
            }
        }.getOrNull() ?: "issue.pdf"

        val sizeMb = runCatching {
            resolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst() && !cursor.isNull(0)) cursor.getLong(0) else null
            }
        }.getOrNull()?.let { "%.1f".format(it / 1_048_576.0) }

        _uiState.update {
            it.copy(
                selectedPdfUri = uri,
                selectedPdfName = name,
                pdfSizeMb = sizeMb,
                error = null,
            )
        }
    }

    fun clearPdf() {
        _uiState.update {
            it.copy(selectedPdfUri = null, selectedPdfName = null, pdfSizeMb = null)
        }
    }

    fun submitIssue(issueNumber: Int, publicationDate: String?) {
        val pdfUri = _uiState.value.selectedPdfUri
        if (pdfUri == null) {
            _uiState.update { it.copy(error = "Выберите PDF-файл") }
            return
        }
        val magazineId = _createdMagazineId.value
        if (magazineId.isNullOrBlank()) {
            _uiState.update { it.copy(error = "Журнал ещё не создан") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(isIssueSubmitting = true, error = null, isSuccess = false)
            }

            val pdfFile = try {
                val bytes = context.contentResolver.openInputStream(pdfUri)!!.use { it.readBytes() }
                val mimeType = context.contentResolver.getType(pdfUri) ?: "application/pdf"
                val fileName = _uiState.value.selectedPdfName ?: "issue.pdf"
                FileData(bytes = bytes, mimeType = mimeType, fileName = fileName)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isIssueSubmitting = false, error = "Не удалось прочитать PDF-файл")
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
                    _uiState.update { it.copy(isIssueSubmitting = false, isSuccess = true) }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(isIssueSubmitting = false, error = result.message)
                    }
                }
                ApiResult.Unauthorized -> Unit
                ApiResult.NetworkError -> {
                    _uiState.update {
                        it.copy(isIssueSubmitting = false, error = NETWORK_ERROR)
                    }
                }
            }
        }
    }

    fun submit(
        title: String,
        publisher: String?,
        yearFounded: Int?,
        categoryId: Int,
        description: String?,
    ) {
        if (title.isBlank()) {
            _uiState.update { it.copy(error = "Укажите название журнала") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null, isSuccess = false) }

            val request = CreateMagazineRequest(
                title = title.trim(),
                publisher = publisher?.trim()?.takeIf { it.isNotEmpty() },
                yearFounded = yearFounded,
                categoryId = categoryId,
                description = description?.trim()?.takeIf { it.isNotEmpty() },
            )

            when (val createResult = uploadMagazineUseCase(request)) {
                is ApiResult.Success -> {
                    val magazineId = createResult.data.id
                    val imageUri = _uiState.value.selectedImageUri
                    if (imageUri == null) {
                        _uiState.update { it.copy(isSubmitting = false) }
                        onMagazineCreated(magazineId)
                        return@launch
                    }

                    val coverFile = UriFileReader.read(
                        context = context,
                        uri = imageUri,
                        defaultMimeType = "image/*",
                        defaultFileName = "cover.jpg",
                    )
                    if (coverFile == null) {
                        _uiState.update {
                            it.copy(
                                isSubmitting = false,
                                error = "Не удалось прочитать изображение обложки",
                            )
                        }
                        return@launch
                    }

                    when (val coverResult = uploadCoverUseCase(magazineId, coverFile)) {
                        is ApiResult.Success -> {
                            _uiState.update { it.copy(isSubmitting = false) }
                            onMagazineCreated(magazineId)
                        }
                        is ApiResult.Error -> {
                            _uiState.update {
                                it.copy(isSubmitting = false, error = coverResult.message)
                            }
                        }
                        ApiResult.Unauthorized -> Unit
                        ApiResult.NetworkError -> {
                            _uiState.update {
                                it.copy(isSubmitting = false, error = NETWORK_ERROR)
                            }
                        }
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(isSubmitting = false, error = createResult.message)
                    }
                }
                ApiResult.Unauthorized -> Unit
                ApiResult.NetworkError -> {
                    _uiState.update {
                        it.copy(isSubmitting = false, error = NETWORK_ERROR)
                    }
                }
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCategories = true) }
            when (val result = getCategoriesUseCase()) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(categories = result.data, isLoadingCategories = false)
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(isLoadingCategories = false, error = result.message)
                    }
                }
                ApiResult.Unauthorized -> Unit
                ApiResult.NetworkError -> {
                    _uiState.update {
                        it.copy(isLoadingCategories = false, error = NETWORK_ERROR)
                    }
                }
            }
        }
    }

    companion object {
        private const val NETWORK_ERROR = "Нет подключения к сети"
    }
}
