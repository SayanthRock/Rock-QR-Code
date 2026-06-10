package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface QrRecordDao {
    @Query("SELECT * FROM qr_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<QrRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: QrRecord): Long

    @Update
    suspend fun updateRecord(record: QrRecord)

    @Query("DELETE FROM qr_records WHERE id = :id")
    suspend fun deleteRecordById(id: Int)

    @Query("DELETE FROM qr_records")
    suspend fun deleteAllRecords()

    @Query("UPDATE qr_records SET isFavorite = :isFav WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Int, isFav: Boolean)
}
