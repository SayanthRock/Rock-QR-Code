package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface QrDao {
    @Query("SELECT * FROM qr_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<QrRecord>>

    @Query("SELECT * FROM qr_history WHERE isScanned = 1 ORDER BY timestamp DESC")
    fun getScannedHistory(): Flow<List<QrRecord>>

    @Query("SELECT * FROM qr_history WHERE isScanned = 0 ORDER BY timestamp DESC")
    fun getGeneratedHistory(): Flow<List<QrRecord>>

    @Query("SELECT * FROM qr_history WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavorites(): Flow<List<QrRecord>>

    @Query("SELECT * FROM qr_history WHERE id = :id LIMIT 1")
    suspend fun getRecordById(id: Int): QrRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: QrRecord): Long

    @Update
    suspend fun updateRecord(record: QrRecord)

    @Delete
    suspend fun deleteRecord(record: QrRecord)

    @Query("DELETE FROM qr_history WHERE id = :id")
    suspend fun deleteRecordById(id: Int)

    @Query("DELETE FROM qr_history")
    suspend fun clearAllHistory()
}
