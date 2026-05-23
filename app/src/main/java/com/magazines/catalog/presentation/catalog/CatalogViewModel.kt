package com.magazines.catalog.presentation.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.magazines.catalog.data.remote.ApiResult
import com.magazines.catalog.domain.model.Category
import com.magazines.catalog.domain.model.Magazine
import com.magazines.catalog.domain.usecase.auth.GetMeUseCase
import com.magazines.catalog.domain.usecase.category.GetCategoriesUseCase
import com.magazines.catalog.domain.usecase.magazine.GetMagazinesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CatalogUiState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val magazines: List<Magazine> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: Int? = null,
    val searchQuery: String = "",
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val currentUserId: String? = null,
    val error: String? = null,
)

@OptIn(FlowPreview::class)
@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val getMagazinesUseCase: GetMagazinesUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getMeUseCase: GetMeUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogUiState())
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    private val searchQueryFlow = MutableStateFlow("")

    init {
        loadCategories()
        loadCurrentUser()
        observeSearch()
        loadMagazines(refresh = true)
    }

    fun loadMagazines(refresh: Boolean = false) {
        viewModelScope.launch {
            fetchMagazines(page = 1, append = false, isRefresh = refresh)
        }
    }

    fun search(query: String) {
        searchQueryFlow.value = query
    }

    fun filterByCategory(categoryId: Int?) {
        _uiState.update { it.copy(selectedCategoryId = categoryId, currentPage = 0, totalPages = 0) }
        viewModelScope.launch {
            fetchMagazines(page = 1, append = false, isRefresh = false)
        }
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (state.isLoading || state.isLoadingMore || state.isRefreshing) return
        if (state.totalPages > 0 && state.currentPage >= state.totalPages) return

        viewModelScope.launch {
            fetchMagazines(page = state.currentPage + 1, append = true, isRefresh = false)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun observeSearch() {
        viewModelScope.launch {
            searchQueryFlow
                .drop(1)
                .debounce(SEARCH_DEBOUNCE_MS)
                .distinctUntilChanged()
                .collectLatest { query ->
                    _uiState.update {
                        it.copy(searchQuery = query, currentPage = 0, totalPages = 0)
                    }
                    fetchMagazines(page = 1, append = false, isRefresh = false)
                }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            when (val result = getCategoriesUseCase()) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(categories = result.data) }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(error = result.message) }
                }
                ApiResult.NetworkError -> {
                    _uiState.update { it.copy(error = "Нет подключения к сети") }
                }
            }
        }
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

    private suspend fun fetchMagazines(page: Int, append: Boolean, isRefresh: Boolean) {
        val state = _uiState.value
        val showInitialLoading = !append && state.magazines.isEmpty() && !isRefresh
        val showRefreshing = isRefresh && state.magazines.isNotEmpty()

        _uiState.update {
            it.copy(
                isLoading = showInitialLoading,
                isLoadingMore = append,
                isRefreshing = showRefreshing,
                error = if (append) it.error else null,
            )
        }

        val result = getMagazinesUseCase(
            page = page,
            pageSize = PAGE_SIZE,
            search = state.searchQuery.takeIf { it.isNotBlank() },
            categoryId = state.selectedCategoryId,
        )

        when (result) {
            is ApiResult.Success -> {
                val data = result.data
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        isRefreshing = false,
                        magazines = if (append) current.magazines + data.items else data.items,
                        currentPage = data.page,
                        totalPages = data.totalPages,
                    )
                }
            }
            is ApiResult.Error -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        isRefreshing = false,
                        error = result.message,
                    )
                }
            }
            ApiResult.NetworkError -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        isRefreshing = false,
                        error = "Нет подключения к сети",
                    )
                }
            }
        }
    }

    companion object {
        private const val PAGE_SIZE = 20
        private const val SEARCH_DEBOUNCE_MS = 300L
    }
}
