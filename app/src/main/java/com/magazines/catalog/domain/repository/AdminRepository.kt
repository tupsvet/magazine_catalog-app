package com.magazines.catalog.domain.repository

import com.magazines.catalog.data.remote.ApiResult
import com.magazines.catalog.domain.model.Magazine

interface AdminRepository {
    suspend fun getPendingMagazines(): ApiResult<List<Magazine>>
    suspend fun approveMagazine(id: String): ApiResult<Magazine>
    suspend fun rejectMagazine(id: String, reason: String?): ApiResult<Magazine>
}
