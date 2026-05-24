package com.magazines.catalog.data.mapper

import com.magazines.catalog.data.local.db.FavoriteEntity
import com.magazines.catalog.domain.model.Magazine
import com.magazines.catalog.domain.model.MagazineStatus

fun FavoriteEntity.toDomain(): Magazine = Magazine(
    id = id,
    title = title,
    publisher = publisher.orEmpty(),
    yearFounded = 0,
    categoryId = "",
    categoryName = "",
    description = null,
    coverUrl = coverUrl,
    uploadedBy = "",
    status = MagazineStatus.APPROVED,
    averageRating = averageRating,
    reviewsCount = 0,
    issuesCount = 0,
    createdAt = "",
)

fun Magazine.toFavoriteEntity(cachedAt: Long = System.currentTimeMillis()): FavoriteEntity =
    FavoriteEntity(
        id = id,
        title = title,
        coverUrl = coverUrl,
        publisher = publisher,
        averageRating = averageRating,
        cachedAt = cachedAt,
    )
