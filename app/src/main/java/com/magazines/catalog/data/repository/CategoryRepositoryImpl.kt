package com.magazines.catalog.data.repository

import com.magazines.catalog.data.mapper.toDomain
import com.magazines.catalog.data.remote.ApiResult
import com.magazines.catalog.data.remote.api.CategoryApi
import com.magazines.catalog.data.remote.safeApiCall
import com.magazines.catalog.domain.model.Category
import com.magazines.catalog.domain.repository.CategoryRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryApi: CategoryApi,
) : CategoryRepository {

    override suspend fun getCategories(): ApiResult<List<Category>> {
        return when (val result = safeApiCall { categoryApi.getCategories() }) {
            is ApiResult.Success -> ApiResult.Success(result.data.map { it.toDomain() })
            is ApiResult.Error -> ApiResult.Error(result.code, result.message)
            ApiResult.NetworkError -> ApiResult.NetworkError
        }
    }
}
