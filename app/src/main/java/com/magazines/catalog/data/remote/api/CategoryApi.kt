package com.magazines.catalog.data.remote.api

import retrofit2.Response
import retrofit2.http.GET

interface CategoryApi {

    @GET("api/categories")
    suspend fun getCategories(): Response<List<CategoryDto>>
}

data class CategoryDto(
    val id: Int,
    val name: String,
    val description: String?,
)
