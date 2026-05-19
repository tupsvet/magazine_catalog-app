package com.magazines.catalog.data.remote.dto

data class ReviewDto(
    val id: String,
    val magazineId: String,
    val userId: String,
    val userName: String,
    val rating: Int,
    val comment: String?,
    val createdAt: String,
)
