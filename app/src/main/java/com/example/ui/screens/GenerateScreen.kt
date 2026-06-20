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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LiquidGlassTheme
import com.example.utils.HapticUtils
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

    // Color options
    val selectedQrColor by viewModel.selectedQrColor.collectAsState()
    val qrBitmap by viewModel.generatedQrBitmap.collectAsState()
    val hasResult by viewModel.hasGeneratedResult.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 120.dp), // Leaves clearance for the floating navigation bar
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // HEADER TITLE
        Text(
            text = "Quartz Forge Generator",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontFamily = FontFamily.SansSerif,
            modifier = Modifier.fillMaxWidth().testTag("generate_title")
        )

        Text(
            text = "Craft local QR codes with customizable crystal mineral dyes.",
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 20.dp)
        )

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
                        .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(24.dp))
                        .border(
                            1.dp,
                            Brush.linearGradient(
                                listOf(primaryColor.copy(alpha = 0.4f), Color.White.copy(alpha = 0.05f))
                            ),
                            RoundedCornerShape(24.dp)
                        )
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Your Finished Core",
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
                                    displayName = "RockQR_",
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
                                    fileName = "rock_qr_code.png",
                                    onShowToast = { msg, type -> viewModel.showToast(msg, type) }
                                )
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.10f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share Image", tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share", color = Color.White, fontSize = 13.sp)
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
                            val webLink = "https://sayanthrock.github.io/Rock-QR-Code/share/?content=${android.net.Uri.encode(rawContent)}&type=$type&title=${android.net.Uri.encode(title)}"

                            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                this.type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_SUBJECT, "Import Rock QR Code: $title")
                                putExtra(android.content.Intent.EXTRA_TEXT, "Look at my Rock-forged QR code: $webLink")
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
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 1.dp
                        )
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "New Code")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Draft New Core", fontSize = 13.sp)
                    }
                }
            } else {
                // DRAW INPUT CREATOR INTERFACE
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // CATEGORY CHIPS TAB LIST
                    Text(
                        text = "Select Data Compound",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.5f),
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
                                        else Color.White.copy(alpha = 0.04f)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) primaryColor.copy(alpha = 0.50f)
                                        else Color.White.copy(alpha = 0.08f),
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
                                        tint = if (isSelected) primaryColor else Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }

                    // MAIN TEXT FIELDS TO INJECT
                    Text(
                        text = "Content Details",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Title Field (optional description for history tracking)
                    OutlinedTextField(
                        value = inputTitle,
                        onValueChange = { viewModel.inputTitle.value = it },
                        label = { Text("Core Title (Optional label for History)") },
                        modifier = Modifier.fillMaxWidth().testTag("input_field_title"),
                        shape = RoundedCornerShape(14.dp),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color.White.copy(alpha = 0.03f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.03f),
                            focusedLabelColor = primaryColor,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.4f),
                            focusedIndicatorColor = primaryColor,
                            unfocusedIndicatorColor = Color.White.copy(alpha = 0.12f)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

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
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color.White.copy(alpha = 0.03f),
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.03f),
                                    focusedLabelColor = primaryColor,
                                    unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                                    focusedIndicatorColor = primaryColor,
                                    unfocusedIndicatorColor = Color.White.copy(alpha = 0.12f)
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
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedContainerColor = Color.White.copy(alpha = 0.03f),
                                        unfocusedContainerColor = Color.White.copy(alpha = 0.03f),
                                        focusedLabelColor = primaryColor,
                                        unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                                        focusedIndicatorColor = primaryColor,
                                        unfocusedIndicatorColor = Color.White.copy(alpha = 0.12f)
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
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedContainerColor = Color.White.copy(alpha = 0.03f),
                                        unfocusedContainerColor = Color.White.copy(alpha = 0.03f),
                                        focusedLabelColor = primaryColor,
                                        unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                                        focusedIndicatorColor = primaryColor,
                                        unfocusedIndicatorColor = Color.White.copy(alpha = 0.12f)
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
                                                    if (isPSelected) primaryColor else Color.White.copy(alpha = 0.1f),
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
                                                color = if (isPSelected) Color.White else Color.White.copy(alpha = 0.6f),
                                                fontSize = 11.sp,
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
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedContainerColor = Color.White.copy(alpha = 0.03f),
                                        unfocusedContainerColor = Color.White.copy(alpha = 0.03f),
                                        focusedLabelColor = primaryColor,
                                        unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                                        focusedIndicatorColor = primaryColor,
                                        unfocusedIndicatorColor = Color.White.copy(alpha = 0.12f)
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
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedContainerColor = Color.White.copy(alpha = 0.03f),
                                        unfocusedContainerColor = Color.White.copy(alpha = 0.03f),
                                        focusedLabelColor = primaryColor,
                                        unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                                        focusedIndicatorColor = primaryColor,
                                        unfocusedIndicatorColor = Color.White.copy(alpha = 0.12f)
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
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedContainerColor = Color.White.copy(alpha = 0.03f),
                                        unfocusedContainerColor = Color.White.copy(alpha = 0.03f),
                                        focusedLabelColor = primaryColor,
                                        unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                                        focusedIndicatorColor = primaryColor,
                                        unfocusedIndicatorColor = Color.White.copy(alpha = 0.12f)
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
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color.White.copy(alpha = 0.03f),
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.03f),
                                    focusedLabelColor = primaryColor,
                                    unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                                    focusedIndicatorColor = primaryColor,
                                    unfocusedIndicatorColor = Color.White.copy(alpha = 0.12f)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // QR MINERAL PIGMENT SPECIFICATION
                    Text(
                        text = "Core QR Dye Tint",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.5f),
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
                                        color = if (isColorSelected) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isColorSelected) Color.Transparent else Color.White.copy(alpha = 0.2f),
                                        shape = CircleShape
                                    )
                            )
                        }
                    }

                    // TRIGGER GENERATION BUTTON
                    Button(
                        onClick = {
                            HapticUtils.vibrate(context, 50)
                            viewModel.generateQR()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("generate_code_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = "Forge Key")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Forge QR Core",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
