package com.magazines.catalog.data.remote.api

import com.magazines.catalog.data.remote.dto.MagazineDto
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface FavoriteApi {

    @GET("api/users/me/favorites")
    suspend fun getFavorites(): Response<List<MagazineDto>>

    @POST("api/users/me/favorites/{id}")
    suspend fun addFavorite(@Path("id") id: String): Response<MagazineDto>

    @DELETE("api/users/me/favorites/{id}")
    suspend fun removeFavorite(@Path("id") id: String): Response<Unit>
}

