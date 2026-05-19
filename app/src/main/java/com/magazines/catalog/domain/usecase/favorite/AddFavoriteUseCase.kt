package com.magazines.catalog.domain.usecase.favorite

import com.magazines.catalog.data.remote.ApiResult
import com.magazines.catalog.domain.repository.FavoriteRepository
import javax.inject.Inject

class AddFavoriteUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
) {
    suspend operator fun invoke(magazineId: String): ApiResult<Unit> =
        favoriteRepository.addFavorite(magazineId)
}
