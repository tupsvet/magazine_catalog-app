package com.magazines.catalog.domain.usecase.magazine

import com.magazines.catalog.data.remote.ApiResult
import com.magazines.catalog.domain.repository.MagazineRepository
import javax.inject.Inject

class DeleteMagazineUseCase @Inject constructor(
    private val magazineRepository: MagazineRepository,
) {
    suspend operator fun invoke(id: String): ApiResult<Unit> = magazineRepository.deleteMagazine(id)
}
