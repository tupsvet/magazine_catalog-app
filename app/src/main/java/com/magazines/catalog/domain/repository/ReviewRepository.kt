package com.magazines.catalog.domain.repository

import com.magazines.catalog.data.remote.ApiResult
import com.magazines.catalog.domain.model.CreateReviewRequest
import com.magazines.catalog.domain.model.PagedData
import com.magazines.catalog.domain.model.Review
import com.magazines.catalog.domain.model.UpdateReviewRequest

interface ReviewRepository {
    suspend fun getReviews(magazineId: String, page: Int, pageSize: Int): ApiResult<PagedData<Review>>
    suspend fun createReview(request: CreateReviewRequest): ApiResult<Review>
    suspend fun updateReview(reviewId: String, request: UpdateReviewRequest): ApiResult<Review>
    suspend fun deleteReview(reviewId: String): ApiResult<Unit>
}
