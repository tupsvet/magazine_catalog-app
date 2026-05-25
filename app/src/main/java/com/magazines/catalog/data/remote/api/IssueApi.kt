package com.magazines.catalog.data.remote.api

import com.magazines.catalog.data.remote.dto.IssueDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface IssueApi {

    @GET("api/magazines/{id}/issues")
    suspend fun getIssues(@Path("id") id: String): Response<List<IssueDto>>

    @Multipart
    @POST("api/magazines/{id}/issues")
    suspend fun uploadIssue(
        @Path("id") id: String,
        @Part("issueNumber") issueNumber: RequestBody,
        @Part("publicationDate") publicationDate: RequestBody?,
        @Part pdf: MultipartBody.Part,
    ): Response<IssueDto>
}
