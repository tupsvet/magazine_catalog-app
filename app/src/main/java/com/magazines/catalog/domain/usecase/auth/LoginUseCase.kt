package com.magazines.catalog.domain.usecase.auth

import com.magazines.catalog.data.remote.ApiResult
import com.magazines.catalog.domain.model.User
import com.magazines.catalog.domain.repository.AuthRepository
class LoginUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String): ApiResult<User> =
        authRepository.login(email, password)
}
