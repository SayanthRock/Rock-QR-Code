package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "qr_records")
data class QrRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "SCAN" or "GENERATE"
    val format: String, // "TEXT", "URL", "WIFI", "PHONE", "EMAIL"
    val content: String,
    val title: String, // Descriptive name
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val customColorHex: String? = null
)
