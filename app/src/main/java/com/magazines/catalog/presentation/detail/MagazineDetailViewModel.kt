package com.magazines.catalog.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.magazines.catalog.data.remote.ApiResult
import com.magazines.catalog.domain.model.CreateReviewRequest
import com.magazines.catalog.domain.model.Issue
import com.magazines.catalog.domain.model.Magazine
import com.magazines.catalog.domain.model.Review
import com.magazines.catalog.domain.model.UserRole
import com.magazines.catalog.domain.usecase.auth.GetMeUseCase
import com.magazines.catalog.domain.usecase.favorite.AddFavoriteUseCase
import com.magazines.catalog.domain.usecase.favorite.IsFavoriteUseCase
import com.magazines.catalog.domain.usecase.favorite.RemoveFavoriteUseCase
import com.magazines.catalog.domain.usecase.issue.GetIssuesUseCase
import com.magazines.catalog.domain.usecase.magazine.GetMagazineByIdUseCase
import com.magazines.catalog.domain.usecase.review.CreateReviewUseCase
import com.magazines.catalog.domain.usecase.review.DeleteReviewUseCase
import com.magazines.catalog.domain.usecase.review.GetReviewsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val magazine: Magazine? = null,
    val issues: List<Issue> = emptyList(),
    val reviews: List<Review> = emptyList(),
    val isFavorite: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentUserReview: Review? = null,
    val isCurrentUserOwner: Boolean = false,
    val isAdmin: Boolean = false,
    val currentUserId: String? = null,
    val isSubmittingReview: Boolean = false,
)

@HiltViewModel
class MagazineDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getMagazineByIdUseCase: GetMagazineByIdUseCase,
    private val getIssuesUseCase: GetIssuesUseCase,
    private val getReviewsUseCase: GetReviewsUseCase,
    private val isFavoriteUseCase: IsFavoriteUseCase,
    private val addFavoriteUseCase: AddFavoriteUseCase,
    private val removeFavoriteUseCase: RemoveFavoriteUseCase,
    private val createReviewUseCase: CreateReviewUseCase,
    private val deleteReviewUseCase: DeleteReviewUseCase,
    private val getMeUseCase: GetMeUseCase,
) : ViewModel() {

    private val magazineId: String = savedStateHandle.get<String>("magazineId").orEmpty()

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        init()
    }

    fun init() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            coroutineScope {
                val magazineDeferred = async { getMagazineByIdUseCase(magazineId) }
                val issuesDeferred = async { getIssuesUseCase(magazineId) }
                val reviewsDeferred = async { getReviewsUseCase(magazineId, page = 1, pageSize = 100) }
                val favoriteDeferred = async { isFavoriteUseCase(magazineId) }
                val userDeferred = async { getMeUseCase() }

                val magazineResult = magazineDeferred.await()
                val issuesResult = issuesDeferred.await()
                val reviewsResult = reviewsDeferred.await()
                val favoriteResult = favoriteDeferred.await()
                val userResult = userDeferred.await()

                var error: String? = null
                var magazine: Magazine? = null
                var issues = emptyList<Issue>()
                var reviews = emptyList<Review>()
                var isFavorite = false
                var currentUserId: String? = null
                var isAdmin = false
                var isOwner = false

                when (magazineResult) {
                    is ApiResult.Success -> magazine = magazineResult.data
                    is ApiResult.Error -> error = magazineResult.message
                    ApiResult.NetworkError -> error = NETWORK_ERROR
                }

                when (issuesResult) {
                    is ApiResult.Success -> issues = issuesResult.data
                    is ApiResult.Error -> if (error == null) error = issuesResult.message
                    ApiResult.NetworkError -> if (error == null) error = NETWORK_ERROR
                }

                when (reviewsResult) {
                    is ApiResult.Success -> reviews = reviewsResult.data.items
                    is ApiResult.Error -> if (error == null) error = reviewsResult.message
                    ApiResult.NetworkError -> if (error == null) error = NETWORK_ERROR
                }

                when (favoriteResult) {
                    is ApiResult.Success -> isFavorite = favoriteResult.data
                    else -> Unit
                }

                when (userResult) {
                    is ApiResult.Success -> {
                        currentUserId = userResult.data.id
                        isAdmin = userResult.data.role == UserRole.ADMIN
                        isOwner = magazine?.uploadedBy == userResult.data.id
                    }
                    else -> Unit
                }

                val currentUserReview = currentUserId?.let { userId ->
                    reviews.find { it.userId == userId }
                }

                _uiState.update {
                    it.copy(
                        magazine = magazine,
                        issues = issues,
                        reviews = reviews,
                        isFavorite = isFavorite,
                        isLoading = false,
                        error = error,
                        currentUserReview = currentUserReview,
                        isCurrentUserOwner = isOwner,
                        isAdmin = isAdmin,
                        currentUserId = currentUserId,
                    )
                }
            }
        }
    }

    fun toggleFavorite() {
        if (_uiState.value.currentUserId == null) return

        viewModelScope.launch {
            val isFavorite = _uiState.value.isFavorite
            val result = if (isFavorite) {
                removeFavoriteUseCase(magazineId)
            } else {
                addFavoriteUseCase(magazineId)
            }

            when (result) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isFavorite = !isFavorite) }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(error = result.message) }
                }
                ApiResult.NetworkError -> {
                    _uiState.update { it.copy(error = NETWORK_ERROR) }
                }
            }
        }
    }

    fun createReview(rating: Int, comment: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingReview = true, error = null) }

            val result = createReviewUseCase(
                CreateReviewRequest(
                    magazineId = magazineId,
                    rating = rating,
                    comment = comment?.takeIf { it.isNotBlank() },
                ),
            )

            when (result) {
                is ApiResult.Success -> {
                    val newReview = result.data
                    _uiState.update { state ->
                        state.copy(
                            reviews = listOf(newReview) + state.reviews,
                            currentUserReview = newReview,
                            isSubmittingReview = false,
                            magazine = state.magazine?.copy(
                                reviewsCount = state.magazine.reviewsCount + 1,
                            ),
                        )
                    }
                    refreshMagazine()
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isSubmittingReview = false, error = result.message) }
                }
                ApiResult.NetworkError -> {
                    _uiState.update { it.copy(isSubmittingReview = false, error = NETWORK_ERROR) }
                }
            }
        }
    }

    fun deleteReview(reviewId: String) {
        viewModelScope.launch {
            when (val result = deleteReviewUseCase(reviewId)) {
                is ApiResult.Success -> {
                    _uiState.update { state ->
                        val wasOwnReview = state.currentUserReview?.id == reviewId
                        state.copy(
                            reviews = state.reviews.filter { it.id != reviewId },
                            currentUserReview = if (wasOwnReview) null else state.currentUserReview,
                            magazine = state.magazine?.copy(
                                reviewsCount = (state.magazine.reviewsCount - 1).coerceAtLeast(0),
                            ),
                        )
                    }
                    refreshMagazine()
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(error = result.message) }
                }
                ApiResult.NetworkError -> {
                    _uiState.update { it.copy(error = NETWORK_ERROR) }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun refreshMagazine() {
        viewModelScope.launch {
            when (val result = getMagazineByIdUseCase(magazineId)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(magazine = result.data) }
                }
                else -> Unit
            }
        }
    }

    companion object {
        private const val NETWORK_ERROR = "Нет подключения к сети"
    }
}
