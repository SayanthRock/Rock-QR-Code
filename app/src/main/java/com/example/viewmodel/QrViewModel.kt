package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.QrDatabase
import com.example.data.QrRecord
import com.example.data.QrRepository
import com.example.utils.QrCodeGenerator
import com.example.utils.QrStyle
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class QrViewModel(application: Application) : AndroidViewModel(application) {

    private val database = QrDatabase.getDatabase(application)
    private val repository = QrRepository(database.qrRecordDao())

    // UI tab select: "SCAN", "GENERATE", "HISTORY"
    private val _activeTab = MutableStateFlow("SCAN")
    val activeTab: StateFlow<String> = _activeTab.asStateFlow()

    fun selectTab(tab: String) {
        _activeTab.value = tab
    }

    // ------------------ SCAN LAYER ------------------
    private val _scannedText = MutableStateFlow<String?>(null)
    val scannedText: StateFlow<String?> = _scannedText.asStateFlow()

    private val _isScanning = MutableStateFlow(true)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    fun setScannedText(text: String) {
        if (_scannedText.value == text) return // debounce duplicates
        _scannedText.value = text
        _isScanning.value = false

        // Automatically save scanned record to local database
        viewModelScope.launch {
            val format = when {
                text.startsWith("http://", ignoreCase = true) || text.startsWith("https://", ignoreCase = true) -> "URL"
                text.startsWith("WIFI:", ignoreCase = true) -> "WIFI"
                text.startsWith("tel:", ignoreCase = true) -> "PHONE"
                text.startsWith("mailto:", ignoreCase = true) -> "EMAIL"
                else -> "TEXT"
            }
            
            // Extract a neat preview title
            val title = when (format) {
                "URL" -> text.substringAfter("://").substringBefore("/")
                "WIFI" -> "Wi-Fi: " + text.substringAfter("S:").substringBefore(";")
                "PHONE" -> "Call: " + text.substringAfter("tel:")
                "EMAIL" -> "Email " + text.substringAfter("mailto:").substringBefore("?")
                else -> if (text.length > 25) text.take(22) + "..." else text
            }

            repository.insertRecord(
                QrRecord(
                    type = "SCAN",
                    format = format,
                    content = text,
                    title = title
                )
            )
        }
    }

    fun resetScanner() {
        _scannedText.value = null
        _isScanning.value = true
    }

    // Manual/Mock Scan (crucial for streaming android environment testing)
    fun triggerManualScanText(text: String) {
        if (text.isNotBlank()) {
            setScannedText(text.trim())
        }
    }


    // ------------------ GENERATION LAYER ------------------
    val genContentRaw = MutableStateFlow("")
    val genFormat = MutableStateFlow("TEXT") // TEXT, URL, WIFI, PHONE, EMAIL
    val genStyle = MutableStateFlow(QrStyle.CLASSIC)
    val genFgColor = MutableStateFlow("#0A0A0A") // Default modern black
    val genBgColor = MutableStateFlow("#FFFFFF") // Default white

    // WIFI builder helper parameters
    val wifiSsid = MutableStateFlow("")
    val wifiPassword = MutableStateFlow("")
    val wifiSecurity = MutableStateFlow("WPA") // WPA, WEP, nopass

    // PHONE builder parameter
    val phoneNum = MutableStateFlow("")

    // EMAIL builder helper parameters
    val emailRecipient = MutableStateFlow("")
    val emailSubject = MutableStateFlow("")
    val emailBody = MutableStateFlow("")

    // URL helper parameter
    val urlLink = MutableStateFlow("")

    // Plain text content
    val plainText = MutableStateFlow("")

    private val _generatedBitmap = MutableStateFlow<Bitmap?>(null)
    val generatedBitmap: StateFlow<Bitmap?> = _generatedBitmap.asStateFlow()

    // Observe changes across generator state and trigger render automatically
    init {
        // Collect generation options changes reactively to sync bitmap preview
        val flowsToObserve = listOf<Flow<Any>>(
            genFormat, genStyle, genFgColor, genBgColor,
            wifiSsid, wifiPassword, wifiSecurity,
            phoneNum,
            emailRecipient, emailSubject, emailBody,
            urlLink, plainText
        )

        viewModelScope.launch {
            combine(flowsToObserve) { _ ->
                val content = getFormattedContent()
                if (content.isNotEmpty()) {
                    QrCodeGenerator.generateQrCode(
                        content = content,
                        foregroundHexColor = genFgColor.value,
                        backgroundHexColor = genBgColor.value,
                        style = genStyle.value
                    )
                } else {
                    null
                }
            }.collect { bitmap ->
                _generatedBitmap.value = bitmap
            }
        }
    }

    fun getFormattedContent(): String {
        return when (genFormat.value) {
            "URL" -> {
                val link = urlLink.value.trim()
                if (link.isEmpty()) ""
                else if (link.startsWith("http://", ignoreCase = true) || link.startsWith("https://", ignoreCase = true)) link
                else "https://$link"
            }
            "WIFI" -> {
                val ssid = wifiSsid.value.replace(";", "\\;").replace(":", "\\:")
                val pass = wifiPassword.value.replace(";", "\\;").replace(":", "\\:")
                if (ssid.isEmpty()) "" 
                else "WIFI:S:$ssid;T:${wifiSecurity.value};P:$pass;;"
            }
            "PHONE" -> {
                val number = phoneNum.value.trim()
                if (number.isEmpty()) "" else "tel:$number"
            }
            "EMAIL" -> {
                val recipient = emailRecipient.value.trim()
                val subject = emailSubject.value.trim()
                val body = emailBody.value.trim()
                if (recipient.isEmpty()) ""
                else "mailto:$recipient?subject=${android.net.Uri.encode(subject)}&body=${android.net.Uri.encode(body)}"
            }
            else -> plainText.value
        }
    }

    fun saveGeneratedCodeInHistory() {
        val finalContent = getFormattedContent()
        if (finalContent.isEmpty()) return

        viewModelScope.launch {
            val format = genFormat.value
            val title = when (format) {
                "URL" -> urlLink.value.trim().substringBefore("/")
                "WIFI" -> "Wi-Fi: " + wifiSsid.value
                "PHONE" -> "Phone: " + phoneNum.value
                "EMAIL" -> "Email: " + emailRecipient.value
                else -> if (plainText.value.length > 25) plainText.value.take(22) + "..." else plainText.value
            }

            repository.insertRecord(
                QrRecord(
                    type = "GENERATE",
                    format = format,
                    content = finalContent,
                    title = title,
                    customColorHex = "${genFgColor.value}|${genBgColor.value}"
                )
            )
        }
    }

    // ------------------ HISTORY & PERSISTENCE ------------------
    val searchQuery = MutableStateFlow("")
    val isOnlyFavorites = MutableStateFlow(false)

    val historyRecords: StateFlow<List<QrRecord>> = combine(
        repository.allRecords,
        searchQuery,
        isOnlyFavorites
    ) { records, query, favOnly ->
        records.filter { record ->
            // Search filter keyword
            val matchQuery = query.isEmpty() || 
                             record.content.contains(query, ignoreCase = true) || 
                             record.title.contains(query, ignoreCase = true)
            // Favorite filter matches
            val matchFav = !favOnly || record.isFavorite
            
            matchQuery && matchFav
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun toggleFavorite(record: QrRecord) {
        viewModelScope.launch {
            repository.updateFavoriteStatus(record.id, !record.isFavorite)
        }
    }

    fun deleteRecord(record: QrRecord) {
        viewModelScope.launch {
            repository.deleteRecordById(record.id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.deleteAllRecords()
        }
    }

    // ------------------ SETTINGS & THEMING PERSISTENCE ------------------
    private val prefs = application.getSharedPreferences("rock_qr_settings", android.content.Context.MODE_PRIVATE)

    val themeMode = MutableStateFlow(prefs.getString("theme_mode", "SYSTEM") ?: "SYSTEM")
    val dynamicColorEnabled = MutableStateFlow(prefs.getBoolean("dynamic_color", false))

    fun setThemeMode(mode: String) {
        themeMode.value = mode
        prefs.edit().putString("theme_mode", mode).apply()
    }

    fun setDynamicColorEnabled(enabled: Boolean) {
        dynamicColorEnabled.value = enabled
        prefs.edit().putBoolean("dynamic_color", enabled).apply()
    }
}
