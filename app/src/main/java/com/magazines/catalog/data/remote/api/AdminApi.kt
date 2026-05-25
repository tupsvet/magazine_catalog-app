package com.magazines.catalog.data.remote.api

import com.magazines.catalog.data.remote.dto.MagazineDto
import com.magazines.catalog.data.remote.dto.RejectRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AdminApi {

    @GET("api/admin/magazines/pending")
    suspend fun getPendingMagazines(): Response<List<MagazineDto>>

    @POST("api/admin/magazines/{id}/approve")
    suspend fun approveMagazine(@Path("id") id: String): Response<MagazineDto>

    @POST("api/admin/magazines/{id}/reject")
    suspend fun rejectMagazine(
        @Path("id") id: String,
        @Body body: RejectRequest,
    ): Response<MagazineDto>
}
