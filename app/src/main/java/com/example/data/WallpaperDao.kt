package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WallpaperDao {
    @Query("SELECT * FROM wallpapers ORDER BY timestamp DESC")
    fun getAllWallpapers(): Flow<List<WallpaperRecord>>

    @Query("SELECT * FROM wallpapers WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavorites(): Flow<List<WallpaperRecord>>

    @Query("SELECT * FROM wallpapers WHERE id = :id LIMIT 1")
    suspend fun getWallpaperById(id: String): WallpaperRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallpaper(wallpaper: WallpaperRecord)

    @Delete
    suspend fun deleteWallpaper(wallpaper: WallpaperRecord)

    @Query("UPDATE wallpapers SET isFavorite = :isFav WHERE id = :id")
    suspend fun updateFavoriteStatus(id: String, isFav: Boolean)

    @Query("UPDATE wallpapers SET downloadedPath = :path WHERE id = :id")
    suspend fun updateDownloadedPath(id: String, path: String?)
}
