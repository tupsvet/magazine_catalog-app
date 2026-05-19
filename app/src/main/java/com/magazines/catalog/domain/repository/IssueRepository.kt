package com.magazines.catalog.domain.repository

import com.magazines.catalog.data.remote.ApiResult
import com.magazines.catalog.domain.model.FileData
import com.magazines.catalog.domain.model.Issue
import com.magazines.catalog.domain.model.UploadIssueRequest

interface IssueRepository {
    suspend fun getIssues(magazineId: String): ApiResult<List<Issue>>
    suspend fun uploadIssue(request: UploadIssueRequest, pdf: FileData): ApiResult<Issue>
    suspend fun deleteIssue(magazineId: String, issueId: String): ApiResult<Unit>
}
