package com.magazines.catalog.domain.model

data class Magazine(
    val id: String,
    val title: String,
    val publisher: String,
    val yearFounded: Int,
    val categoryId: String,
    val categoryName: String,
    val description: String?,
    val coverUrl: String?,
    val uploadedBy: String,
    val status: MagazineStatus,
    val averageRating: Double,
    val reviewsCount: Int,
    val issuesCount: Int,
    val createdAt: String,
    val rejectionReason: String? = null,
)
