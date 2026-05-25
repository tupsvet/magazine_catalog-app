package com.magazines.catalog.data.repository

import com.magazines.catalog.data.mapper.toDomain
import com.magazines.catalog.data.remote.ApiResult
import com.magazines.catalog.data.remote.api.MagazineApi
import com.magazines.catalog.data.remote.dto.CreateMagazineRequestDto
import com.magazines.catalog.data.remote.safeApiCall
import com.magazines.catalog.data.remote.toMultipartPart
import com.magazines.catalog.domain.model.CreateMagazineRequest
import com.magazines.catalog.domain.model.FileData
import com.magazines.catalog.domain.model.Magazine
import com.magazines.catalog.domain.model.PagedData
import com.magazines.catalog.domain.repository.MagazineRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MagazineRepositoryImpl @Inject constructor(
    private val magazineApi: MagazineApi,
) : MagazineRepository {

    override suspend fun getMagazines(
        page: Int,
        pageSize: Int,
        search: String?,
        categoryId: Int?,
    ): ApiResult<PagedData<Magazine>> {
        return when (
            val result = safeApiCall {
                magazineApi.getMagazines(
                    search = search,
                    category = categoryId,
                    page = page,
                    pageSize = pageSize,
                )
            }
        ) {
            is ApiResult.Success -> ApiResult.Success(result.data.toDomain { it.toDomain() })
            is ApiResult.Error -> ApiResult.Error(result.code, result.message)
            ApiResult.NetworkError -> ApiResult.NetworkError
        }
    }

    override suspend fun getMagazineById(id: String): ApiResult<Magazine> {
        return when (val result = safeApiCall { magazineApi.getMagazineById(id) }) {
            is ApiResult.Success -> ApiResult.Success(result.data.toDomain())
            is ApiResult.Error -> ApiResult.Error(result.code, result.message)
            ApiResult.NetworkError -> ApiResult.NetworkError
        }
    }

    override suspend fun searchMagazines(
        query: String,
        page: Int,
        pageSize: Int,
    ): ApiResult<PagedData<Magazine>> = getMagazines(page, pageSize, search = query)

    override suspend fun getMyMagazines(): ApiResult<List<Magazine>> {
        return when (val result = safeApiCall { magazineApi.getMyMagazines() }) {
            is ApiResult.Success -> ApiResult.Success(result.data.map { it.toDomain() })
            is ApiResult.Error -> ApiResult.Error(result.code, result.message)
            ApiResult.NetworkError -> ApiResult.NetworkError
        }
    }

    override suspend fun uploadMagazine(request: CreateMagazineRequest): ApiResult<Magazine> {
        val body = CreateMagazineRequestDto(
            title = request.title,
            publisher = request.publisher,
            yearFounded = request.yearFounded,
            categoryId = request.categoryId,
            description = request.description,
        )
        return when (val result = safeApiCall { magazineApi.createMagazine(body) }) {
            is ApiResult.Success -> ApiResult.Success(result.data.toDomain())
            is ApiResult.Error -> ApiResult.Error(result.code, result.message)
            ApiResult.NetworkError -> ApiResult.NetworkError
        }
    }

    override suspend fun uploadCover(magazineId: String, cover: FileData): ApiResult<Magazine> {
        val part = cover.toMultipartPart("cover")
        return when (val result = safeApiCall { magazineApi.uploadCover(magazineId, part) }) {
            is ApiResult.Success -> ApiResult.Success(result.data.toDomain())
            is ApiResult.Error -> ApiResult.Error(result.code, result.message)
            ApiResult.NetworkError -> ApiResult.NetworkError
        }
    }

    override suspend fun deleteMagazine(id: String): ApiResult<Unit> {
        return ApiResult.Error(-1, "Not implemented")
    }
}
