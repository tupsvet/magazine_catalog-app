package com.magazines.catalog.data.repository

import android.util.Log
import androidx.room.withTransaction
import com.magazines.catalog.data.local.db.AppDatabase
import com.magazines.catalog.data.local.db.FavoriteDao
import com.magazines.catalog.data.mapper.toDomain
import com.magazines.catalog.data.mapper.toFavoriteEntity
import com.magazines.catalog.data.remote.ApiResult
import com.magazines.catalog.data.remote.api.FavoriteApi
import com.magazines.catalog.data.remote.safeApiCall
import com.magazines.catalog.data.remote.safeApiCallNoContent
import com.magazines.catalog.domain.model.Magazine
import com.magazines.catalog.domain.model.PagedData
import com.magazines.catalog.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepositoryImpl @Inject constructor(
    private val favoriteApi: FavoriteApi,
    private val favoriteDao: FavoriteDao,
    private val database: AppDatabase,
) : FavoriteRepository {

    override fun observeFavorites(): Flow<List<Magazine>> {
        return favoriteDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun syncFavorites(): ApiResult<Unit> {
        return when (val result = safeApiCall { favoriteApi.getFavorites() }) {
            is ApiResult.Success -> {
                val magazines = result.data.map { it.toDomain() }
                cacheFavorites(magazines)
                Log.d(TAG, "syncFavorites: cached ${magazines.size} items")
                ApiResult.Success(Unit)
            }
            is ApiResult.Error -> ApiResult.Error(result.code, result.message)
            ApiResult.NetworkError -> ApiResult.NetworkError
        }
    }

    override suspend fun getFavorites(page: Int, pageSize: Int): ApiResult<PagedData<Magazine>> {
        return when (val result = safeApiCall { favoriteApi.getFavorites() }) {
            is ApiResult.Success -> {
                val magazines = result.data.map { it.toDomain() }
                ApiResult.Success(
                    PagedData(
                        items = magazines,
                        page = 1,
                        pageSize = magazines.size,
                        totalItems = magazines.size,
                        totalPages = 1
                    )
                )
            }
            is ApiResult.Error -> ApiResult.Error(result.code, result.message)
            ApiResult.NetworkError -> ApiResult.NetworkError
        }
    }

    override suspend fun addFavorite(magazineId: String): ApiResult<Unit> {
        Log.d(TAG, "addFavorite: magazineId=$magazineId")
        return when (val result = safeApiCall { favoriteApi.addFavorite(magazineId) }) {
            is ApiResult.Success -> {
                favoriteDao.insert(result.data.toDomain().toFavoriteEntity())
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
                favoriteDao.deleteById(magazineId)
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
        val isFavorite = favoriteDao.isFavorite(magazineId)
        Log.d(TAG, "isFavorite(local): magazineId=$magazineId, isFavorite=$isFavorite")
        return ApiResult.Success(isFavorite)
    }

    private suspend fun cacheFavorites(magazines: List<Magazine>) {
        val cachedAt = System.currentTimeMillis()
        val entities = magazines.map { it.toFavoriteEntity(cachedAt) }
        database.withTransaction {
            favoriteDao.deleteAll()
            if (entities.isNotEmpty()) {
                favoriteDao.insert(*entities.toTypedArray())
            }
        }
    }

    companion object {
        private const val TAG = "Favorites"
    }
}
