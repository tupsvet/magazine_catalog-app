package com.magazines.catalog.domain.usecase.admin

import com.magazines.catalog.data.remote.ApiResult
import com.magazines.catalog.domain.model.Magazine
import com.magazines.catalog.domain.repository.AdminRepository
import javax.inject.Inject

class ApproveMagazineUseCase @Inject constructor(
    private val adminRepository: AdminRepository,
) {
    suspend operator fun invoke(id: String): ApiResult<Magazine> = adminRepository.approveMagazine(id)
}
