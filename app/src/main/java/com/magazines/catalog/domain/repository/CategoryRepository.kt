package com.magazines.catalog.domain.repository

import com.magazines.catalog.data.remote.ApiResult
import com.magazines.catalog.domain.model.Category

interface CategoryRepository {
    suspend fun getCategories(): ApiResult<List<Category>>
}
