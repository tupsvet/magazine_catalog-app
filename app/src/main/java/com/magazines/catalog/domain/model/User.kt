package com.magazines.catalog.domain.model

data class User(
    val id: String,
    val email: String,
    val displayName: String?,
    val role: UserRole,
    val createdAt: String,
)
