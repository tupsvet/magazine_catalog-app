package com.magazines.catalog.domain.usecase.auth

import com.magazines.catalog.data.remote.ApiResult
import com.magazines.catalog.domain.model.User
import com.magazines.catalog.domain.repository.AuthRepository
import javax.inject.Inject

class GetMeUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): ApiResult<User> = authRepository.getMe()
}
