package com.magazines.catalog.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.magazines.catalog.data.remote.ApiResult
import com.magazines.catalog.data.remote.ignoreUnauthorized
import com.magazines.catalog.domain.model.Magazine
import com.magazines.catalog.domain.repository.FavoriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritesUiState(
    val isSyncing: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
) : ViewModel() {

    val favorites: StateFlow<List<Magazine>> = favoriteRepository
        .observeFavorites()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        syncWithServer()
    }

    fun syncWithServer() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, error = null) }
            when (val result = favoriteRepository.syncFavorites()) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isSyncing = false) }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(isSyncing = false, error = result.message)
                    }
                }
                ApiResult.Unauthorized -> Unit
                ApiResult.NetworkError -> {
                    _uiState.update {
                        it.copy(isSyncing = false, error = NETWORK_ERROR)
                    }
                }
            }
        }
    }

    fun removeFavorite(magazineId: String) {
        viewModelScope.launch {
            when (val result = favoriteRepository.removeFavorite(magazineId)) {
                is ApiResult.Success -> Unit
                is ApiResult.Error -> {
                    _uiState.update { it.copy(error = result.message) }
                }
                ApiResult.Unauthorized -> Unit
                ApiResult.NetworkError -> {
                    _uiState.update { it.copy(error = NETWORK_ERROR) }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    companion object {
        private const val NETWORK_ERROR = "Нет подключения к сети"
    }
}
