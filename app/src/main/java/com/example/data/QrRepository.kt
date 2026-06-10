package com.example.data

import kotlinx.coroutines.flow.Flow

class QrRepository(private val qrRecordDao: QrRecordDao) {
    val allRecords: Flow<List<QrRecord>> = qrRecordDao.getAllRecords()

    suspend fun insertRecord(record: QrRecord): Long {
        return qrRecordDao.insertRecord(record)
    }

    suspend fun updateRecord(record: QrRecord) {
        qrRecordDao.updateRecord(record)
    }

    suspend fun deleteRecordById(id: Int) {
        qrRecordDao.deleteRecordById(id)
    }

    suspend fun deleteAllRecords() {
        qrRecordDao.deleteAllRecords()
    }

    suspend fun updateFavoriteStatus(id: Int, isFav: Boolean) {
        qrRecordDao.updateFavoriteStatus(id, isFav)
    }
}
