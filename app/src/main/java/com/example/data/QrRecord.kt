package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "qr_history")
data class QrRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val content: String,
    val title: String,
    val type: String, // "TEXT", "URL", "WIFI", "CONTACT", "EMAIL", "PHONE"
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val isScanned: Boolean = false, // True if scanned, False if generated
    val colorHex: String? = null
) : Serializable
