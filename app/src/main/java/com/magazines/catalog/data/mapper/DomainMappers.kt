package com.magazines.catalog.data.mapper

import com.magazines.catalog.data.remote.CoverUrlResolver
import com.magazines.catalog.data.remote.api.CategoryDto
import com.magazines.catalog.data.remote.dto.IssueDto
import com.magazines.catalog.data.remote.dto.MagazineDto
import com.magazines.catalog.data.remote.dto.PagedResponse
import com.magazines.catalog.data.remote.dto.ReviewDto
import com.magazines.catalog.data.remote.dto.UserDto
import com.magazines.catalog.domain.model.Category
import com.magazines.catalog.domain.model.PagedData
import com.magazines.catalog.domain.model.Issue
import com.magazines.catalog.domain.model.Magazine
import com.magazines.catalog.domain.model.MagazineStatus
import com.magazines.catalog.domain.model.Review
import com.magazines.catalog.domain.model.User
import com.magazines.catalog.domain.model.UserRole

fun CategoryDto.toDomain(): Category = Category(
    id = id,
    name = name,
    description = description,
)

fun <T, R> PagedResponse<T>.toDomain(mapper: (T) -> R): PagedData<R> = PagedData(
    items = items.map(mapper),
    page = page,
    pageSize = pageSize,
    totalItems = totalItems,
    totalPages = totalPages,
)

fun UserDto.toDomain(): User = User(
    id = id,
    email = email,
    displayName = displayName,
    role = UserRole.valueOf(role.uppercase()),
    createdAt = createdAt,
)

fun MagazineDto.toDomain(): Magazine = Magazine(
    id = id,
    title = title,
    publisher = publisher,
    yearFounded = yearFounded,
    categoryId = categoryId,
    categoryName = categoryName,
    description = description,
    coverUrl = CoverUrlResolver.resolve(coverUrl),
    uploadedBy = uploadedBy,
    status = MagazineStatus.valueOf(status.uppercase()),
    averageRating = averageRating,
    reviewsCount = reviewsCount,
    issuesCount = issuesCount,
    createdAt = createdAt,
)

fun IssueDto.toDomain(): Issue = Issue(
    id = id,
    magazineId = magazineId,
    issueNumber = issueNumber,
    publicationDate = publicationDate,
    pdfUrl = CoverUrlResolver.resolve(pdfUrl, logTag = "Issues") ?: pdfUrl,
    pagesCount = pagesCount,
    createdAt = createdAt,
)

fun ReviewDto.toDomain(): Review = Review(
    id = id,
    magazineId = magazineId,
    userId = userId,
    userName = userName,
    rating = rating,
    comment = comment,
    createdAt = createdAt,
)
