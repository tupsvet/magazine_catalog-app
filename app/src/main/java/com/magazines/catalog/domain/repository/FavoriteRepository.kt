package com.magazines.catalog.domain.repository

import com.magazines.catalog.data.remote.ApiResult
import com.magazines.catalog.domain.model.Magazine
import com.magazines.catalog.domain.model.PagedData

interface FavoriteRepository {
    suspend fun getFavorites(page: Int, pageSize: Int): ApiResult<PagedData<Magazine>>
    suspend fun addFavorite(magazineId: String): ApiResult<Unit>
    suspend fun removeFavorite(magazineId: String): ApiResult<Unit>
    suspend fun isFavorite(magazineId: String): ApiResult<Boolean>
}
