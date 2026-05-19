package com.magazines.catalog.domain.repository

import com.magazines.catalog.data.remote.ApiResult
import com.magazines.catalog.domain.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): ApiResult<User>
    suspend fun register(email: String, password: String, displayName: String?): ApiResult<User>
    suspend fun getMe(): ApiResult<User>
    suspend fun logout(): ApiResult<Unit>
}
