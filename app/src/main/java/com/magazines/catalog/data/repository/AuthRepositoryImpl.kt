package com.magazines.catalog.data.repository

import com.magazines.catalog.data.local.prefs.TokenStorage
import com.magazines.catalog.data.mapper.toDomain
import com.magazines.catalog.data.remote.ApiResult
import com.magazines.catalog.data.remote.api.AuthApi
import com.magazines.catalog.data.remote.dto.LoginRequest
import com.magazines.catalog.data.remote.dto.RegisterRequest
import com.magazines.catalog.data.remote.safeApiCall
import com.magazines.catalog.domain.model.User
import com.magazines.catalog.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val tokenStorage: TokenStorage,
) : AuthRepository {

    override suspend fun login(email: String, password: String): ApiResult<User> {
        return when (val result = safeApiCall { authApi.login(LoginRequest(email, password)) }) {
            is ApiResult.Success -> {
                tokenStorage.saveToken(result.data.token)
                ApiResult.Success(result.data.user.toDomain())
            }
            is ApiResult.Error -> ApiResult.Error(result.code, result.message)
            ApiResult.NetworkError -> ApiResult.NetworkError
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        displayName: String?,
    ): ApiResult<User> {
        return when (
            val result = safeApiCall {
                authApi.register(RegisterRequest(email, password, displayName))
            }
        ) {
            is ApiResult.Success -> {
                tokenStorage.saveToken(result.data.token)
                ApiResult.Success(result.data.user.toDomain())
            }
            is ApiResult.Error -> ApiResult.Error(result.code, result.message)
            ApiResult.NetworkError -> ApiResult.NetworkError
        }
    }

    override suspend fun getMe(): ApiResult<User> {
        return when (val result = safeApiCall { authApi.getMe() }) {
            is ApiResult.Success -> ApiResult.Success(result.data.toDomain())
            is ApiResult.Error -> ApiResult.Error(result.code, result.message)
            ApiResult.NetworkError -> ApiResult.NetworkError
        }
    }

    override suspend fun logout(): ApiResult<Unit> {
        tokenStorage.clearToken()
        return ApiResult.Success(Unit)
    }
}
