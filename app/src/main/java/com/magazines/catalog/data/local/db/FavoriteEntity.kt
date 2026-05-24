package com.magazines.catalog.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val coverUrl: String?,
    val publisher: String?,
    val averageRating: Double,
    val cachedAt: Long,
)
