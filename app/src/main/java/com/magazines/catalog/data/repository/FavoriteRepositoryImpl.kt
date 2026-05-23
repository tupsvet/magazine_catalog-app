package com.magazines.catalog.data.repository

import com.magazines.catalog.data.mapper.toDomain
import com.magazines.catalog.data.remote.ApiResult
import com.magazines.catalog.data.remote.api.FavoriteApi
import com.magazines.catalog.data.remote.safeApiCall
import com.magazines.catalog.domain.model.Magazine
import com.magazines.catalog.domain.model.PagedData
import com.magazines.catalog.domain.repository.FavoriteRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepositoryImpl @Inject constructor(
    private val favoriteApi: FavoriteApi,
) : FavoriteRepository {

    override suspend fun getFavorites(page: Int, pageSize: Int): ApiResult<PagedData<Magazine>> {
        return when (val result = safeApiCall { favoriteApi.getFavorites(page, pageSize) }) {
            is ApiResult.Success -> ApiResult.Success(result.data.toDomain { it.toDomain() })
            is ApiResult.Error -> ApiResult.Error(result.code, result.message)
            ApiResult.NetworkError -> ApiResult.NetworkError
        }
    }

    override suspend fun addFavorite(magazineId: String): ApiResult<Unit> {
        return when (val result = safeApiCall { favoriteApi.addFavorite(magazineId) }) {
            is ApiResult.Success -> ApiResult.Success(Unit)
            is ApiResult.Error -> ApiResult.Error(result.code, result.message)
            ApiResult.NetworkError -> ApiResult.NetworkError
        }
    }

    override suspend fun removeFavorite(magazineId: String): ApiResult<Unit> {
        return when (val result = safeApiCall { favoriteApi.removeFavorite(magazineId) }) {
            is ApiResult.Success -> ApiResult.Success(Unit)
            is ApiResult.Error -> ApiResult.Error(result.code, result.message)
            ApiResult.NetworkError -> ApiResult.NetworkError
        }
    }

    override suspend fun isFavorite(magazineId: String): ApiResult<Boolean> {
        return when (val result = getFavorites(page = 1, pageSize = 100)) {
            is ApiResult.Success -> {
                ApiResult.Success(result.data.items.any { it.id == magazineId })
            }
            is ApiResult.Error -> ApiResult.Error(result.code, result.message)
            ApiResult.NetworkError -> ApiResult.NetworkError
        }
    }
}
