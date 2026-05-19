package com.magazines.catalog.domain.usecase.issue

import com.magazines.catalog.data.remote.ApiResult
import com.magazines.catalog.domain.repository.IssueRepository
import javax.inject.Inject

class DeleteIssueUseCase @Inject constructor(
    private val issueRepository: IssueRepository,
) {
    suspend operator fun invoke(magazineId: String, issueId: String): ApiResult<Unit> =
        issueRepository.deleteIssue(magazineId, issueId)
}
