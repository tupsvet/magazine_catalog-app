package com.magazines.catalog.domain.usecase.favorite

import com.magazines.catalog.data.remote.ApiResult
import com.magazines.catalog.domain.model.Magazine
import com.magazines.catalog.domain.model.PagedData
import com.magazines.catalog.domain.repository.FavoriteRepository
import javax.inject.Inject

class GetFavoritesUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
) {
    suspend operator fun invoke(page: Int, pageSize: Int): ApiResult<PagedData<Magazine>> =
        favoriteRepository.getFavorites(page, pageSize)
}
