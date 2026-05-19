package com.magazines.catalog.domain.usecase.review

import com.magazines.catalog.data.remote.ApiResult
import com.magazines.catalog.domain.model.Review
import com.magazines.catalog.domain.model.UpdateReviewRequest
import com.magazines.catalog.domain.repository.ReviewRepository
import javax.inject.Inject

class UpdateReviewUseCase @Inject constructor(
    private val reviewRepository: ReviewRepository,
) {
    suspend operator fun invoke(reviewId: String, request: UpdateReviewRequest): ApiResult<Review> =
        reviewRepository.updateReview(reviewId, request)
}
