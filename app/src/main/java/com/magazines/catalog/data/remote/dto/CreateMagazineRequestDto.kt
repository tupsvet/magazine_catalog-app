package com.magazines.catalog.data.remote.dto

data class CreateMagazineRequestDto(
    val title: String,
    val publisher: String?,
    val yearFounded: Int?,
    val categoryId: Int,
    val description: String?,
)
