package com.magazines.catalog.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.magazines.catalog.data.remote.ApiResult
import com.magazines.catalog.domain.model.User
import com.magazines.catalog.domain.usecase.auth.GetMeUseCase
import com.magazines.catalog.domain.usecase.auth.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedOut: Boolean = false,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getMeUseCase: GetMeUseCase,
    private val logoutUseCase: LogoutUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = getMeUseCase()) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, user = result.data) }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                ApiResult.NetworkError -> {
                    _uiState.update { it.copy(isLoading = false, error = NETWORK_ERROR) }
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            _uiState.update { it.copy(isLoggedOut = true) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    companion object {
        private const val NETWORK_ERROR = "Нет подключения к сети"
    }
}
