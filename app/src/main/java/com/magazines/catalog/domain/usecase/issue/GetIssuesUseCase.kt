package com.magazines.catalog.domain.usecase.issue

import com.magazines.catalog.data.remote.ApiResult
import com.magazines.catalog.domain.model.Issue
import com.magazines.catalog.domain.repository.IssueRepository
import javax.inject.Inject

class GetIssuesUseCase @Inject constructor(
    private val issueRepository: IssueRepository,
) {
    suspend operator fun invoke(magazineId: String): ApiResult<List<Issue>> =
        issueRepository.getIssues(magazineId)
}
