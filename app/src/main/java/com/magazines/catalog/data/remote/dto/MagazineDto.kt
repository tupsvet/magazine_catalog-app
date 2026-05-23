package com.magazines.catalog.data.remote.dto

data class MagazineDto(
    val id: String,
    val title: String,
    val publisher: String,
    val yearFounded: Int,
    val categoryId: String,
    val categoryName: String,
    val description: String?,
    val coverUrl: String?,
    val uploadedBy: String,
    val status: String,
    val averageRating: Double,
    val reviewsCount: Int,
    val issuesCount: Int,
    val createdAt: String,
)

