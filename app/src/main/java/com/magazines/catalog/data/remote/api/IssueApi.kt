package com.magazines.catalog.data.remote.api

import com.magazines.catalog.data.remote.dto.IssueDto
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface IssueApi {

    @GET("api/magazines/{id}/issues")
    suspend fun getIssues(@Path("id") id: String): Response<List<IssueDto>>

    @POST("api/magazines/{id}/cover")
    suspend fun uploadCover(
        @Path("id") id: String,
        @Body body: MultipartBody,
    ): Response<Unit>
}
