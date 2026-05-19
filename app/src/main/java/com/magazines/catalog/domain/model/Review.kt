package com.magazines.catalog.domain.model

data class Review(
    val id: String,
    val magazineId: String,
    val userId: String,
    val userName: String,
    val rating: Int,
    val comment: String?,
    val createdAt: String,
)
