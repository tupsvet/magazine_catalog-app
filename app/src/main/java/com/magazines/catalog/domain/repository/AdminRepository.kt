package com.magazines.catalog.domain.repository

import com.magazines.catalog.data.remote.ApiResult
import com.magazines.catalog.domain.model.Magazine
import com.magazines.catalog.domain.model.PagedData

interface AdminRepository {
    suspend fun getPendingMagazines(page: Int, pageSize: Int): ApiResult<PagedData<Magazine>>
    suspend fun approveMagazine(id: String): ApiResult<Magazine>
    suspend fun rejectMagazine(id: String): ApiResult<Magazine>
}
