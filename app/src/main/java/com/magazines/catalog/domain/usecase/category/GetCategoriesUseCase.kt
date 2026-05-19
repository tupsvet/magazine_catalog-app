package com.magazines.catalog.domain.usecase.category

import com.magazines.catalog.data.remote.ApiResult
import com.magazines.catalog.domain.model.Category
import com.magazines.catalog.domain.repository.CategoryRepository
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository,
) {
    suspend operator fun invoke(): ApiResult<List<Category>> = categoryRepository.getCategories()
}
