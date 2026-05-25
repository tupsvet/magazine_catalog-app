package com.magazines.catalog.data.repository

import android.util.Log
import com.magazines.catalog.data.mapper.toDomain
import com.magazines.catalog.data.remote.ApiResult
import com.magazines.catalog.data.remote.api.AdminApi
import com.magazines.catalog.data.remote.dto.RejectRequest
import com.magazines.catalog.data.remote.safeApiCall
import com.magazines.catalog.data.remote.unauthorizedMessage
import com.magazines.catalog.domain.model.Magazine
import com.magazines.catalog.domain.repository.AdminRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepositoryImpl @Inject constructor(
    private val adminApi: AdminApi,
) : AdminRepository {

    override suspend fun getPendingMagazines(): ApiResult<List<Magazine>> {
        return when (val result = safeApiCall { adminApi.getPendingMagazines() }) {
            is ApiResult.Success -> {
                val magazines = result.data.map { it.toDomain() }
                Log.d(TAG, "Loaded ${magazines.size} pending magazines")
                ApiResult.Success(magazines)
            }
            is ApiResult.Error -> {
                Log.e(TAG, "getPendingMagazines error ${result.code}: ${result.message}")
                ApiResult.Error(result.code, result.message)
            }
            ApiResult.Unauthorized -> ApiResult.Error(401, unauthorizedMessage())
            ApiResult.NetworkError -> {
                Log.e(TAG, "getPendingMagazines: network error")
                ApiResult.NetworkError
            }
        }
    }

    override suspend fun approveMagazine(id: String): ApiResult<Magazine> {
        return when (val result = safeApiCall { adminApi.approveMagazine(id) }) {
            is ApiResult.Success -> ApiResult.Success(result.data.toDomain())
            is ApiResult.Error -> ApiResult.Error(result.code, result.message)
            ApiResult.Unauthorized -> ApiResult.Error(401, unauthorizedMessage())
            ApiResult.NetworkError -> ApiResult.NetworkError
        }
    }

    override suspend fun rejectMagazine(id: String, reason: String?): ApiResult<Magazine> {
        val body = RejectRequest(reason = reason?.trim()?.takeIf { it.isNotEmpty() })
        return when (val result = safeApiCall { adminApi.rejectMagazine(id, body) }) {
            is ApiResult.Success -> ApiResult.Success(result.data.toDomain())
            is ApiResult.Error -> ApiResult.Error(result.code, result.message)
            ApiResult.Unauthorized -> ApiResult.Error(401, unauthorizedMessage())
            ApiResult.NetworkError -> ApiResult.NetworkError
        }
    }

    companion object {
        private const val TAG = "Admin"
    }
}
