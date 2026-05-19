package com.magazines.catalog.domain.usecase.review

import com.magazines.catalog.data.remote.ApiResult
import com.magazines.catalog.domain.model.CreateReviewRequest
import com.magazines.catalog.domain.model.Review
import com.magazines.catalog.domain.repository.ReviewRepository
import javax.inject.Inject

class CreateReviewUseCase @Inject constructor(
    private val reviewRepository: ReviewRepository,
) {
    suspend operator fun invoke(request: CreateReviewRequest): ApiResult<Review> =
        reviewRepository.createReview(request)
}
