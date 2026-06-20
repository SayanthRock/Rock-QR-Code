package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.QrDatabase
import com.example.data.QrRecord
import com.example.data.QrRepository
import com.example.utils.QRGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class CustomToastType {
    SUCCESS, INFO, ERROR, WARNING
}

data class CustomToastMessage(
    val message: String,
    val type: CustomToastType = CustomToastType.INFO,
    val durationMs: Long = 2500L,
    val id: Long = System.currentTimeMillis()
)

data class QrColorOption(
    val name: String,
    var primaryColor: Int,
    val secondaryColor: Int = Color.WHITE
)

class QRViewModel(application: Application) : AndroidViewModel(application) {

    private val database = QrDatabase.getDatabase(application)
    private val repository = QrRepository(database.qrDao())
    private val prefs = application.getSharedPreferences("rock_qr_settings", Context.MODE_PRIVATE)

    // Current screen: "SCAN", "GENERATE", "HISTORY", "SETTINGS"
    private val _activeTab = MutableStateFlow("SCAN")
    val activeTab: StateFlow<String> = _activeTab.asStateFlow()

    fun selectTab(tab: String) {
        _activeTab.value = tab
    }

    // Generator inputs
    val inputText = MutableStateFlow("")
    val inputTitle = MutableStateFlow("")
    val inputType = MutableStateFlow("TEXT") // "TEXT", "URL", "WIFI", "CONTACT", "EMAIL", "PHONE"

    // WiFi Specific Inputs
    val wifiSsid = MutableStateFlow("")
    val wifiPassword = MutableStateFlow("")
    val wifiSecurity = MutableStateFlow("WPA") // "WPA", "WEP", "nopass"

    // Contact Specific Inputs
    val contactName = MutableStateFlow("")
    val contactPhone = MutableStateFlow("")
    val contactEmail = MutableStateFlow("")

    // QR Color Selection
    val colorOptions = listOf(
        QrColorOption("Obsidian", Color.parseColor("#111111")),
        QrColorOption("Amethyst", Color.parseColor("#9D4EED")),
        QrColorOption("Emerald", Color.parseColor("#06D6A0")),
        QrColorOption("Amber", Color.parseColor("#FFB703")),
        QrColorOption("Ruby", Color.parseColor("#FF4D4D")),
        QrColorOption("Sapphire", Color.parseColor("#2196F3"))
    )
    val selectedQrColor = MutableStateFlow(colorOptions[0])

    // Current Generating/Generated QR Result Bitmap
    private val _generatedQrBitmap = MutableStateFlow<Bitmap?>(null)
    val generatedQrBitmap: StateFlow<Bitmap?> = _generatedQrBitmap.asStateFlow()

    // Flag stating whether generator has results on display
    val hasGeneratedResult = MutableStateFlow(false)

    // History flows
    val searchQuery = MutableStateFlow("")
    val historyFilterType = MutableStateFlow("ALL") // "ALL", "SCANNED", "GENERATED", "FAVORITE"

