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

data class CategoryDto(
    val id: String,
    val name: String,
    val description: String?,
)

data class PagedResponseDto<T>(
    val items: List<T>,
    val page: Int,
    val pageSize: Int,
    val totalItems: Int,
    val totalPages: Int,
)
