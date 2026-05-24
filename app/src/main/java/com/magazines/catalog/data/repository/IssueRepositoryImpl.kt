package com.magazines.catalog.data.repository

import android.util.Log
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
        Log.d(TAG, "getIssues: magazineId=$magazineId")
        return when (val result = safeApiCall { issueApi.getIssues(magazineId) }) {
            is ApiResult.Success -> {
                val issues = result.data.map { it.toDomain() }
                Log.d(TAG, "Loaded ${issues.size} issues for magazineId=$magazineId")
                issues.forEach { issue ->
                    Log.d(TAG, "issue #${issue.issueNumber} pdfUrl=${issue.pdfUrl}")
                }
                ApiResult.Success(issues)
            }
            is ApiResult.Error -> {
                Log.e(TAG, "getIssues error ${result.code}: ${result.message}")
                ApiResult.Error(result.code, result.message)
            }
            ApiResult.NetworkError -> {
                Log.e(TAG, "getIssues: network error")
                ApiResult.NetworkError
            }
        }
    }

    override suspend fun uploadIssue(request: UploadIssueRequest, pdf: FileData): ApiResult<Issue> {
        return ApiResult.Error(-1, "Not implemented")
    }

    override suspend fun deleteIssue(magazineId: String, issueId: String): ApiResult<Unit> {
        return ApiResult.Error(-1, "Not implemented")
    }

    companion object {
        private const val TAG = "Issues"
    }
}
