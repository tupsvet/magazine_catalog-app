package com.magazines.catalog.domain.usecase.review

import com.magazines.catalog.data.remote.ApiResult
import com.magazines.catalog.domain.repository.ReviewRepository
import javax.inject.Inject

class DeleteReviewUseCase @Inject constructor(
    private val reviewRepository: ReviewRepository,
) {
    suspend operator fun invoke(reviewId: String): ApiResult<Unit> =
        reviewRepository.deleteReview(reviewId)
}
