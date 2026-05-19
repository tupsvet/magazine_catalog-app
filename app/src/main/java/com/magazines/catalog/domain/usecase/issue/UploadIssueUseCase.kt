package com.magazines.catalog.domain.usecase.issue

import com.magazines.catalog.data.remote.ApiResult
import com.magazines.catalog.domain.model.FileData
import com.magazines.catalog.domain.model.Issue
import com.magazines.catalog.domain.model.UploadIssueRequest
import com.magazines.catalog.domain.repository.IssueRepository
import javax.inject.Inject

class UploadIssueUseCase @Inject constructor(
    private val issueRepository: IssueRepository,
) {
    suspend operator fun invoke(request: UploadIssueRequest, pdf: FileData): ApiResult<Issue> =
        issueRepository.uploadIssue(request, pdf)
}
