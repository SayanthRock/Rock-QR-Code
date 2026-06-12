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
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
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
                text.startsWith("smsto:", ignoreCase = true) || text.startsWith("sms:", ignoreCase = true) -> "SMS"
                text.startsWith("upi://", ignoreCase = true) -> "UPI"
                text.contains("BEGIN:VCARD", ignoreCase = true) -> "CONTACT"
                else -> "TEXT"
            }
            
            // Extract a neat preview title
            val title = when (format) {
                "URL" -> text.substringAfter("://").substringBefore("/")
                "WIFI" -> "Wi-Fi: " + text.substringAfter("S:").substringBefore(";")
                "PHONE" -> "Call: " + text.substringAfter("tel:")
                "EMAIL" -> "Email " + text.substringAfter("mailto:").substringBefore("?")
                "SMS" -> "SMS: " + text.substringAfter("SMSTO:").substringBefore(":")
                "UPI" -> {
                    val payee = text.substringAfter("pa=").substringBefore("&")
                    "UPI Payee: $payee"
                }
                "CONTACT" -> {
                    val contactName = text.substringAfter("N:").substringBefore("\n")
                    "Contact: $contactName"
                }
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

    fun setScanning(enabled: Boolean) {
        _isScanning.value = enabled
    }

    // Manual/Mock Scan (crucial for streaming android environment testing)
    fun triggerManualScanText(text: String) {
        if (text.isNotBlank()) {
            setScannedText(text.trim())
        }
    }


    // ------------------ GENERATION LAYER ------------------
    val genContentRaw = MutableStateFlow("")
    val genFormat = MutableStateFlow("TEXT") // TEXT, URL, WIFI, PHONE, EMAIL, SMS, UPI, CONTACT
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

    // SMS helper parameters
    val smsPhone = MutableStateFlow("")
    val smsBody = MutableStateFlow("")

    // UPI helper parameters
    val upiVpa = MutableStateFlow("")
    val upiName = MutableStateFlow("")
    val upiAmount = MutableStateFlow("")

    // CONTACT (vCard) helper parameters
    val contactName = MutableStateFlow("")
    val contactPhone = MutableStateFlow("")
    val contactEmail = MutableStateFlow("")
    val contactOrg = MutableStateFlow("")

    // Logo embed style (NONE, CRYSTAL, SPARK, DIAMOND)
    val embedLogo = MutableStateFlow("NONE")

    // Error Correction Level (L, M, Q, H)
    val errorCorrectionLevel = MutableStateFlow(ErrorCorrectionLevel.H)

    // URL validation error stream
    val urlValidationError: StateFlow<String?> = urlLink.map { link ->
        val trimmed = link.trim()
        if (trimmed.isEmpty()) {
            null
        } else {
            val cleanLink = if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
                trimmed
            } else {
                "https://$trimmed"
            }
            if (!isValidUrlFormat(cleanLink)) {
                "Invalid URL format. Please format like: example.com"
            } else {
                null
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun isValidUrlFormat(url: String): Boolean {
        // Broad yet accurate regex matching valid domain and protocol names
        val rawPattern = "^(https?://)?([a-zA-Z0-9]([a-zA-Z0-9\\-]*[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,}(:\\d+)?(/.*)?$"
        val regex = rawPattern.toRegex(RegexOption.IGNORE_CASE)
        val isStandardUrl = url.matches(regex)
        val isLocalhost = url.startsWith("http://localhost", ignoreCase = true) || 
                          url.startsWith("https://localhost", ignoreCase = true) || 
                          url.equals("localhost", ignoreCase = true) || 
                          url.startsWith("localhost:", ignoreCase = true)
        return isStandardUrl || isLocalhost
    }

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
            urlLink, plainText,
            smsPhone, smsBody,
            upiVpa, upiName, upiAmount,
            contactName, contactPhone, contactEmail, contactOrg,
            embedLogo, errorCorrectionLevel
        )

        viewModelScope.launch {
            combine(flowsToObserve) { _ ->
                val content = getFormattedContent()
                val isUrlFormat = genFormat.value == "URL"
                val isValidUrl = !isUrlFormat || content.isEmpty() || isValidUrlFormat(content)
                if (content.isNotEmpty() && isValidUrl) {
                    QrCodeGenerator.generateQrCode(
                        content = content,
                        foregroundHexColor = genFgColor.value,
                        backgroundHexColor = genBgColor.value,
                        style = genStyle.value,
                        embedLogo = embedLogo.value,
                        errorCorrection = errorCorrectionLevel.value
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
            "SMS" -> {
                val phone = smsPhone.value.trim()
                val body = smsBody.value.trim()
                if (phone.isEmpty()) "" else "SMTO:$phone:$body"
            }
            "UPI" -> {
                val vpa = upiVpa.value.trim()
                val name = upiName.value.trim()
                val amount = upiAmount.value.trim()
                if (vpa.isEmpty()) "" 
                else {
                    var uri = "upi://pay?pa=$vpa"
                    if (name.isNotEmpty()) uri += "&pn=${android.net.Uri.encode(name)}"
                    if (amount.isNotEmpty()) uri += "&am=${android.net.Uri.encode(amount)}"
                    uri
                }
            }
            "CONTACT" -> {
                val name = contactName.value.trim()
                val phone = contactPhone.value.trim()
                val email = contactEmail.value.trim()
                val org = contactOrg.value.trim()
                if (name.isEmpty() && phone.isEmpty()) ""
                else {
                    "BEGIN:VCARD\nVERSION:3.0\nN:$name\nTEL:$phone" +
                            (if (email.isNotEmpty()) "\nEMAIL:$email" else "") +
                            (if (org.isNotEmpty()) "\nORG:$org" else "") +
                            "\nEND:VCARD"
                }
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
                "SMS" -> "SMS to " + smsPhone.value
                "UPI" -> "UPI Payee: " + (if (upiName.value.isNotEmpty()) upiName.value else upiVpa.value)
                "CONTACT" -> "Contact: " + contactName.value
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
    val colorPreset = MutableStateFlow(prefs.getString("color_preset", "MIDNIGHT") ?: "MIDNIGHT")

    // Dynamic 'Blur UI' glassmorphism settings
    val glassBlurRadius = MutableStateFlow(prefs.getFloat("glass_blur_radius", 16f))
    val glassOpacity = MutableStateFlow(prefs.getFloat("glass_opacity", 0.28f))
    val glassBorderThickness = MutableStateFlow(prefs.getFloat("glass_border_thickness", 1.5f))
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

    // ------------------ CUSTOM TOAST SYSTEM ------------------
    private val _toastEvent = MutableStateFlow<CustomToastMessage?>(null)
    val toastEvent: StateFlow<CustomToastMessage?> = _toastEvent.asStateFlow()

    fun showToast(message: String, type: CustomToastType = CustomToastType.INFO, durationMs: Long = 2500L) {
        _toastEvent.value = CustomToastMessage(message, type, durationMs, System.currentTimeMillis())
    }

    fun clearToast() {
        _toastEvent.value = null
    }
}

enum class CustomToastType {
    SUCCESS, INFO, ERROR, WARNING
}

data class CustomToastMessage(
    val message: String,
    val type: CustomToastType = CustomToastType.INFO,
    val durationMs: Long = 2500L,
    val id: Long = System.currentTimeMillis()
)

