package com.magazines.catalog.data.remote.api

import com.magazines.catalog.data.remote.dto.MagazineDto
import com.magazines.catalog.data.remote.dto.PagedResponse
import retrofit2.Response
import retrofit2.http.GET
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

    @GET("api/magazines/my")
    suspend fun getMyMagazines(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
    ): Response<PagedResponse<MagazineDto>>
}
