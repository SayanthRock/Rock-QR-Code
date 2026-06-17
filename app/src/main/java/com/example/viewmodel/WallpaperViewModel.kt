package com.example.viewmodel

import android.app.Application
import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.InputStream
import java.net.URL

enum class CustomToastType {
    SUCCESS, INFO, ERROR, WARNING
}

data class CustomToastMessage(
    val message: String,
    val type: CustomToastType = CustomToastType.INFO,
    val durationMs: Long = 2500L,
    val id: Long = System.currentTimeMillis()
)

data class WallpaperItem(
    val id: String,
    val title: String,
    val category: String,
    val url: String,
    val thumb: String,
    val tags: List<String>,
    val isPremium: Boolean,
    val isFavorite: Boolean = false
)

data class CategoryItem(
    val key: String,
    val name: String,
    val description: String,
    val count: Int,
    val coverUrl: String
)

class WallpaperViewModel(application: Application) : AndroidViewModel(application) {

    private val database = WallpaperDatabase.getDatabase(application)
    private val repository = WallpaperRepository(database.wallpaperDao())
    private val prefs = application.getSharedPreferences("rock_wallpaper_settings", Context.MODE_PRIVATE)

    // Current screen: "EXPLORE", "CATEGORIES", "FAVORITES", "SETTINGS"
    private val _activeTab = MutableStateFlow("EXPLORE")
    val activeTab: StateFlow<String> = _activeTab.asStateFlow()

    fun selectTab(tab: String) {
        _activeTab.value = tab
    }

    // Selected category for filtering
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    fun selectCategory(categoryKey: String?) {
        _selectedCategory.value = categoryKey
        if (categoryKey != null) {
            _activeTab.value = "EXPLORE" // Switch automatically to list view
        }
    }

    // Search query
    val searchQuery = MutableStateFlow("")

    // Raw catalog assets lists loaded from local JSON
    private val _rawCategories = MutableStateFlow<List<CategoryItem>>(emptyList())
    val categories: StateFlow<List<CategoryItem>> = _rawCategories.asStateFlow()

    private val _rawWallpapers = MutableStateFlow<List<WallpaperItem>>(emptyList())

