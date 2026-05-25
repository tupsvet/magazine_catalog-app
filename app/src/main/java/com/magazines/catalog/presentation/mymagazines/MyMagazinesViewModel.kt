package com.magazines.catalog.presentation.mymagazines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.magazines.catalog.data.remote.ApiResult
import com.magazines.catalog.domain.model.Magazine
import com.magazines.catalog.domain.model.MagazineStatus
import com.magazines.catalog.domain.usecase.auth.GetMeUseCase
import com.magazines.catalog.domain.usecase.magazine.GetMyMagazinesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MyMagazinesUiState(
    val isLoading: Boolean = false,
    val selectedTab: Int = 0,
    val approved: List<Magazine> = emptyList(),
    val pending: List<Magazine> = emptyList(),
    val rejected: List<Magazine> = emptyList(),
    val currentUserId: String? = null,
    val error: String? = null,
)

@HiltViewModel
class MyMagazinesViewModel @Inject constructor(
    private val getMyMagazinesUseCase: GetMyMagazinesUseCase,
    private val getMeUseCase: GetMeUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyMagazinesUiState())
    val uiState: StateFlow<MyMagazinesUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
        loadMagazines()
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun refresh() {
        loadMagazines()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            when (val result = getMeUseCase()) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(currentUserId = result.data.id) }
                }
                else -> Unit
            }
        }
    }

    private fun loadMagazines() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = getMyMagazinesUseCase()) {
                is ApiResult.Success -> {
                    val magazines = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            approved = magazines.filter { m -> m.status == MagazineStatus.APPROVED },
                            pending = magazines.filter { m -> m.status == MagazineStatus.PENDING },
                            rejected = magazines.filter { m -> m.status == MagazineStatus.REJECTED },
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = result.message)
                    }
                }
                ApiResult.NetworkError -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = NETWORK_ERROR)
                    }
                }
            }
        }
    }

    companion object {
        private const val NETWORK_ERROR = "Нет подключения к сети"
    }
}
