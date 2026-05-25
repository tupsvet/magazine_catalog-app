package com.magazines.catalog.data.repository

import com.magazines.catalog.data.mapper.toDomain
import com.magazines.catalog.data.remote.ApiResult
import com.magazines.catalog.data.remote.api.ReviewApi
import com.magazines.catalog.data.remote.api.ReviewRequest
import com.magazines.catalog.data.remote.safeApiCall
import com.magazines.catalog.data.remote.safeApiCallNoContent
import com.magazines.catalog.data.remote.unauthorizedMessage
import com.magazines.catalog.domain.model.CreateReviewRequest
import com.magazines.catalog.domain.model.PagedData
import com.magazines.catalog.domain.model.Review
import com.magazines.catalog.domain.model.UpdateReviewRequest
import com.magazines.catalog.domain.repository.ReviewRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewRepositoryImpl @Inject constructor(
    private val reviewApi: ReviewApi,
) : ReviewRepository {

    override suspend fun getReviews(
        magazineId: String,
        page: Int,
        pageSize: Int,
    ): ApiResult<PagedData<Review>> {
        return when (val result = safeApiCall { reviewApi.getReviews(magazineId) }) {
            is ApiResult.Success -> {
                val reviews = result.data.map { it.toDomain() }
                val startIndex = (page - 1) * pageSize
                val pageItems = reviews.drop(startIndex).take(pageSize)
                ApiResult.Success(
                    PagedData(
                        items = pageItems,
                        page = page,
                        pageSize = pageSize,
                        totalItems = reviews.size,
                        totalPages = if (reviews.isEmpty()) 0 else (reviews.size + pageSize - 1) / pageSize,
                    ),
                )
            }
            is ApiResult.Error -> ApiResult.Error(result.code, result.message)
            ApiResult.Unauthorized -> ApiResult.Error(401, unauthorizedMessage())
            ApiResult.NetworkError -> ApiResult.NetworkError
        }
    }

    override suspend fun createReview(request: CreateReviewRequest): ApiResult<Review> {
        return when (
            val result = safeApiCall {
                reviewApi.createReview(
                    id = request.magazineId,
                    body = ReviewRequest(rating = request.rating, comment = request.comment),
                )
            }
        ) {
            is ApiResult.Success -> ApiResult.Success(result.data.toDomain())
            is ApiResult.Error -> ApiResult.Error(result.code, result.message)
            ApiResult.Unauthorized -> ApiResult.Error(401, unauthorizedMessage())
            ApiResult.NetworkError -> ApiResult.NetworkError
        }
    }

    override suspend fun updateReview(reviewId: String, request: UpdateReviewRequest): ApiResult<Review> {
        return when (
            val result = safeApiCall {
                reviewApi.updateReview(
                    id = reviewId,
                    body = ReviewRequest(rating = request.rating, comment = request.comment),
                )
            }
        ) {
            is ApiResult.Success -> ApiResult.Success(result.data.toDomain())
            is ApiResult.Error -> ApiResult.Error(result.code, result.message)
            ApiResult.Unauthorized -> ApiResult.Error(401, unauthorizedMessage())
            ApiResult.NetworkError -> ApiResult.NetworkError
        }
    }

    override suspend fun deleteReview(reviewId: String): ApiResult<Unit> {
        return when (val result = safeApiCallNoContent { reviewApi.deleteReview(reviewId) }) {
            is ApiResult.Success -> ApiResult.Success(Unit)
            is ApiResult.Error -> ApiResult.Error(result.code, result.message)
            ApiResult.Unauthorized -> ApiResult.Error(401, unauthorizedMessage())
            ApiResult.NetworkError -> ApiResult.NetworkError
        }
    }
}
