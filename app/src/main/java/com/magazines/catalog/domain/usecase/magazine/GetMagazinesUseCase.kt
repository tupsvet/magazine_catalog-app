package com.magazines.catalog.domain.usecase.magazine

import com.magazines.catalog.data.remote.ApiResult
import com.magazines.catalog.domain.model.Magazine
import com.magazines.catalog.domain.model.PagedData
import com.magazines.catalog.domain.repository.MagazineRepository
import javax.inject.Inject

class GetMagazinesUseCase @Inject constructor(
    private val magazineRepository: MagazineRepository,
) {
    suspend operator fun invoke(
        page: Int,
        pageSize: Int,
        search: String? = null,
        categoryId: Int? = null,
    ): ApiResult<PagedData<Magazine>> =
        magazineRepository.getMagazines(page, pageSize, search, categoryId)
}
