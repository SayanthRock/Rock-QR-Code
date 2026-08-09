package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LiquidGlassTheme
import com.example.utils.HapticUtils
import com.example.utils.QRGenerator
import com.example.utils.ShareUtils
import com.example.viewmodel.CustomToastType
import com.example.viewmodel.QRViewModel

@Composable
fun GenerateScreen(
    viewModel: QRViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val activePreset by viewModel.colorPreset.collectAsState()
    val themeConfig = LiquidGlassTheme.LocalConfig.current
    val primaryColor = themeConfig.primaryColor
    val secondaryColor = themeConfig.secondaryColor

    val inputType by viewModel.inputType.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val inputTitle by viewModel.inputTitle.collectAsState()

    // WiFi States
    val wifiSsid by viewModel.wifiSsid.collectAsState()
    val wifiPassword by viewModel.wifiPassword.collectAsState()
    val wifiSecurity by viewModel.wifiSecurity.collectAsState()

    // Contact States
    val contactName by viewModel.contactName.collectAsState()
    val contactPhone by viewModel.contactPhone.collectAsState()
    val contactEmail by viewModel.contactEmail.collectAsState()

    val maxCapacity = 1200
    val warningThreshold = 960
    val currentPayloadLength = remember(inputType, inputText, wifiSsid, wifiPassword, wifiSecurity, contactName, contactPhone, contactEmail) {
        when (inputType) {
            "URL" -> {
                val trimmed = inputText.trim()
                val payload = if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
                    "https://$trimmed"
                } else trimmed
                payload.length
            }
            "WIFI" -> {
                "WIFI:S:${wifiSsid.trim()};T:$wifiSecurity;P:$wifiPassword;;".length
            }
            "CONTACT" -> {
                "MECARD:N:${contactName.trim()};TEL:${contactPhone.trim()};EMAIL:${contactEmail.trim()};;".length
            }
            else -> {
                inputText.trim().length
            }
        }
    }
    val isOverCapacity = currentPayloadLength > maxCapacity
    val isNearCapacity = currentPayloadLength in warningThreshold..maxCapacity

    // Color options
    val selectedQrColor by viewModel.selectedQrColor.collectAsState()
    val qrBitmap by viewModel.generatedQrBitmap.collectAsState()
    val hasResult by viewModel.hasGeneratedResult.collectAsState()

    val selectedStyle by viewModel.selectedQrStyle.collectAsState()
    val selectedEyeColor by viewModel.selectedEyeColor.collectAsState()
    val selectedInnerEyeColor by viewModel.selectedInnerEyeColor.collectAsState()
    val isDynamic by viewModel.isDynamicQr.collectAsState()
    val isTransparentBg by viewModel.isTransparentBg.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 120.dp), // Leaves clearance for the floating navigation bar
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // TOP HEADER ROW WITH GEAR SETTINGS BUTTON
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Chamo QR",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    fontFamily = FontFamily.SansSerif,
                    modifier = Modifier.testTag("generate_title")
                )
                Text(
                    text = "Create and scan QR codes",
                    fontSize = 13.sp,
                    color = Color(0xFFA1A1AA),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF27272A))
                    .clickable {
                        HapticUtils.vibrate(context, 20)
                        viewModel.selectTab("SETTINGS")
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }


        // SLIDE ANIMATION ON DISPLAY STATE
        AnimatedContent(
            targetState = hasResult,
            transitionSpec = {
                slideInVertically { it } + fadeIn() togetherWith slideOutVertically { -it } + fadeOut()
            },
            label = "generator_flow"
        ) { isResultVisible ->
            if (isResultVisible && qrBitmap != null) {
                // RENDER GENERATED PREMIUM QR INSIDE AN ANCHORED ROCK MATTE CONTAINER
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f), RoundedCornerShape(24.dp))
                        .border(
                            1.dp,
                            Brush.linearGradient(
                                listOf(primaryColor.copy(alpha = 0.4f), MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                            ),
                            RoundedCornerShape(24.dp)
                        )
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Your Generated QR Code",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(18.dp))

                    // QR CONTAINER LAYER WITH BACKDROP
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .background(Color.White, RoundedCornerShape(16.dp))
                            .border(1.5.dp, primaryColor.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = qrBitmap!!.asImageBitmap(),
                            contentDescription = "Forged QR Code Key",
                            modifier = Modifier.fillMaxSize().testTag("generated_qr_image")
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // OPERATIONS ROW
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // SAVE BUTTON
                        Button(
                            onClick = {
                                HapticUtils.vibrate(context, 40)
                                ShareUtils.saveBitmapToGallery(
                                    context = context,
                                    bitmap = qrBitmap!!,
                                    displayName = "QuartzQR_",
                                    onShowToast = { msg, type -> viewModel.showToast(msg, type) }
                                )
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = "Download")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Download", fontSize = 13.sp)
                        }

                        // SHARE BUTTON
                        Button(
                            onClick = {
                                HapticUtils.vibrate(context, 30)
                                ShareUtils.shareBitmap(
                                    context = context,
                                    bitmap = qrBitmap!!,
                                    fileName = "quartz_qr_code.png",
                                    onShowToast = { msg, type -> viewModel.showToast(msg, type) }
                                )
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share Image", tint = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // COMPANION WEB LINK SHARING BUTTON
                    Button(
                        onClick = {
                            HapticUtils.vibrate(context, 30)
                            val rawContent = viewModel.inputText.value.ifEmpty { viewModel.wifiSsid.value }
                            val title = viewModel.inputTitle.value.ifEmpty { "Shared Code" }
                            val type = viewModel.inputType.value
                            val webLink = "https://sayanthrock.github.io/Chamo-QR/share?content=${android.net.Uri.encode(rawContent)}&type=$type&title=${android.net.Uri.encode(title)}"

                            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                this.type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_SUBJECT, "Import Quartz QR Code: $title")
                                putExtra(android.content.Intent.EXTRA_TEXT, "Check out my Quartz QR code: $webLink")
                            }
                            context.startActivity(android.content.Intent.createChooser(intent, "Share Companion Web Link"))
                        },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = "Share Link",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Share Companion Web Link",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // EDIT / NEW BTN
                    OutlinedButton(
                        onClick = {
                            HapticUtils.vibrate(context, 20)
                            viewModel.resetGenerator()
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 1.dp
                        )
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "New Code")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Create New QR Code", fontSize = 13.sp)
                    }
                }
            } else {
                // DRAW INPUT CREATOR INTERFACE
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // CATEGORY CHIPS TAB LIST
                    Text(
                        text = "Select Content Type",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val types = listOf(
                            Triple("TEXT", Icons.Default.TextFields, "Text"),
                            Triple("URL", Icons.Default.Link, "Link"),
                            Triple("WIFI", Icons.Default.Wifi, "Wi-Fi"),
                            Triple("CONTACT", Icons.Default.ContactMail, "Contact")
                        )

                        types.forEach { (typeKey, icon, label) ->
                            val isSelected = inputType == typeKey
                            Box(
                                modifier = Modifier
                                    .testTag("type_chip_$typeKey")
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) primaryColor.copy(alpha = 0.20f)
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) primaryColor.copy(alpha = 0.50f)
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        HapticUtils.vibrate(context, 20)
                                        viewModel.inputType.value = typeKey
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        tint = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }

                    // MAIN TEXT FIELDS TO INJECT
                    Text(
                        text = "Content Details",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(16.dp),
                        tonalElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Title Field
                            OutlinedTextField(
                                value = inputTitle,
                                onValueChange = { viewModel.inputTitle.value = it },
                                label = { Text("Core Title (Optional label for History)") },
                                modifier = Modifier.fillMaxWidth().testTag("input_field_title"),
                                shape = RoundedCornerShape(14.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedLabelColor = primaryColor,
                                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    focusedIndicatorColor = primaryColor,
                                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                ),
                                singleLine = true
                            )

                            // DYNAMIC SWITCHER SPECIFIC FOR EACH INPUT TYPE
                            when (inputType) {
                                "URL" -> {
                                    OutlinedTextField(
                                        value = inputText,
                                        onValueChange = { viewModel.inputText.value = it },
                                        label = { Text("Website Link URL") },
                                        placeholder = { Text("URL Link (e.g. google.com)") },
                                        modifier = Modifier.fillMaxWidth().testTag("input_field_content"),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = TextFieldDefaults.colors(
                                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            focusedLabelColor = primaryColor,
                                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                            focusedIndicatorColor = primaryColor,
                                            unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                        ),
                                        singleLine = true
                                    )
                                }
                                "WIFI" -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        OutlinedTextField(
                                            value = wifiSsid,
                                            onValueChange = { viewModel.wifiSsid.value = it },
                                            label = { Text("Network SSID (Name)") },
                                            modifier = Modifier.fillMaxWidth().testTag("input_field_ssid"),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = TextFieldDefaults.colors(
                                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                focusedContainerColor = Color.Transparent,
                                                unfocusedContainerColor = Color.Transparent,
                                                focusedLabelColor = primaryColor,
                                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                                focusedIndicatorColor = primaryColor,
                                                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                            ),
                                            singleLine = true
                                        )

                                        OutlinedTextField(
                                            value = wifiPassword,
                                            onValueChange = { viewModel.wifiPassword.value = it },
                                            label = { Text("Access Password") },
                                            modifier = Modifier.fillMaxWidth().testTag("input_field_password"),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = TextFieldDefaults.colors(
                                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                focusedContainerColor = Color.Transparent,
                                                unfocusedContainerColor = Color.Transparent,
                                                focusedLabelColor = primaryColor,
                                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                                focusedIndicatorColor = primaryColor,
                                                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                            ),
                                            singleLine = true
                                        )

                                        // Security Selection
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            val protocols = listOf("WPA", "WEP", "nopass")
                                            protocols.forEach { p ->
                                                val isPSelected = wifiSecurity == p
                                                val display = when(p) {
                                                    "WPA" -> "WPA/WPA2"
                                                    "WEP" -> "WEP"
                                                    else -> "None"
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(if (isPSelected) primaryColor.copy(alpha = 0.15f) else Color.Transparent)
                                                        .border(
                                                            1.dp,
                                                            if (isPSelected) primaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                                            RoundedCornerShape(8.dp)
                                                        )
                                                        .clickable {
                                                            HapticUtils.vibrate(context, 15)
                                                            viewModel.wifiSecurity.value = p
                                                        }
                                                        .padding(vertical = 10.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        display,
                                                        color = if (isPSelected) primaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                "CONTACT" -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        OutlinedTextField(
                                            value = contactName,
                                            onValueChange = { viewModel.contactName.value = it },
                                            label = { Text("Full Name") },
                                            modifier = Modifier.fillMaxWidth().testTag("input_field_contact_name"),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = TextFieldDefaults.colors(
                                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                focusedContainerColor = Color.Transparent,
                                                unfocusedContainerColor = Color.Transparent,
                                                focusedLabelColor = primaryColor,
                                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                                focusedIndicatorColor = primaryColor,
                                                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                            ),
                                            singleLine = true
                                        )

                                        OutlinedTextField(
                                            value = contactPhone,
                                            onValueChange = { viewModel.contactPhone.value = it },
                                            label = { Text("Phone Number") },
                                            modifier = Modifier.fillMaxWidth().testTag("input_field_contact_phone"),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = TextFieldDefaults.colors(
                                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                focusedContainerColor = Color.Transparent,
                                                unfocusedContainerColor = Color.Transparent,
                                                focusedLabelColor = primaryColor,
                                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                                focusedIndicatorColor = primaryColor,
                                                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                            ),
                                            singleLine = true
                                        )

                                        OutlinedTextField(
                                            value = contactEmail,
                                            onValueChange = { viewModel.contactEmail.value = it },
                                            label = { Text("Email Address") },
                                            modifier = Modifier.fillMaxWidth().testTag("input_field_contact_email"),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = TextFieldDefaults.colors(
                                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                focusedContainerColor = Color.Transparent,
                                                unfocusedContainerColor = Color.Transparent,
                                                focusedLabelColor = primaryColor,
                                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                                focusedIndicatorColor = primaryColor,
                                                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                            ),
                                            singleLine = true
                                        )
                                    }
                                }
                                else -> { // TEXT
                                    OutlinedTextField(
                                        value = inputText,
                                        onValueChange = { viewModel.inputText.value = it },
                                        label = { Text("Decoded Statement") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(120.dp)
                                            .testTag("input_field_content"),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = TextFieldDefaults.colors(
                                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            focusedLabelColor = primaryColor,
                                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                            focusedIndicatorColor = primaryColor,
                                            unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                        )
                                    )
                                }
                            }

                            // Dynamic Input Validation Layer
                            if (currentPayloadLength > 0) {
                                val stateColor = when {
                                    isOverCapacity -> MaterialTheme.colorScheme.error
                                    isNearCapacity -> Color(0xFFE5A93C) // Clean amber warning color
                                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                                }

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        if (isOverCapacity) {
                                            Icon(
                                                imageVector = Icons.Default.Error,
                                                contentDescription = "Error Limit Exceeded",
                                                tint = stateColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Exceeds QR standard capacity limit.",
                                                fontSize = 11.sp,
                                                color = stateColor,
                                                fontWeight = FontWeight.Medium
                                            )
                                        } else if (isNearCapacity) {
                                            Icon(
                                                imageVector = Icons.Default.Warning,
                                                contentDescription = "High Density Warning",
                                                tint = stateColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "High density. Code may be harder to scan.",
                                                fontSize = 11.sp,
                                                color = stateColor,
                                                fontWeight = FontWeight.Medium
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Optimal Capacity",
                                                tint = primaryColor.copy(alpha = 0.7f),
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Payload density level: Optimal",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                            )
                                        }
                                    }

                                    Text(
                                        text = "$currentPayloadLength / $maxCapacity",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = stateColor
                                    )
                                }
                            }
                        }
                    }


                    Spacer(modifier = Modifier.height(24.dp))

                    // DYNAMIC REDIRECTION TOGGLE (For URLs)
                    if (inputType == "URL") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f), RoundedCornerShape(16.dp))
                                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f), RoundedCornerShape(16.dp))
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = "Dynamic Redirection",
                                        tint = Color(0xFFFFB703),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Forge Redirection Core",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = "Creates editable link target & enables tracking scan logs instantly.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            Switch(
                                checked = isDynamic,
                                onCheckedChange = {
                                    HapticUtils.vibrate(context, 20)
                                    viewModel.isDynamicQr.value = it
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = primaryColor,
                                    checkedTrackColor = primaryColor.copy(alpha = 0.3f)
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // QR NODE VISUAL STYLE PICKER
                    Text(
                        text = "QR Module Style",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    var dropdownExpanded by remember { mutableStateOf(false) }
                    val styles = listOf("Square", "Rounded", "Dot", "Mosaic", "Thin")
                    val currentStyleDisplay = when (selectedStyle) {
                        "Classic", "Square" -> "Square"
                        "Circles", "Dot" -> "Dot"
                        "Smooth", "Mosaic" -> "Mosaic"
                        else -> selectedStyle
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                RoundedCornerShape(14.dp)
                            )
                            .clickable {
                                HapticUtils.vibrate(context, 15)
                                dropdownExpanded = !dropdownExpanded
                            }
                            .padding(horizontal = 16.dp)
                            .testTag("style_dropdown_box"),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val styleIcon = when (currentStyleDisplay) {
                                    "Square" -> Icons.Default.GridOn
                                    "Rounded" -> Icons.Default.RoundedCorner
                                    "Dot" -> Icons.Default.Lens
                                    "Mosaic" -> Icons.Default.BlurOn
                                    "Thin" -> Icons.Default.BorderOuter
                                    else -> Icons.Default.Category
                                }
                                Icon(
                                    imageVector = styleIcon,
                                    contentDescription = "Selected Style",
                                    tint = primaryColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Module Shape Style",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                    Text(
                                        text = currentStyleDisplay,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            Icon(
                                imageVector = if (dropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = "Toggle Style List",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            modifier = Modifier
                                .fillMaxWidth(0.88f)
                                .background(MaterialTheme.colorScheme.surface)
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                    RoundedCornerShape(12.dp)
                                )
                        ) {
                            styles.forEach { styleOpt ->
                                val isSelected = currentStyleDisplay == styleOpt
                                val styleDesc = when (styleOpt) {
                                    "Square" -> "Classic solid blocks"
                                    "Rounded" -> "Smooth, friendly corners"
                                    "Dot" -> "Elegant circular matrices"
                                    "Mosaic" -> "Interconnecting modules"
                                    "Thin" -> "Minimalist fine lines"
                                    else -> ""
                                }
                                val itemIcon = when (styleOpt) {
                                    "Square" -> Icons.Default.GridOn
                                    "Rounded" -> Icons.Default.RoundedCorner
                                    "Dot" -> Icons.Default.Lens
                                    "Mosaic" -> Icons.Default.BlurOn
                                    "Thin" -> Icons.Default.BorderOuter
                                    else -> Icons.Default.Category
                                }

                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = itemIcon,
                                                    contentDescription = null,
                                                    tint = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text(
                                                        text = styleOpt,
                                                        fontSize = 14.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = styleDesc,
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                                    )
                                                }
                                            }
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    tint = primaryColor,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        HapticUtils.vibrate(context, 20)
                                        viewModel.selectedQrStyle.value = styleOpt
                                        dropdownExpanded = false
                                    },
                                    colors = MenuDefaults.itemColors(
                                        textColor = MaterialTheme.colorScheme.onSurface,
                                        leadingIconColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // TRANSPARENT BACKGROUND TOGGLE
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f), RoundedCornerShape(16.dp))
                            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                            .testTag("transparent_bg_toggle_row"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Opacity,
                                    contentDescription = "Transparent Background",
                                    tint = primaryColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Transparent Background",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "Removes the solid background color and sets it to transparent.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier.padding(top = 4.dp)
                                    .testTag("transparent_bg_toggle_desc")
                            )
                        }
                        Switch(
                            checked = isTransparentBg,
                            onCheckedChange = {
                                HapticUtils.vibrate(context, 20)
                                viewModel.isTransparentBg.value = it
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = primaryColor,
                                checkedTrackColor = primaryColor.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.testTag("transparent_bg_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // OUTER EYE RING TINT SELECTOR
                    Text(
                        text = "Outer Eye Ring Tint",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        viewModel.colorOptions.forEach { opt ->
                            val isSelected = selectedEyeColor.name == opt.name
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(opt.primaryColor))
                                    .clickable {
                                        HapticUtils.vibrate(context, 15)
                                        viewModel.selectedEyeColor.value = opt
                                    }
                                    .border(
                                        width = 3.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                        shape = CircleShape
                                    )
                            )
                        }
                    }

                    // INNER EYE CORE TINT SELECTOR
                    Text(
                        text = "Inner Eye Core Tint",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        viewModel.colorOptions.forEach { opt ->
                            val isSelected = selectedInnerEyeColor.name == opt.name
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(opt.primaryColor))
                                    .clickable {
                                        HapticUtils.vibrate(context, 15)
                                        viewModel.selectedInnerEyeColor.value = opt
                                    }
                                    .border(
                                        width = 3.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                        shape = CircleShape
                                    )
                            )
                        }
                    }

                    // QR MINERAL PIGMENT SPECIFICATION
                    Text(
                        text = "Core QR Dye Tint",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        viewModel.colorOptions.forEach { opt ->
                            val isColorSelected = selectedQrColor.name == opt.name
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(opt.primaryColor))
                                    .clickable {
                                        HapticUtils.vibrate(context, 15)
                                        viewModel.selectedQrColor.value = opt
                                    }
                                    .border(
                                        width = 3.dp,
                                        color = if (isColorSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isColorSelected) Color.Transparent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                        shape = CircleShape
                                    )
                            )
                        }
                    }

                    // TRIGGER GENERATION BUTTON
                    Button(
                        onClick = {
                            HapticUtils.vibrate(context, 50)
                            if (isOverCapacity) {
                                viewModel.showToast("Cannot generate QR: content length exceeds capacity limit.", CustomToastType.ERROR)
                            } else {
                                viewModel.generateQR()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("generate_code_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isOverCapacity) Color.Red else Color.White,
                            contentColor = if (isOverCapacity) Color.White else Color.Black
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(
                            text = if (isOverCapacity) "Limit Exceeded" else "Generate QR",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = if (isOverCapacity) Icons.Default.ErrorOutline else Icons.Default.ArrowForward,
                            contentDescription = "Generate",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // PREVIOUSLY CREATED SECTION (Screenshot 1)
        val historyRecords by viewModel.historyRecords.collectAsState()
        val latestCreated = remember(historyRecords) {
            historyRecords.firstOrNull { !it.isScanned } ?: historyRecords.firstOrNull()
        }

        if (latestCreated != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Previously Created",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "View All",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFA1A1AA),
                    modifier = Modifier.clickable {
                        HapticUtils.vibrate(context, 20)
                        viewModel.selectTab("HISTORY")
                    }
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        HapticUtils.vibrate(context, 20)
                        viewModel.selectTab("HISTORY")
                    },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f).padding(end = 12.dp)
                    ) {
                        Text(
                            text = latestCreated.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = latestCreated.content,
                            fontSize = 12.sp,
                            color = Color(0xFFA1A1AA),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Category Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF3B1825))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = latestCreated.type,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF75C3)
                                )
                            }

                            // Source Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF152B42))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (latestCreated.isScanned) "SCAN" else "GENERATE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8)
                                )
                            }

                            // Time Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF143224))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "a moment ago",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4ADE80)
                                )
                            }
                        }
                    }

                    // QR CODE THUMBNAIL IN WHITE SQUARE FRAME
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val thumbBitmap = remember(latestCreated.content) {
                            QRGenerator.generate(
                                text = latestCreated.content,
                                style = latestCreated.selectedStyle
                            )
                        }

                        Image(
                            bitmap = thumbBitmap.asImageBitmap(),
                            contentDescription = "QR Thumbnail",
                            modifier = Modifier.fillMaxSize()
                        )

                    }
                }
            }
        }
    }
}
