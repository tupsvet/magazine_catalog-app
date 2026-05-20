package com.magazines.catalog.domain.usecase.auth

import com.magazines.catalog.data.remote.ApiResult
import com.magazines.catalog.domain.repository.AuthRepository
class LogoutUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): ApiResult<Unit> = authRepository.logout()
}
