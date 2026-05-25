package com.magazines.catalog.data.remote.api

import com.magazines.catalog.data.remote.dto.CreateMagazineRequestDto
import com.magazines.catalog.data.remote.dto.MagazineDto
import com.magazines.catalog.data.remote.dto.PagedResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface MagazineApi {

    @GET("api/magazines")
    suspend fun getMagazines(
        @Query("search") search: String? = null,
        @Query("category") category: Int? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
    ): Response<PagedResponse<MagazineDto>>

    @GET("api/magazines/{id}")
    suspend fun getMagazineById(@Path("id") id: String): Response<MagazineDto>

    @GET("api/magazines/mine")
    suspend fun getMyMagazines(): Response<List<MagazineDto>>

    @POST("api/magazines")
    suspend fun createMagazine(@Body body: CreateMagazineRequestDto): Response<MagazineDto>

    @Multipart
    @POST("api/magazines/{id}/cover")
    suspend fun uploadCover(
        @Path("id") id: String,
        @Part cover: MultipartBody.Part,
    ): Response<MagazineDto>
}
