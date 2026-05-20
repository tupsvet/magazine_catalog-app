package com.magazines.catalog.domain.usecase.auth

import com.magazines.catalog.data.remote.ApiResult
import com.magazines.catalog.domain.model.User
import com.magazines.catalog.domain.repository.AuthRepository
class RegisterUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        displayName: String?,
    ): ApiResult<User> = authRepository.register(email, password, displayName)
}
