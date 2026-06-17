package com.example.data

import kotlinx.coroutines.flow.Flow

class WallpaperRepository(private val wallpaperDao: WallpaperDao) {
    val allWallpapers: Flow<List<WallpaperRecord>> = wallpaperDao.getAllWallpapers()
    val favoriteWallpapers: Flow<List<WallpaperRecord>> = wallpaperDao.getFavorites()

    suspend fun getWallpaperById(id: String): WallpaperRecord? {
        return wallpaperDao.getWallpaperById(id)
    }

    suspend fun insertWallpaper(wallpaper: WallpaperRecord) {
        wallpaperDao.insertWallpaper(wallpaper)
    }

    suspend fun deleteWallpaper(wallpaper: WallpaperRecord) {
        wallpaperDao.deleteWallpaper(wallpaper)
    }

    suspend fun updateFavoriteStatus(id: String, isFav: Boolean) {
        wallpaperDao.updateFavoriteStatus(id, isFav)
    }

    suspend fun updateDownloadedPath(id: String, path: String?) {
        wallpaperDao.updateDownloadedPath(id, path)
    }
}
