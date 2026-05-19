package com.magazines.catalog.domain.usecase.magazine

import com.magazines.catalog.data.remote.ApiResult
import com.magazines.catalog.domain.model.CreateMagazineRequest
import com.magazines.catalog.domain.model.Magazine
import com.magazines.catalog.domain.repository.MagazineRepository
import javax.inject.Inject

class UploadMagazineUseCase @Inject constructor(
    private val magazineRepository: MagazineRepository,
) {
    suspend operator fun invoke(request: CreateMagazineRequest): ApiResult<Magazine> =
        magazineRepository.uploadMagazine(request)
}
