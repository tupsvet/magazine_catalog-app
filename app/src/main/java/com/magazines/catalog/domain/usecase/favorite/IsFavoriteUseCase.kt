package com.magazines.catalog.domain.usecase.favorite

import com.magazines.catalog.data.remote.ApiResult
import com.magazines.catalog.domain.repository.FavoriteRepository
import javax.inject.Inject

class IsFavoriteUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
) {
    suspend operator fun invoke(magazineId: String): ApiResult<Boolean> =
        favoriteRepository.isFavorite(magazineId)
}
