package com.magazines.catalog.domain.usecase.magazine

import com.magazines.catalog.data.remote.ApiResult
import com.magazines.catalog.domain.model.FileData
import com.magazines.catalog.domain.model.Magazine
import com.magazines.catalog.domain.repository.MagazineRepository
import javax.inject.Inject

class UploadCoverUseCase @Inject constructor(
    private val magazineRepository: MagazineRepository,
) {
    suspend operator fun invoke(magazineId: String, cover: FileData): ApiResult<Magazine> =
        magazineRepository.uploadCover(magazineId, cover)
}
