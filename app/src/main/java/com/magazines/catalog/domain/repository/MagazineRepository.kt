package com.magazines.catalog.domain.repository

import com.magazines.catalog.data.remote.ApiResult
import com.magazines.catalog.domain.model.CreateMagazineRequest
import com.magazines.catalog.domain.model.FileData
import com.magazines.catalog.domain.model.Magazine
import com.magazines.catalog.domain.model.PagedData

interface MagazineRepository {
    suspend fun getMagazines(
        page: Int,
        pageSize: Int,
        search: String? = null,
        categoryId: Int? = null,
    ): ApiResult<PagedData<Magazine>>

    suspend fun getMagazineById(id: String): ApiResult<Magazine>
    suspend fun searchMagazines(query: String, page: Int, pageSize: Int): ApiResult<PagedData<Magazine>>
    suspend fun getMyMagazines(): ApiResult<List<Magazine>>
    suspend fun uploadMagazine(request: CreateMagazineRequest): ApiResult<Magazine>
    suspend fun uploadCover(magazineId: String, cover: FileData): ApiResult<Magazine>
    suspend fun deleteMagazine(id: String): ApiResult<Unit>
}
