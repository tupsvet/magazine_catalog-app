package com.magazines.catalog.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.magazines.catalog.data.remote.ApiResult
import com.magazines.catalog.data.remote.ignoreUnauthorized
import com.magazines.catalog.domain.model.Magazine
import com.magazines.catalog.domain.usecase.admin.ApproveMagazineUseCase
import com.magazines.catalog.domain.usecase.admin.GetPendingMagazinesUseCase
import com.magazines.catalog.domain.usecase.admin.RejectMagazineUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminUiState(
    val pendingMagazines: List<Magazine> = emptyList(),
    val isLoading: Boolean = false,
    val processingIds: Set<String> = emptySet(),
    val error: String? = null,
)

@HiltViewModel
class AdminPanelViewModel @Inject constructor(
    private val getPendingMagazinesUseCase: GetPendingMagazinesUseCase,
    private val approveMagazineUseCase: ApproveMagazineUseCase,
    private val rejectMagazineUseCase: RejectMagazineUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = getPendingMagazinesUseCase()) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, pendingMagazines = result.data)
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                ApiResult.Unauthorized -> Unit
                ApiResult.NetworkError -> {
                    _uiState.update { it.copy(isLoading = false, error = NETWORK_ERROR) }
                }
            }
        }
    }

    fun approve(magazineId: String) {
        viewModelScope.launch {
            markProcessing(magazineId, true)
            when (val result = approveMagazineUseCase(magazineId)) {
                is ApiResult.Success -> removeFromList(magazineId)
                is ApiResult.Error -> _uiState.update { it.copy(error = result.message) }
                ApiResult.Unauthorized -> Unit
                ApiResult.NetworkError -> _uiState.update { it.copy(error = NETWORK_ERROR) }
            }
            markProcessing(magazineId, false)
        }
    }

    fun reject(magazineId: String, reason: String?) {
        viewModelScope.launch {
            markProcessing(magazineId, true)
            when (val result = rejectMagazineUseCase(magazineId, reason)) {
                is ApiResult.Success -> removeFromList(magazineId)
                is ApiResult.Error -> _uiState.update { it.copy(error = result.message) }
                ApiResult.Unauthorized -> Unit
                ApiResult.NetworkError -> _uiState.update { it.copy(error = NETWORK_ERROR) }
            }
            markProcessing(magazineId, false)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun removeFromList(magazineId: String) {
        _uiState.update { state ->
            state.copy(pendingMagazines = state.pendingMagazines.filterNot { it.id == magazineId })
        }
    }

    private fun markProcessing(magazineId: String, processing: Boolean) {
        _uiState.update { state ->
            val ids = state.processingIds.toMutableSet()
            if (processing) ids.add(magazineId) else ids.remove(magazineId)
            state.copy(processingIds = ids)
        }
    }

    companion object {
        private const val NETWORK_ERROR = "Нет подключения к сети"
    }
}