    // Combined Flow: Merges local JSON raw wallpapers with local Room Database favorite states
    val wallpapers: StateFlow<List<WallpaperItem>> = combine(
        _rawWallpapers,
        repository.favoriteWallpapers,
        searchQuery,
        _selectedCategory
    ) { rawList, favorites, query, selectedCat ->
        val favoriteIds = favorites.map { it.id }.toSet()
        rawList.map { item ->
            item.copy(isFavorite = favoriteIds.contains(item.id))
        }.filter { item ->
            val matchesCategory = selectedCat == null || item.category.uppercase() == selectedCat.uppercase()
            val matchesSearch = query.isEmpty() || 
                                item.title.contains(query, ignoreCase = true) || 
                                item.tags.any { it.contains(query, ignoreCase = true) }
            matchesCategory && matchesSearch
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Local-only Database Favorites List
    val favoriteWallpapers: StateFlow<List<WallpaperRecord>> = repository.favoriteWallpapers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        loadMetadata()
    }

    private fun loadMetadata() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Read categories.json from Assets
                val categoriesJsonString = loadJsonFromAssets("metadata/categories.json")
                val categoriesArray = JSONArray(categoriesJsonString)
                val categoryList = mutableListOf<CategoryItem>()
                for (i in 0 until categoriesArray.length()) {
                    val obj = categoriesArray.getJSONObject(i)
                    categoryList.add(
                        CategoryItem(
                            key = obj.getString("key"),
                            name = obj.getString("name"),
                            description = obj.getString("description"),
                            count = obj.getInt("count"),
                            coverUrl = obj.getString("coverUrl")
                        )
                    )
                }
                _rawCategories.value = categoryList

                // Read wallpapers.json from Assets
                val wallpapersJsonString = loadJsonFromAssets("metadata/wallpapers.json")
                val wallpapersArray = JSONArray(wallpapersJsonString)
                val wallpapersList = mutableListOf<WallpaperItem>()
                for (i in 0 until wallpapersArray.length()) {
                    val obj = wallpapersArray.getJSONObject(i)
                    val tagsArray = obj.getJSONArray("tags")
                    val tagsList = mutableListOf<String>()
                    for (j in 0 until tagsArray.length()) {
                        tagsList.add(tagsArray.getString(j))
                    }
                    wallpapersList.add(
                        WallpaperItem(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            category = obj.getString("category"),
                            url = obj.getString("url"),
                            thumb = obj.getString("thumb"),
                            tags = tagsList,
                            isPremium = obj.getBoolean("isPremium")
                        )
                    )
                }
                _rawWallpapers.value = wallpapersList
            } catch (e: Exception) {
                e.printStackTrace()
                showToast("Failed to load catalogs metadata: ${e.localizedMessage}", CustomToastType.ERROR)
            }
        }
    }

    private fun loadJsonFromAssets(filePath: String): String {
        return getApplication<Application>().assets.open(filePath).use { stream ->
            stream.bufferedReader().use { it.readText() }
        }
    }

    // ------------------ FAVORITES HANDLER ------------------
    fun toggleFavorite(item: WallpaperItem) {
        viewModelScope.launch(Dispatchers.IO) {
            val isCurrentlyFav = item.isFavorite
            if (isCurrentlyFav) {
                // Retrieve from DB first
                val existing = repository.getWallpaperById(item.id)
                if (existing != null) {
                    repository.deleteWallpaper(existing)
                }
                showToast("Removed from favorites", CustomToastType.INFO)
            } else {
                repository.insertWallpaper(
                    WallpaperRecord(
                        id = item.id,
                        title = item.title,
                        category = item.category,
                        url = item.url,
                        thumbUrl = item.thumb,
                        tagsCsv = item.tags.joinToString(","),
                        isFavorite = true,
                        isPremium = item.isPremium
                    )
                )
                showToast("Added to favorites!", CustomToastType.SUCCESS)
            }
        }
    }

    // ------------------ WALLPAPER APPLICATION SERVICE ------------------
    private val _isApplyingWallpaper = MutableStateFlow(false)
    val isApplyingWallpaper: StateFlow<Boolean> = _isApplyingWallpaper.asStateFlow()

    fun applyWallpaper(imageUrl: String, target: String) {
        _isApplyingWallpaper.value = true
        viewModelScope.launch(Dispatchers.IO) {
            showToast("Downloading full quality image...", CustomToastType.INFO, 3000L)
            val bitmap = downloadBitmap(imageUrl)
            if (bitmap != null) {
                try {
                    val wm = WallpaperManager.getInstance(getApplication())
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        when (target) {
                            "HOMESCREEN" -> {
                                wm.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                                showToast("Home screen wallpaper updated!", CustomToastType.SUCCESS)
                            }
                            "LOCKSCREEN" -> {
                                wm.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                                showToast("Lock screen wallpaper updated!", CustomToastType.SUCCESS)
                            }
                            "BOTH" -> {
                                wm.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                                wm.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                                showToast("System wallpapers successfully updated!", CustomToastType.SUCCESS)
                            }
                        }
                    } else {
                        wm.setBitmap(bitmap)
                        showToast("Wallpaper applied successfully!", CustomToastType.SUCCESS)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    showToast("Failed to apply: ${e.localizedMessage}", CustomToastType.ERROR)
                }
            } else {
                showToast("Download failed. Check your internet connection.", CustomToastType.ERROR)
            }
            _isApplyingWallpaper.value = false
        }
    }

    private fun downloadBitmap(urlString: String): Bitmap? {
        return try {
            val url = URL(urlString)
            val connection = url.openConnection()
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.doInput = true
            connection.getInputStream().use { inputStream: InputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ------------------ SETTINGS & THEME CONTROLS ------------------
    val themeMode = MutableStateFlow(prefs.getString("theme_mode", "DARK") ?: "DARK")
    val dynamicColorEnabled = MutableStateFlow(prefs.getBoolean("dynamic_color", false))
    val colorPreset = MutableStateFlow(prefs.getString("color_preset", "MIDNIGHT") ?: "MIDNIGHT")

    // Liquid Glass settings
    val glassBlurRadius = MutableStateFlow(prefs.getFloat("glass_blur_radius", 18f))
    val glassOpacity = MutableStateFlow(prefs.getFloat("glass_opacity", 0.22f))
    val glassBorderThickness = MutableStateFlow(prefs.getFloat("glass_border_thickness", 1.2f))
    val glassGlowEnabled = MutableStateFlow(prefs.getBoolean("glass_glow_enabled", true))

    fun setThemeMode(mode: String) {
        themeMode.value = mode
        prefs.edit().putString("theme_mode", mode).apply()
    }

    fun setDynamicColorEnabled(enabled: Boolean) {
        dynamicColorEnabled.value = enabled
        prefs.edit().putBoolean("dynamic_color", enabled).apply()
    }

    fun setColorPreset(preset: String) {
        colorPreset.value = preset
        prefs.edit().putString("color_preset", preset).apply()
    }

    fun setGlassBlurRadius(value: Float) {
        glassBlurRadius.value = value
        prefs.edit().putFloat("glass_blur_radius", value).apply()
    }

    fun setGlassOpacity(value: Float) {
        glassOpacity.value = value
        prefs.edit().putFloat("glass_opacity", value).apply()
    }

    fun setGlassBorderThickness(value: Float) {
        glassBorderThickness.value = value
        prefs.edit().putFloat("glass_border_thickness", value).apply()
    }

    fun setGlassGlowEnabled(enabled: Boolean) {
        glassGlowEnabled.value = enabled
        prefs.edit().putBoolean("glass_glow_enabled", enabled).apply()
    }

    // ------------------ TOAST EVENT DISPATCHER ------------------
    private val _toastEvent = MutableStateFlow<CustomToastMessage?>(null)
    val toastEvent: StateFlow<CustomToastMessage?> = _toastEvent.asStateFlow()

    fun showToast(message: String, type: CustomToastType = CustomToastType.INFO, durationMs: Long = 2500L) {
        _toastEvent.value = CustomToastMessage(message, type, durationMs, System.currentTimeMillis())
    }

    fun clearToast() {
        _toastEvent.value = null
    }
}
