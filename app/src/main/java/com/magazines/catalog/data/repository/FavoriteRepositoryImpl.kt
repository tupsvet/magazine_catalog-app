package com.magazines.catalog.data.repository

import android.util.Log
import com.magazines.catalog.data.mapper.toDomain
import com.magazines.catalog.data.remote.ApiResult
import com.magazines.catalog.data.remote.api.FavoriteApi
import com.magazines.catalog.data.remote.safeApiCall
import com.magazines.catalog.data.remote.safeApiCallNoContent
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
        Log.d(TAG, "addFavorite: magazineId=$magazineId")
        return when (val result = safeApiCall { favoriteApi.addFavorite(magazineId) }) {
            is ApiResult.Success -> {
                Log.d(TAG, "addFavorite: success")
                ApiResult.Success(Unit)
            }
            is ApiResult.Error -> {
                Log.e(TAG, "addFavorite: error ${result.code} ${result.message}")
                ApiResult.Error(result.code, result.message)
            }
            ApiResult.NetworkError -> {
                Log.e(TAG, "addFavorite: network error")
                ApiResult.NetworkError
            }
        }
    }

    override suspend fun removeFavorite(magazineId: String): ApiResult<Unit> {
        Log.d(TAG, "removeFavorite: magazineId=$magazineId")
        return when (val result = safeApiCallNoContent { favoriteApi.removeFavorite(magazineId) }) {
            is ApiResult.Success -> {
                Log.d(TAG, "removeFavorite: success")
                ApiResult.Success(Unit)
            }
            is ApiResult.Error -> {
                Log.e(TAG, "removeFavorite: error ${result.code} ${result.message}")
                ApiResult.Error(result.code, result.message)
            }
            ApiResult.NetworkError -> {
                Log.e(TAG, "removeFavorite: network error")
                ApiResult.NetworkError
            }
        }
    }

    override suspend fun isFavorite(magazineId: String): ApiResult<Boolean> {
        var page = 1
        var totalPages = 1

        while (page <= totalPages) {
            when (val result = getFavorites(page = page, pageSize = FAVORITES_PAGE_SIZE)) {
                is ApiResult.Success -> {
                    val found = result.data.items.any { it.id == magazineId }
                    Log.d(
                        TAG,
                        "isFavorite: page=$page/${result.data.totalPages}, " +
                            "items=${result.data.items.size}, found=$found, magazineId=$magazineId",
                    )
                    if (found) {
                        return ApiResult.Success(true)
                    }
                    totalPages = result.data.totalPages.coerceAtLeast(1)
                    page++
                }
                is ApiResult.Error -> {
                    Log.e(TAG, "isFavorite: error ${result.code} ${result.message}")
                    return ApiResult.Error(result.code, result.message)
                }
                ApiResult.NetworkError -> {
                    Log.e(TAG, "isFavorite: network error")
                    return ApiResult.NetworkError
                }
            }
        }

        Log.d(TAG, "isFavorite: not found, magazineId=$magazineId")
        return ApiResult.Success(false)
    }

    companion object {
        private const val TAG = "Favorites"
        private const val FAVORITES_PAGE_SIZE = 100
    }
}
