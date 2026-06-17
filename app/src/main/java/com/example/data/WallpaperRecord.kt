package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallpapers")
data class WallpaperRecord(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val url: String,
    val thumbUrl: String,
    val tagsCsv: String,
    val isFavorite: Boolean = false,
    val downloadedPath: String? = null,
    val isPremium: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