    val historyRecords: StateFlow<List<QrRecord>> = combine(
        repository.allHistory,
        searchQuery,
        historyFilterType
    ) { all, query, filter ->
        all.filter { record ->
            val matchesFilter = when (filter) {
                "SCANNED" -> record.isScanned
                "GENERATED" -> !record.isScanned
                "FAVORITE" -> record.isFavorite
                else -> true
            }
            val matchesQuery = query.isEmpty() ||
                    record.title.contains(query, ignoreCase = true) ||
                    record.content.contains(query, ignoreCase = true) ||
                    record.type.contains(query, ignoreCase = true)

            matchesFilter && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun generateQR() {
        val payload: String
        val title: String

        when (inputType.value) {
            "URL" -> {
                val inputUrl = inputText.value.trim()
                if (inputUrl.isEmpty()) {
                    showToast("Please enter a URL to generate code.", CustomToastType.WARNING)
                    return
                }
                // Prepend protocol if missing
                payload = if (!inputUrl.startsWith("http://") && !inputUrl.startsWith("https://")) {
                    "https://$inputUrl"
                } else inputUrl

                title = inputTitle.value.trim().ifEmpty { "URL Link" }
            }
            "WIFI" -> {
                val ssid = wifiSsid.value.trim()
                val password = wifiPassword.value
                val sec = wifiSecurity.value
                if (ssid.isEmpty()) {
                    showToast("Please enter WiFi SSID Name.", CustomToastType.WARNING)
                    return
                }
                // WIFI:S:MySsid;T:WPA;P:MyPassword;;
                payload = "WIFI:S:$ssid;T:$sec;P:$password;;"
                title = inputTitle.value.trim().ifEmpty { "WiFi: $ssid" }
            }
            "CONTACT" -> {
                val name = contactName.value.trim()
                val phone = contactPhone.value.trim()
                val email = contactEmail.value.trim()
                if (name.isEmpty() && phone.isEmpty()) {
                    showToast("Please enter at least Name or Phone.", CustomToastType.WARNING)
                    return
                }
                // MECARD:N:Name;TEL:Phone;EMAIL:Email;;
                payload = "MECARD:N:$name;TEL:$phone;EMAIL:$email;;"
                title = inputTitle.value.trim().ifEmpty { "Contact: $name" }
            }
            else -> { // TEXT, EMAIL, PHONE, etc.
                val text = inputText.value.trim()
                if (text.isEmpty()) {
                    showToast("Please enter text or numeric digits.", CustomToastType.WARNING)
                    return
                }
                payload = text
                title = inputTitle.value.trim().ifEmpty {
                    if (text.length > 20) text.take(18) + "..." else text
                }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val colorOption = selectedQrColor.value
                val bitmap = QRGenerator.generate(
                    text = payload,
                    primaryColor = colorOption.primaryColor,
                    secondaryColor = colorOption.secondaryColor
                )

                _generatedQrBitmap.value = bitmap
                hasGeneratedResult.value = true

                // Save automatically to Room DB generated log
                val record = QrRecord(
                    content = payload,
                    title = title,
                    type = inputType.value,
                    isScanned = false,
                    colorHex = String.format("#%06X", 0xFFFFFF and colorOption.primaryColor)
                )
                repository.insertRecord(record)
                showToast("Premium QR Code Rendered Successfully!", CustomToastType.SUCCESS)
            } catch (e: Exception) {
                e.printStackTrace()
                showToast("Generation failed: ${e.localizedMessage}", CustomToastType.ERROR)
            }
        }
    }

    fun saveScannedResult(content: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val type = detectQrType(content)
            val title = when (type) {
                "URL" -> {
                    val label = content.removePrefix("https://").removePrefix("http://")
                    if (label.length > 25) label.take(22) + "..." else label
                }
                "WIFI" -> "WiFi Network Scan"
                "CONTACT" -> "Scanned Contact Details"
                else -> if (content.length > 25) content.take(22) + "..." else content
            }

            // Check if exact record exists to avoid duplication spam
            val currentRecords = repository.allHistory.first()
            val alreadyExists = currentRecords.any { it.content == content && it.isScanned }
            if (!alreadyExists) {
                val record = QrRecord(
                    content = content,
                    title = title,
                    type = type,
                    isScanned = true
                )
                repository.insertRecord(record)
            }
        }
    }

    private fun detectQrType(content: String): String {
        return when {
            content.startsWith("http://", ignoreCase = true) || content.startsWith("https://", ignoreCase = true) -> "URL"
            content.startsWith("WIFI:", ignoreCase = true) -> "WIFI"
            content.startsWith("MECARD:", ignoreCase = true) || content.startsWith("BEGIN:VCARD", ignoreCase = true) -> "CONTACT"
            content.contains("@") && (content.startsWith("mailto:", ignoreCase = true) || content.length < 50) -> "EMAIL"
            content.startsWith("tel:", ignoreCase = true) || (content.length in 7..15 && content.all { it.isDigit() || it == '+' || it == ' ' || it == '-' }) -> "PHONE"
            else -> "TEXT"
        }
    }

    fun importFromDeepLink(url: String) {
        try {
            val uri = Uri.parse(url)
            val content = uri.getQueryParameter("content") ?: uri.getQueryParameter("text")
            if (content != null) {
                val type = uri.getQueryParameter("type") ?: "TEXT"
                val title = uri.getQueryParameter("title") ?: "Imported Core"

                inputType.value = when (type.uppercase(java.util.Locale.ROOT)) {
                    "URL" -> "URL"
                    "WIFI" -> "WIFI"
                    "CONTACT" -> "CONTACT"
                    else -> "TEXT"
                }

                if (inputType.value == "WIFI") {
                    val ssid = uri.getQueryParameter("ssid") ?: ""
                    val pass = uri.getQueryParameter("pass") ?: uri.getQueryParameter("password") ?: ""
                    val sec = uri.getQueryParameter("sec") ?: uri.getQueryParameter("security") ?: "WPA"

                    wifiSsid.value = ssid
                    wifiPassword.value = pass
                    wifiSecurity.value = sec
                } else if (inputType.value == "CONTACT") {
                    val name = uri.getQueryParameter("name") ?: ""
                    val phone = uri.getQueryParameter("phone") ?: ""
                    val email = uri.getQueryParameter("email") ?: ""

                    contactName.value = name
                    contactPhone.value = phone
                    contactEmail.emailAddressWorkaround(email) // Just local state backup
                    contactEmail.value = email
                } else {
                    inputText.value = content
                }

                inputTitle.value = title
                selectTab("GENERATE")
                generateQR()
                showToast("Imported QR Core from Web Link!", CustomToastType.SUCCESS)
            } else {
                // If there's no content parameter, check if we can interpret the whole path or general string
                val decodedQuery = uri.lastPathSegment
                if (!decodedQuery.isNullOrBlank() && decodedQuery != "Rock-QR-Code") {
                    inputText.value = decodedQuery
                    inputType.value = detectQrType(decodedQuery)
                    inputTitle.value = "Web Import"
                    selectTab("GENERATE")
                    generateQR()
                    showToast("Imported Segment from Web Link!", CustomToastType.SUCCESS)
                }
            }
        } catch (e: Exception) {
            showToast("Failed to parse link: ${e.localizedMessage}", CustomToastType.ERROR)
        }
    }

    // Workaround helper to satisfy any local state references cleanly
    private fun MutableStateFlow<String>.emailAddressWorkaround(value: String) {
        this.value = value
    }

    fun toggleFavorite(record: QrRecord) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = record.copy(isFavorite = !record.isFavorite)
            repository.updateRecord(updated)
            if (updated.isFavorite) {
                showToast("Saved to Bookmarked History", CustomToastType.SUCCESS)
            } else {
                showToast("Removed bookmark", CustomToastType.INFO)
            }
        }
    }

    fun deleteRecord(record: QrRecord) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteRecord(record)
            showToast("Record removed from log", CustomToastType.INFO)
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllHistory()
            showToast("Database and all scans cleared offline", CustomToastType.SUCCESS)
        }
    }

    fun resetGenerator() {
        inputText.value = ""
        inputTitle.value = ""
        wifiSsid.value = ""
        wifiPassword.value = ""
        contactName.value = ""
        contactPhone.value = ""
        contactEmail.value = ""
        _generatedQrBitmap.value = null
        hasGeneratedResult.value = false
    }

    // ------------------ SETTINGS & THEME CONTROLS ------------------
    val themeMode = MutableStateFlow(prefs.getString("theme_mode", "DARK") ?: "DARK")
    val dynamicColorEnabled = MutableStateFlow(prefs.getBoolean("dynamic_color", false))
    val colorPreset = MutableStateFlow(prefs.getString("color_preset", "MIDNIGHT") ?: "MIDNIGHT")

    // Liquid Glass settings
    val glassBlurRadius = MutableStateFlow(prefs.getFloat("glass_blur_radius", 18f))
    val glassBlurEnabled = MutableStateFlow(prefs.getBoolean("glass_blur_enabled", true))
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

    fun setGlassBlurEnabled(enabled: Boolean) {
        glassBlurEnabled.value = enabled
        prefs.edit().putBoolean("glass_blur_enabled", enabled).apply()
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
