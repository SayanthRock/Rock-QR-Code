package com.example.data

import kotlinx.coroutines.flow.Flow

class QrRepository(private val qrDao: QrDao) {
    val allHistory: Flow<List<QrRecord>> = qrDao.getAllHistory()
    val scannedHistory: Flow<List<QrRecord>> = qrDao.getScannedHistory()
    val generatedHistory: Flow<List<QrRecord>> = qrDao.getGeneratedHistory()
    val favorites: Flow<List<QrRecord>> = qrDao.getFavorites()

    suspend fun getRecordById(id: Int): QrRecord? {
        return qrDao.getRecordById(id)
    }

    suspend fun insertRecord(record: QrRecord): Long {
        return qrDao.insertRecord(record)
    }

    suspend fun updateRecord(record: QrRecord) {
        qrDao.updateRecord(record)
    }

    suspend fun deleteRecord(record: QrRecord) {
        qrDao.deleteRecord(record)
    }

    suspend fun deleteRecordById(id: Int) {
        qrDao.deleteRecordById(id)
    }

    suspend fun clearAllHistory() {
        qrDao.clearAllHistory()
    }
}
