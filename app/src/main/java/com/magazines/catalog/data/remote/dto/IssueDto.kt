package com.magazines.catalog.data.remote.dto

data class IssueDto(
    val id: String,
    val magazineId: String,
    val issueNumber: Int,
    val publicationDate: String,
    val pdfUrl: String,
    val pagesCount: Int?,
    val createdAt: String,
)
