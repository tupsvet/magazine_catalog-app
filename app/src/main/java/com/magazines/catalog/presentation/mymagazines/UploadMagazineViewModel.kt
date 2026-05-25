package com.magazines.catalog.presentation.mymagazines

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.magazines.catalog.data.remote.ApiResult
import com.magazines.catalog.data.remote.ignoreUnauthorized
import com.magazines.catalog.data.remote.UriFileReader
import com.magazines.catalog.domain.model.Category
import com.magazines.catalog.domain.model.CreateMagazineRequest
import com.magazines.catalog.domain.usecase.category.GetCategoriesUseCase
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
)

@HiltViewModel
class UploadMagazineViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val uploadMagazineUseCase: UploadMagazineUseCase,
    private val uploadCoverUseCase: UploadCoverUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UploadMagazineUiState())
    val uiState: StateFlow<UploadMagazineUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    fun pickImage(uri: Uri) {
        _uiState.update { it.copy(selectedImageUri = uri, error = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
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
                        _uiState.update { it.copy(isSubmitting = false, isSuccess = true) }
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
                            _uiState.update { it.copy(isSubmitting = false, isSuccess = true) }
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
