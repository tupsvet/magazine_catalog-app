package com.magazines.catalog.domain.model

data class Issue(
    val id: String,
    val magazineId: String,
    val issueNumber: String,
    val publicationDate: String?,
    val pdfUrl: String,
    val pagesCount: Int?,
    val createdAt: String,
)
