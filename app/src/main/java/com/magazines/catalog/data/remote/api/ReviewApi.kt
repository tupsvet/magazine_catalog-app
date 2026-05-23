package com.magazines.catalog.data.remote.api

import com.magazines.catalog.data.remote.dto.ReviewDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

data class ReviewRequest(
    val rating: Int,
    val comment: String?,
)

interface ReviewApi {

    @GET("api/magazines/{id}/reviews")
    suspend fun getReviews(@Path("id") id: String): Response<List<ReviewDto>>

    @POST("api/magazines/{id}/reviews")
    suspend fun createReview(
        @Path("id") id: String,
        @Body body: ReviewRequest,
    ): Response<ReviewDto>

    @PUT("api/reviews/{id}")
    suspend fun updateReview(
        @Path("id") id: String,
        @Body body: ReviewRequest,
    ): Response<ReviewDto>

    @DELETE("api/reviews/{id}")
    suspend fun deleteReview(@Path("id") id: String): Response<Unit>
}
