package com.magazines.catalog.data.remote.dto

data class PagedResponse<T>(
    val items: List<T>,
    val page: Int,
    val pageSize: Int,
    val totalItems: Int,
    val totalPages: Int,
)
