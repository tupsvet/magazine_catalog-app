package com.magazines.catalog.data.repository

import com.magazines.catalog.data.mapper.toDomain
import com.magazines.catalog.data.remote.ApiResult
import com.magazines.catalog.data.remote.api.IssueApi
import com.magazines.catalog.data.remote.safeApiCall
import com.magazines.catalog.domain.model.FileData
import com.magazines.catalog.domain.model.Issue
import com.magazines.catalog.domain.model.UploadIssueRequest
import com.magazines.catalog.domain.repository.IssueRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IssueRepositoryImpl @Inject constructor(
    private val issueApi: IssueApi,
) : IssueRepository {

    override suspend fun getIssues(magazineId: String): ApiResult<List<Issue>> {
        return when (val result = safeApiCall { issueApi.getIssues(magazineId) }) {
            is ApiResult.Success -> ApiResult.Success(result.data.map { it.toDomain() })
            is ApiResult.Error -> ApiResult.Error(result.code, result.message)
            ApiResult.NetworkError -> ApiResult.NetworkError
        }
    }

    override suspend fun uploadIssue(request: UploadIssueRequest, pdf: FileData): ApiResult<Issue> {
        return ApiResult.Error(-1, "Not implemented")
    }

    override suspend fun deleteIssue(magazineId: String, issueId: String): ApiResult<Unit> {
        return ApiResult.Error(-1, "Not implemented")
    }
}
