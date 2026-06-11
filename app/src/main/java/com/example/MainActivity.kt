package com.example

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.QrRecord
import com.example.ui.components.CameraPreview
import com.example.ui.theme.MyApplicationTheme
import com.example.utils.QrStyle
import com.example.utils.QrCodeGenerator
import com.example.viewmodel.QrViewModel
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                var showSplash by remember { mutableStateOf(true) }
                
                if (showSplash) {
                    RockQrSplashScreen(onSplashFinished = { showSplash = false })
                } else {
                    QrMainDashboard()
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QrMainDashboard(viewModel: QrViewModel = viewModel()) {
    val activeTab by viewModel.activeTab.collectAsState()
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == "SCAN",
                    onClick = { viewModel.selectTab("SCAN") },
                    icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = "Scanner Tab") },
                    label = { Text(stringResource(R.string.tab_scan), fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                )
                NavigationBarItem(
                    selected = activeTab == "GENERATE",
                    onClick = { viewModel.selectTab("GENERATE") },
                    icon = { Icon(Icons.Default.QrCode, contentDescription = "Generator Tab") },
                    label = { Text(stringResource(R.string.tab_generate), fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                )
                NavigationBarItem(
                    selected = activeTab == "HISTORY",
                    onClick = { viewModel.selectTab("HISTORY") },
                    icon = { Icon(Icons.Default.History, contentDescription = "History Tab") },
                    label = { Text(stringResource(R.string.tab_history), fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                )
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "TabTransition"
            ) { targetTab ->
                when (targetTab) {
                    "SCAN" -> ScanScreen(viewModel)
                    "GENERATE" -> GenerateScreen(viewModel)
                    "HISTORY" -> HistoryScreen(viewModel)
                }
            }
        }
    }
}

// ------------------ SCANNER COMPOSABLE ------------------
@Composable
fun ScanScreen(viewModel: QrViewModel) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scannedText by viewModel.scannedText.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()

    // Runtime Permission Handler
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    // Show scanned detail bottom dialog/sheet when detected
    var showDetailDialog by remember { mutableStateOf(false) }
    LaunchedEffect(scannedText) {
        if (scannedText != null) {
            showDetailDialog = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Launcher Title Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Rock QR Code",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Instant, offline secure scanning",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
            IconButton(
                onClick = { launcher.launch(Manifest.permission.CAMERA) },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            ) {
                Icon(
                    imageVector = if (hasCameraPermission) Icons.Default.CameraAlt else Icons.Default.NoPhotography,
                    contentDescription = "Permission Status indicator",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Scanner Area Frame Box
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            if (hasCameraPermission && isScanning) {
                CameraPreview(
                    onQrScanned = { text ->
                        viewModel.setScannedText(text)
                    }
                )

                // Beautiful HUD Scan corners overlay
                Canvas(
                    modifier = Modifier
                        .size(240.dp)
                        .semantics { contentDescription = "Scanning view frame" }
                ) {
                    val stroke = 6.dp.toPx()
                    val sizeLn = 36.dp.toPx()
                    val fgColor = Color(0xFF00BDD6)

                    // Top Left
                    drawLine(fgColor, Offset(0f, 0f), Offset(sizeLn, 0f), strokeWidth = stroke)
                    drawLine(fgColor, Offset(0f, 0f), Offset(0f, sizeLn), strokeWidth = stroke)

                    // Top Right
                    drawLine(fgColor, Offset(size.width, 0f), Offset(size.width - sizeLn, 0f), strokeWidth = stroke)
                    drawLine(fgColor, Offset(size.width, 0f), Offset(size.width, sizeLn), strokeWidth = stroke)

                    // Bottom Left
                    drawLine(fgColor, Offset(0f, size.height), Offset(sizeLn, size.height), strokeWidth = stroke)
                    drawLine(fgColor, Offset(0f, size.height), Offset(0f, size.height - sizeLn), strokeWidth = stroke)

                    // Bottom Right
                    drawLine(fgColor, Offset(size.width, size.height), Offset(size.width - sizeLn, size.height), strokeWidth = stroke)
                    drawLine(fgColor, Offset(size.width, size.height), Offset(size.width, size.height - sizeLn), strokeWidth = stroke)
                }

                // Smoothly pulsating scanner laser guides
                var animationState by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    while (true) {
                        animationState = !animationState
                        kotlinx.coroutines.delay(1800)
                    }
                }
                val animVerticalOffset by animateFloatAsState(
                    targetValue = if (animationState) 230f else 10f,
                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                    label = "LaserGuide"
                )
                Box(
                    modifier = Modifier
                        .width(220.dp)
                        .height(3.dp)
                        .offset(y = animVerticalOffset.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, Color(0xFFE040FB), Color(0xFF00BDD6), Color.Transparent)
                            )
                        )
                )
            } else {
                // Friendly emulator fallback state (when permission is denied or running in headless sandbox)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LinkedCamera,
                        contentDescription = "Camera unavailable graphic",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Camera Live Preview Stopped",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Unlock using security button header, or enter test input string below to scan in simulation mode.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Simulated Input scanner box - CRITICAL UX for headless web previews
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                var simText by remember { mutableStateOf("") }
                Text(
                    text = "Streaming Emulator Mock Barcode Parser",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = simText,
                        onValueChange = { simText = it },
                        placeholder = { Text("Paste test URL, Wi-Fi configuration...", fontSize = 13.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                        trailingIcon = {
                            if (simText.isNotEmpty()) {
                                IconButton(onClick = { simText = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear custom input")
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (simText.isNotBlank()) {
                                viewModel.triggerManualScanText(simText)
                                simText = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Mock", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // SCANNED DIALOG RESULT VIEWER
    if (showDetailDialog && scannedText != null) {
        Dialog(onDismissRequest = {
            showDetailDialog = false
            viewModel.resetScanner()
        }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Code Scanned Icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Decoder Successful!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = "Parsed Content Payload:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = scannedText ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Dialog Actions list
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(scannedText ?: ""))
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy text")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy", fontWeight = FontWeight.Bold)
                        }

                        val isUrl = scannedText?.startsWith("http", ignoreCase = true) == true
                        val btnText = if (isUrl) "Browse" else "Share"
                        Button(
                            onClick = {
                                if (isUrl) {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(scannedText))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Cannot open browser", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, scannedText)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share QR Content"))
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = if (isUrl) Icons.Default.OpenInBrowser else Icons.Default.Share,
                                contentDescription = "Action button icon"
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(btnText, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(
                        onClick = {
                            showDetailDialog = false
                            viewModel.resetScanner()
                        }
                    ) {
                        Text("Dismiss and Reset Analyzer", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ------------------ GENERATOR COMPOSABLE ------------------
@Composable
fun GenerateScreen(viewModel: QrViewModel) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val genFormat by viewModel.genFormat.collectAsState()
    val genStyle by viewModel.genStyle.collectAsState()
    val genFgColor by viewModel.genFgColor.collectAsState()
    val generatedBitmap by viewModel.generatedBitmap.collectAsState()

    // Parameters
    val plainText by viewModel.plainText.collectAsState()
    val urlLink by viewModel.urlLink.collectAsState()
    val wifiSsid by viewModel.wifiSsid.collectAsState()
    val wifiPassword by viewModel.wifiPassword.collectAsState()
    val wifiSecurity by viewModel.wifiSecurity.collectAsState()
    val phoneNum by viewModel.phoneNum.collectAsState()
    val emailRecipient by viewModel.emailRecipient.collectAsState()
    val emailSubject by viewModel.emailSubject.collectAsState()
    val emailBody by viewModel.emailBody.collectAsState()

    // Predefined stylish color preset combinations
    val colorSwatches = listOf(
        Pair("#0A0A0A", "Midnight"),
        Pair("#00BDD6", "Neon Teal"),
        Pair("#FFA000", "Sunfire Core"),
        Pair("#7A1C1C", "Red Lava"),
        Pair("#4A148C", "Amethyst"),
        Pair("#1A5F20", "Forest Emerald")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "QR Builder Studio",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Generate pebbles, rocks, or classic styled coordinates",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Step 1: Format Selector Tabs
        ScrollableTabRow(
            selectedTabIndex = when(genFormat) {
                "TEXT" -> 0
                "URL" -> 1
                "WIFI" -> 2
                "PHONE" -> 3
                "EMAIL" -> 4
                else -> 0
            },
            edgePadding = 0.dp,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[tabPositions.indexOfFirst { true }]),
                    color = MaterialTheme.colorScheme.primary
                )
            },
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            val formats = listOf("TEXT", "URL", "WIFI", "PHONE", "EMAIL")
            formats.forEach { format ->
                Tab(
                    selected = genFormat == format,
                    onClick = { viewModel.genFormat.value = format },
                    text = {
                        Text(
                            text = format,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (genFormat == format) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Step 2: Input fields conditional rendering
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                when (genFormat) {
                    "TEXT" -> {
                        OutlinedTextField(
                            value = plainText,
                            onValueChange = { viewModel.plainText.value = it },
                            label = { Text("Write raw text here") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    "URL" -> {
                        OutlinedTextField(
                            value = urlLink,
                            onValueChange = { viewModel.urlLink.value = it },
                            label = { Text("Enter Web URL / Link") },
                            placeholder = { Text("example.com") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.Language, contentDescription = null) }
                        )
                    }
                    "WIFI" -> {
                        OutlinedTextField(
                            value = wifiSsid,
                            onValueChange = { viewModel.wifiSsid.value = it },
                            label = { Text("Network SSID (Name)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.Wifi, contentDescription = null) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = wifiPassword,
                            onValueChange = { viewModel.wifiPassword.value = it },
                            label = { Text("Password") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Security Mode:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("WPA", "WEP", "nopass").forEach { mode ->
                                val selected = wifiSecurity == mode
                                FilterChip(
                                    selected = selected,
                                    onClick = { viewModel.wifiSecurity.value = mode },
                                    label = { Text(if (mode == "nopass") "None" else mode) }
                                )
                            }
                        }
                    }
                    "PHONE" -> {
                        OutlinedTextField(
                            value = phoneNum,
                            onValueChange = { viewModel.phoneNum.value = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) }
                        )
                    }
                    "EMAIL" -> {
                        OutlinedTextField(
                            value = emailRecipient,
                            onValueChange = { viewModel.emailRecipient.value = it },
                            label = { Text("Recipient Email") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = emailSubject,
                            onValueChange = { viewModel.emailSubject.value = it },
                            label = { Text("Subject (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = emailBody,
                            onValueChange = { viewModel.emailBody.value = it },
                            label = { Text("Body Text (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Step 3: Styles Customizer
        Text(
            text = "1. Choose Rock Coordinate Style",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QrStyle.entries.forEach { style ->
                val isSel = genStyle == style
                val title = when (style) {
                    QrStyle.CLASSIC -> "Classic"
                    QrStyle.ROUNDED_DOT -> "Pebbles"
                    QrStyle.ROCK -> "Crystals"
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                        .border(
                            1.dp,
                            if (isSel) Color.Transparent else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { viewModel.genStyle.value = style },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Step 4: Color Picker Presets
        Text(
            text = "2. Customize Obsidian Accents",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            colorSwatches.forEach { (hex, name) ->
                val isSel = genFgColor.equals(hex, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(android.graphics.Color.parseColor(hex)))
                        .border(
                            width = if (isSel) 3.dp else 1.dp,
                            color = if (isSel) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                        .clickable { viewModel.genFgColor.value = hex }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Step 5: Live QR Rendering output
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (generatedBitmap != null) {
                    Image(
                        bitmap = generatedBitmap!!.asImageBitmap(),
                        contentDescription = "Live code generation template",
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(6.dp, Color.White, RoundedCornerShape(16.dp))
                            .background(Color.White)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Real-time Styled QR Ready",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00BDD6)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val content = viewModel.getFormattedContent()
                                clipboardManager.setText(AnnotatedString(content))
                                Toast.makeText(context, "Copied content", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CopyAll, contentDescription = "Copy QR payload")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy link")
                        }

                        Button(
                            onClick = {
                                viewModel.saveGeneratedCodeInHistory()
                                Toast.makeText(context, "Saved to history library", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = "Save template")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save History", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.background)
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f),
                                RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = "Draft setup icon",
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Enter configuration details above.",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ------------------ HISTORY COMPOSABLE ------------------
@Composable
fun HistoryScreen(viewModel: QrViewModel) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val historyRecords by viewModel.historyRecords.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isOnlyFavorites by viewModel.isOnlyFavorites.collectAsState()

    // Date formatter helper
    val dateFormatter = remember { SimpleDateFormat("MMM d, yyyy - hh:mm a", Locale.getDefault()) }

    var selectedRecordForPopup by remember { mutableStateOf<QrRecord?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Locker History",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Manage your scanned and built coordinates",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
            if (historyRecords.isNotEmpty()) {
                IconButton(
                    onClick = {
                        viewModel.clearAllHistory()
                        Toast.makeText(context, "Cleared history", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Delete all history action",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search field & Favorite toggle row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                placeholder = { Text("Search records...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear query", modifier = Modifier.size(18.dp))
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconToggleButton(
                checked = isOnlyFavorites,
                onCheckedChange = { viewModel.isOnlyFavorites.value = it },
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (isOnlyFavorites) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(12.dp)
                    )
            ) {
                Icon(
                    imageVector = if (isOnlyFavorites) Icons.Default.Star else Icons.Default.StarOutline,
                    contentDescription = "Filter Favorites",
                    tint = if (isOnlyFavorites) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // History list items rendering
        if (historyRecords.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (searchQuery.isNotEmpty()) Icons.Default.SearchOff else Icons.Default.SpeakerNotesOff,
                        contentDescription = "Empty state icon",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No Matching Records" else "Library is Empty",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Text(
                        text = if (searchQuery.isNotEmpty()) "Try refining your search keyword" else "Generated and scanned codes appear here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(historyRecords, key = { it.id }) { record ->
                    HistoryItemCard(
                        record = record,
                        dateString = dateFormatter.format(Date(record.timestamp)),
                        onToggleFav = { viewModel.toggleFavorite(record) },
                        onDelete = { viewModel.deleteRecord(record) },
                        onSelectCard = { selectedRecordForPopup = record }
                    )
                }
            }
        }
    }

    // HISTORY ITEM POPUP CODE DETAILED PREVIEW
    if (selectedRecordForPopup != null) {
        val record = selectedRecordForPopup!!
        Dialog(onDismissRequest = { selectedRecordForPopup = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Cabinet Archive",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = record.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Regenerate Bitmap on demand in popup!
                    val fgColorHex = record.customColorHex ?: "#0A0A0A"
                    val bmap: Bitmap? = remember(record.content) {
                        QrCodeGenerator.generateQrCode(
                            content = record.content,
                            foregroundHexColor = fgColorHex,
                            style = QrStyle.ROUNDED_DOT
                        )
                    }

                    if (bmap != null) {
                        Image(
                            bitmap = bmap.asImageBitmap(),
                            contentDescription = "Regenerated code",
                            modifier = Modifier
                                .size(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(4.dp, Color.White, RoundedCornerShape(12.dp))
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Locker Content:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = record.content,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(record.content))
                                Toast.makeText(context, "Copied payload", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy text")
                        }

                        val isWeb = record.content.startsWith("http", ignoreCase = true) == true
                        Button(
                            onClick = {
                                if (isWeb) {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(record.content))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Cannot open link", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, record.content)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share payload"))
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = if (isWeb) Icons.Default.OpenInBrowser else Icons.Default.Share,
                                contentDescription = "Share details"
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isWeb) "Browse" else "Share")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(onClick = { selectedRecordForPopup = null }) {
                        Text("Dismiss Window", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItemCard(
    record: QrRecord,
    dateString: String,
    onToggleFav: () -> Unit,
    onDelete: () -> Unit,
    onSelectCard: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectCard() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon identifier based on format types
            val shapeBg = if (record.type == "SCAN") MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                          else MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
            val iconTint = if (record.type == "SCAN") MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.tertiary
            val iconVec = when (record.format) {
                "URL" -> Icons.Default.Language
                "WIFI" -> Icons.Default.Wifi
                "PHONE" -> Icons.Default.Phone
                "EMAIL" -> Icons.Default.Email
                else -> Icons.Default.Article
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(shapeBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVec,
                    contentDescription = "Format indicator icon",
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Body Info Texts
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = record.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Format Tag Pill
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = record.format,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.labelMedium,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Fast actions row
            IconButton(onClick = onToggleFav) {
                Icon(
                    imageVector = if (record.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Favorite action icon",
                    tint = if (record.isFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                    modifier = Modifier.size(22.dp)
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete item",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun RockQrSplashScreen(onSplashFinished: () -> Unit) {
    var startPulse by remember { mutableStateOf(false) }
    var startTextFade by remember { mutableStateOf(false) }
    var startColorPulse by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        startPulse = true
        kotlinx.coroutines.delay(200)
        startTextFade = true
        kotlinx.coroutines.delay(400)
        startColorPulse = true
        kotlinx.coroutines.delay(1800) // 2.4s total elegant loader display
        onSplashFinished()
    }

    val pulseScale by animateFloatAsState(
        targetValue = if (startPulse) 1f else 0.4f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessLow),
        label = "LogoScale"
    )

    val logoRotation by animateFloatAsState(
        targetValue = if (startPulse) 360f else 0f,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessVeryLow),
        label = "LogoRotation"
    )

    val textAlpha by animateFloatAsState(
        targetValue = if (startTextFade) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "TextAlpha"
    )

    val gemGlowAlpha by animateFloatAsState(
        targetValue = if (startColorPulse) 0.9f else 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowGlow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1113)), // Deep luxury Obsidian Black
        contentAlignment = Alignment.Center
    ) {
        // Crystalline ambient back-glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF00BDD6).copy(alpha = 0.22f * gemGlowAlpha), Color.Transparent),
                    center = center,
                    radius = size.minDimension * 0.75f
                ),
                radius = size.minDimension * 0.75f,
                center = center
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Elegant crystalline hexagon QR logo
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                        rotationZ = logoRotation
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidthPx = 4.dp.toPx()
                    val path = androidx.compose.ui.graphics.Path().apply {
                        val w = size.width
                        val h = size.height
                        moveTo(w / 2f, 10f)
                        lineTo(w - 10f, h * 0.28f)
                        lineTo(w - 10f, h * 0.72f)
                        lineTo(w / 2f, h - 10f)
                        lineTo(10f, h * 0.72f)
                        lineTo(10f, h * 0.28f)
                        close()
                    }
                    drawPath(
                        path = path,
                        brush = Brush.sweepGradient(
                            colors = listOf(Color(0xFFFFA000), Color(0xFF00BDD6), Color(0xFFFFA000))
                        ),
                        style = Stroke(width = strokeWidthPx)
                    )
                }

                // Custom embedded QR structural code nodes
                Row(
                    modifier = Modifier.size(72.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF00BDD6))
                                .padding(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0xFF0F1113))
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFFA000))
                                .padding(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0xFF0F1113))
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.End
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF00BDD6))
                                .padding(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0xFF0F1113))
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(modifier = Modifier.size(6.dp).background(Color.White, RoundedCornerShape(1f)))
                            Box(modifier = Modifier.size(6.dp).background(Color(0xFF00BDD6), RoundedCornerShape(1f)))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Subtitle info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer { alpha = textAlpha }
            ) {
                Text(
                    text = "ROCK QR CODE",
                    fontSize = 24.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 4.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "SECURE  •  OFFLINE  •  INSTANT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00BDD6),
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Fine Loading Energy charging bar
                Box(
                    modifier = Modifier
                        .width(160.dp)
                        .height(3.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "SplashLoaderTransition")
                    val progressOffset by infiniteTransition.animateFloat(
                        initialValue = -160f,
                        targetValue = 160f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(
                                durationMillis = 1400,
                                easing = LinearEasing
                            ),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "ProgressBarOffset"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(80.dp)
                            .offset(x = progressOffset.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color.Transparent, Color(0xFF00BDD6), Color(0xFFFFA000), Color.Transparent)
                                )
                            )
                    )
                }
            }
        }
    }
}
