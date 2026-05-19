package com.magazines.catalog.domain.usecase.review

import com.magazines.catalog.data.remote.ApiResult
import com.magazines.catalog.domain.model.PagedData
import com.magazines.catalog.domain.model.Review
import com.magazines.catalog.domain.repository.ReviewRepository
import javax.inject.Inject

class GetReviewsUseCase @Inject constructor(
    private val reviewRepository: ReviewRepository,
) {
    suspend operator fun invoke(
        magazineId: String,
        page: Int,
        pageSize: Int,
    ): ApiResult<PagedData<Review>> = reviewRepository.getReviews(magazineId, page, pageSize)
}
