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
import com.example.ui.components.*
import com.example.ui.theme.MyApplicationTheme
import com.example.utils.QrStyle
import com.example.utils.QrCodeGenerator
import com.example.utils.ShareUtils
import com.example.viewmodel.QrViewModel
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.platform.testTag
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: QrViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState()
            val dynamicColorEnabled by viewModel.dynamicColorEnabled.collectAsState()
            val colorPreset by viewModel.colorPreset.collectAsState()

            val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
            val useDarkTheme = when (themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemDark
            }

            MyApplicationTheme(
                darkTheme = useDarkTheme,
                dynamicColor = dynamicColorEnabled,
                colorPresetName = colorPreset
            ) {
                var showSplash by remember { mutableStateOf(true) }
                
                if (showSplash) {
                    RockQrSplashScreen(onSplashFinished = { showSplash = false })
                } else {
                    QrMainDashboard(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QrMainDashboard(viewModel: QrViewModel = viewModel()) {
    val activeTab by viewModel.activeTab.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val useDarkTheme = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemDark
    }
    
    val context = LocalContext.current
    var showSettingsDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Dynamic floating liquid orbit backdrop
        LiquidGlassBackground(useDarkTheme = useDarkTheme)

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            bottomBar = {
                // Floating glass style bottom navigation
                NavigationBar(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .liquidGlass(shape = RoundedCornerShape(24.dp))
                        .windowInsetsPadding(WindowInsets.navigationBars),
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp
                ) {
                    NavigationBarItem(
                        selected = activeTab == "SCAN",
                        onClick = { viewModel.selectTab("SCAN") },
                        icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = "Scanner Tab") },
                        label = { Text(stringResource(R.string.tab_scan), fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    )
                    NavigationBarItem(
                        selected = activeTab == "GENERATE",
                        onClick = { viewModel.selectTab("GENERATE") },
                        icon = { Icon(Icons.Default.QrCode, contentDescription = "Generator Tab") },
                        label = { Text(stringResource(R.string.tab_generate), fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    )
                    NavigationBarItem(
                        selected = activeTab == "HISTORY",
                        onClick = { viewModel.selectTab("HISTORY") },
                        icon = { Icon(Icons.Default.History, contentDescription = "History Tab") },
                        label = { Text(stringResource(R.string.tab_history), fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
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
                color = Color.Transparent
            ) {
                AnimatedContent(
                    targetState = activeTab,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "TabTransition"
                ) { targetTab ->
                    when (targetTab) {
                        "SCAN" -> ScanScreen(viewModel, onSettingsClick = { showSettingsDialog = true })
                        "GENERATE" -> GenerateScreen(viewModel, onSettingsClick = { showSettingsDialog = true })
                        "HISTORY" -> HistoryScreen(viewModel, onSettingsClick = { showSettingsDialog = true })
                    }
                }

                SettingsDialog(
                    showDialog = showSettingsDialog,
                    onDismiss = { showSettingsDialog = false },
                    viewModel = viewModel
                )
            }
        }
    }
}

// ------------------ SCANNER COMPOSABLE ------------------
@Composable
fun ScanScreen(viewModel: QrViewModel, onSettingsClick: () -> Unit) {
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

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // --- 1. FULL SCREEN CAMERA PREVIEW WITH TRANSLUCENT OVERLAYS ---
        if (hasCameraPermission && isScanning) {
            CameraPreview(
                onQrScanned = { text ->
                    viewModel.setScannedText(text)
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Elegant placeholder matching deep slate
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F1115)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LinkedCamera,
                        contentDescription = "Camera Standby",
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Camera Live Feed Ready",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Standard Android permission dialog can be unlocked using the security indicator, or insert a mock QR payload below.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // --- 2. GLASS DETECTING HUD OVERLAY (CENTERED) ---
        if (isScanning) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val activePrimary = MaterialTheme.colorScheme.primary
                
                Canvas(
                    modifier = Modifier
                        .size(240.dp)
                        .semantics { contentDescription = "Scanning view frame" }
                ) {
                    val stroke = 6.dp.toPx()
                    val sizeLn = 36.dp.toPx()
                    val fgColor = if (hasCameraPermission) activePrimary else Color(0xFF00FFCC)

                    // Top Left Corner
                    drawLine(fgColor, Offset(0f, 0f), Offset(sizeLn, 0f), strokeWidth = stroke)
                    drawLine(fgColor, Offset(0f, 0f), Offset(0f, sizeLn), strokeWidth = stroke)

                    // Top Right Corner
                    drawLine(fgColor, Offset(size.width, 0f), Offset(size.width - sizeLn, 0f), strokeWidth = stroke)
                    drawLine(fgColor, Offset(size.width, 0f), Offset(size.width, sizeLn), strokeWidth = stroke)

                    // Bottom Left Corner
                    drawLine(fgColor, Offset(0f, size.height), Offset(sizeLn, size.height), strokeWidth = stroke)
                    drawLine(fgColor, Offset(0f, size.height), Offset(0f, size.height - sizeLn), strokeWidth = stroke)

                    // Bottom Right Corner
                    drawLine(fgColor, Offset(size.width, size.height), Offset(size.width - sizeLn, size.height), strokeWidth = stroke)
                    drawLine(fgColor, Offset(size.width, size.height), Offset(size.width, size.height - sizeLn), strokeWidth = stroke)
                }

                // Pulsating laser sweeping indicator
                var animationState by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    while (true) {
                        animationState = !animationState
                        kotlinx.coroutines.delay(1800)
                    }
                }
                val animVerticalOffset by animateFloatAsState(
                    targetValue = if (animationState) 110f else -110f,
                    animationSpec = tween(durationMillis = 1800, easing = LinearEasing),
                    label = "LaserGuide"
                )
                Box(
                    modifier = Modifier
                        .width(220.dp)
                        .height(3.dp)
                        .offset(y = animVerticalOffset.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, Color(0xFF00FFCC), activePrimary, Color.Transparent)
                            )
                        )
                )
            }
        }

        // --- 3. FLOATING GLASS HEADER (TOP) ---
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
        ) {
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AURA PARSER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "Chiseled Lens",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(
                        onClick = { launcher.launch(Manifest.permission.CAMERA) },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.glassTouchFeedback { launcher.launch(Manifest.permission.CAMERA) }
                    ) {
                        Icon(
                            imageVector = if (hasCameraPermission) Icons.Default.CameraAlt else Icons.Default.NoPhotography,
                            contentDescription = "Permission Status indicator",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onSettingsClick,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.glassTouchFeedback { onSettingsClick() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // --- 4. FLOATING GLASS MOCK PARSER PANEL (BOTTOM) ---
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    var simText by remember { mutableStateOf("") }
                    Text(
                        text = "MOCK BEACON ANALYZER",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = simText,
                            onValueChange = { simText = it },
                            placeholder = { Text("Paste test URL, payee UPI, contact vCard...", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
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
    }

    // SCANNED DIALOG RESULT VIEWER
    if (showDetailDialog && scannedText != null) {
        Dialog(onDismissRequest = {
            showDetailDialog = false
            viewModel.resetScanner()
        }) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
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

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .liquidGlass(RoundedCornerShape(12.dp), bgAlpha = 0.2f)
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
fun GenerateScreen(viewModel: QrViewModel, onSettingsClick: () -> Unit) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val genFormat by viewModel.genFormat.collectAsState()
    val genStyle by viewModel.genStyle.collectAsState()
    val genFgColor by viewModel.genFgColor.collectAsState()
    val genBgColor by viewModel.genBgColor.collectAsState()
    val activeEmbedLogo by viewModel.embedLogo.collectAsState()
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
    val smsPhone by viewModel.smsPhone.collectAsState()
    val smsBody by viewModel.smsBody.collectAsState()
    val upiVpa by viewModel.upiVpa.collectAsState()
    val upiName by viewModel.upiName.collectAsState()
    val upiAmount by viewModel.upiAmount.collectAsState()
    val contactName by viewModel.contactName.collectAsState()
    val contactPhone by viewModel.contactPhone.collectAsState()
    val contactEmail by viewModel.contactEmail.collectAsState()
    val contactOrg by viewModel.contactOrg.collectAsState()

    // Active design mode: "STANDARD" or the futuristic "MATERIAL_10"
    var isMaterial10Enabled by remember { mutableStateOf(true) }

    // Holographic laser sweep state
    val infiniteTransition = rememberInfiniteTransition(label = "hologram_laser")
    val laserSweepValue by infiniteTransition.animateFloat(
        initialValue = -0.1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "laser_sweep"
    )

    // Pulsing aura animation for the card representing Material 10 3D emission
    val pulsingAuraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulsing_aura"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // App Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            RockChiseledHeader(
                title = if (isMaterial10Enabled) "Material 10 QR Studio" else "QR Builder Studio",
                subtitle = if (isMaterial10Enabled) "Ultra-expressive neon-matrix hologram engine" else "Generate classic styled coordinates",
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            
            // Material 10 Interactive Toggle Badge
            IconButton(
                onClick = { isMaterial10Enabled = !isMaterial10Enabled },
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = if (isMaterial10Enabled) Color(0xFF00FFCC).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                        shape = ChiseledOctagonShape
                    )
                    .border(
                        width = 1.dp,
                        color = if (isMaterial10Enabled) Color(0xFF00FFCC) else Color.Transparent,
                        shape = ChiseledOctagonShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Flare,
                    contentDescription = "Toggle Material 10 mode",
                    tint = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(
                onClick = onSettingsClick,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ),
                modifier = Modifier.glassTouchFeedback { onSettingsClick() }
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Step 1: Format Selector Tabs
        ScrollableTabRow(
            selectedTabIndex = when (genFormat) {
                "TEXT" -> 0
                "URL" -> 1
                "WIFI" -> 2
                "PHONE" -> 3
                "EMAIL" -> 4
                "SMS" -> 5
                "UPI" -> 6
                "CONTACT" -> 7
                else -> 0
            },
            edgePadding = 0.dp,
            containerColor = Color.Transparent,
            indicator = { tabPositions ->
                val currIndex = when (genFormat) {
                    "TEXT" -> 0
                    "URL" -> 1
                    "WIFI" -> 2
                    "PHONE" -> 3
                    "EMAIL" -> 4
                    "SMS" -> 5
                    "UPI" -> 6
                    "CONTACT" -> 7
                    else -> 0
                }
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[currIndex]),
                    color = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary
                )
            },
            modifier = Modifier
                .liquidGlass(RoundedCornerShape(12.dp), bgAlpha = 0.25f)
        ) {
            val formats = listOf("TEXT", "URL", "WIFI", "PHONE", "EMAIL", "SMS", "UPI", "CONTACT")
            formats.forEach { format ->
                Tab(
                    selected = genFormat == format,
                    onClick = { viewModel.genFormat.value = format },
                    text = {
                        Text(
                            text = format,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (genFormat == format) {
                                if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            }
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Step 2: Input fields conditional rendering
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (isMaterial10Enabled) {
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF161A22), Color(0xFF0C0E14))
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface)
                        )
                    }
                )
                .border(
                    width = 1.dp,
                    color = if (isMaterial10Enabled) Color(0xFF00FFCC).copy(alpha = 0.35f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            Column {
                when (genFormat) {
                    "TEXT" -> {
                        OutlinedTextField(
                            value = plainText,
                            onValueChange = { viewModel.plainText.value = it },
                            label = { Text("Input Text Payload") },
                            placeholder = { Text("Write any raw text data...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("qr_content_input"),
                            minLines = 3,
                            maxLines = 5,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                            )
                        )
                    }
                    "URL" -> {
                        OutlinedTextField(
                            value = urlLink,
                            onValueChange = { viewModel.urlLink.value = it },
                            label = { Text("Input Web URL") },
                            placeholder = { Text("example.com") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("qr_url_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = null,
                                    tint = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                            )
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
                            leadingIcon = { Icon(Icons.Default.Wifi, contentDescription = null, tint = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = wifiPassword,
                            onValueChange = { viewModel.wifiPassword.value = it },
                            label = { Text("Password") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Security Mode:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("WPA", "WEP", "nopass").forEach { mode ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (wifiSecurity == mode) {
                                                if (isMaterial10Enabled) Color(0xFF00FFCC).copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            } else {
                                                Color.Transparent
                                            }
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (wifiSecurity == mode) {
                                                if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f)
                                            },
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { viewModel.wifiSecurity.value = mode }
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (mode == "nopass") "None" else mode,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (wifiSecurity == mode) {
                                            if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                        }
                                    )
                                }
                            }
                        }
                    }
                    "PHONE" -> {
                        OutlinedTextField(
                            value = phoneNum,
                            onValueChange = { viewModel.phoneNum.value = it },
                            label = { Text("Phone Number") },
                            placeholder = { Text("+1 (555) 0199") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                    "EMAIL" -> {
                        OutlinedTextField(
                            value = emailRecipient,
                            onValueChange = { viewModel.emailRecipient.value = it },
                            label = { Text("Recipient Email") },
                            placeholder = { Text("name@example.com") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = emailSubject,
                            onValueChange = { viewModel.emailSubject.value = it },
                            label = { Text("Subject (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = emailBody,
                            onValueChange = { viewModel.emailBody.value = it },
                            label = { Text("Body Text (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                    "SMS" -> {
                        OutlinedTextField(
                            value = smsPhone,
                            onValueChange = { viewModel.smsPhone.value = it },
                            label = { Text("Recipient Phone") },
                            placeholder = { Text("e.g. +15551234567") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = smsBody,
                            onValueChange = { viewModel.smsBody.value = it },
                            label = { Text("SMS Message Body") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                    "UPI" -> {
                        OutlinedTextField(
                            value = upiVpa,
                            onValueChange = { viewModel.upiVpa.value = it },
                            label = { Text("Payee UPI ID (VPA)") },
                            placeholder = { Text("e.g. merchant@upi") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null, tint = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = upiName,
                            onValueChange = { viewModel.upiName.value = it },
                            label = { Text("Payee Merchant Name") },
                            placeholder = { Text("e.g. Acme Stores") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = upiAmount,
                            onValueChange = { viewModel.upiAmount.value = it },
                            label = { Text("Transaction Amount (Optional)") },
                            placeholder = { Text("e.g. 150.00") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                    "CONTACT" -> {
                        OutlinedTextField(
                            value = contactName,
                            onValueChange = { viewModel.contactName.value = it },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = contactPhone,
                            onValueChange = { viewModel.contactPhone.value = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = contactEmail,
                            onValueChange = { viewModel.contactEmail.value = it },
                            label = { Text("Email Address") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = contactOrg,
                            onValueChange = { viewModel.contactOrg.value = it },
                            label = { Text("Organization / Company") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Step 3: Choose QrStyle Option Coordinates
        Text(
            text = "1. Choose Coordinate Geometry",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.onBackground
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
                    QrStyle.CLASSIC -> "Classic Grid"
                    QrStyle.ROUNDED_DOT -> "Fluid Pebbles"
                    QrStyle.ROCK -> "Chiseled Gem"
                }
                val buttonTag = when (style) {
                    QrStyle.CLASSIC -> "qr_style_classic"
                    QrStyle.ROUNDED_DOT -> "qr_style_pebble"
                    QrStyle.ROCK -> "qr_style_crystal"
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSel) {
                                if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary
                            } else {
                                if (isMaterial10Enabled) Color.Black.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
                            }
                        )
                        .border(
                            1.dp,
                            if (isSel) {
                                Color.Transparent
                            } else {
                                if (isMaterial10Enabled) Color(0xFF00FFCC).copy(alpha = 0.25f) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f)
                            },
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { viewModel.genStyle.value = style }
                        .testTag(buttonTag),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (isSel) {
                            if (isMaterial10Enabled) Color(0xFF0B0C0E) else MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onBackground
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Step 2: Choose brand overlay shield
        Text(
            text = "2. Customize Branding Shield Logo",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.onBackground
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val logoOptions = listOf(
                "NONE" to "Direct Classic",
                "CRYSTAL" to "Chiseled Gem",
                "SPARK" to "Star Spark",
                "DIAMOND" to "Cyber Diamond"
            )
            logoOptions.forEach { (option, label) ->
                val isSel = activeEmbedLogo == option
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSel) {
                                if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary
                            } else {
                                if (isMaterial10Enabled) Color.Black.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
                            }
                        )
                        .border(
                            1.dp,
                            if (isSel) {
                                Color.Transparent
                            } else {
                                if (isMaterial10Enabled) Color(0xFF00FFCC).copy(alpha = 0.25f) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f)
                            },
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { viewModel.embedLogo.value = option }
                        .testTag("logo_style_$option"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = if (isSel) {
                            if (isMaterial10Enabled) Color(0xFF0B0C0E) else MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onBackground
                        },
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Step 4: Color Customizer (Foreground and Background)
        RockFacetedCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = if (isMaterial10Enabled) Color(0xFF15181F) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            borderColor = if (isMaterial10Enabled) Color(0xFF00FFCC).copy(alpha = 0.4f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "Material 10 Cosmic Dye",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Pick active neon resonators or type hex parameters below",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Presets Layout
                Text(
                    text = "A. Material 10 Crystal Presets",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val gemThemes = listOf(
                        Triple("Liquid Cyan", "#00FFCC", "#0B0C0E"),
                        Triple("Laser Amethyst", "#CC33FF", "#0A0512"),
                        Triple("Pyrite Aura", "#EAA21D", "#15181F"),
                        Triple("Electric Jade", "#00FF66", "#051A0D"),
                        Triple("Garnet Plasma", "#FF3366", "#150207"),
                        Triple("Ice Pearl", "#1A73E8", "#FFFFFF"),
                        Triple("Standard High Contrast", "#0A0A0A", "#FFFFFF")
                    )

                    gemThemes.forEach { (name, fg, bg) ->
                        val isThemeActive = genFgColor.equals(fg, ignoreCase = true) && genBgColor.equals(bg, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isThemeActive) {
                                        if (isMaterial10Enabled) Color(0xFF00FFCC).copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    } else {
                                        MaterialTheme.colorScheme.background.copy(alpha = 0.4f)
                                    }
                                )
                                .border(
                                    width = if (isThemeActive) 2.dp else 1.dp,
                                    color = if (isThemeActive) {
                                        if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    viewModel.genFgColor.value = fg
                                    viewModel.genBgColor.value = bg
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy((-4).dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(ChiseledOctagonShape)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color(android.graphics.Color.parseColor(fg)))
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(ChiseledOctagonShape)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color(android.graphics.Color.parseColor(bg)))
                                                .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f), ChiseledOctagonShape)
                                        )
                                    }
                                }
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isThemeActive) {
                                        if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onBackground
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons row (Invert & Resonate)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val temp = genFgColor
                            viewModel.genFgColor.value = genBgColor
                            viewModel.genBgColor.value = temp
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isMaterial10Enabled) Color(0xFF00FFCC).copy(alpha = 0.4f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Autorenew,
                            contentDescription = "Swap colors",
                            modifier = Modifier.size(16.dp),
                            tint = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Invert Colors", 
                            fontSize = 11.sp, 
                            fontWeight = FontWeight.Bold,
                            color = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            val randomFg = listOf("#00FFCC", "#CC33FF", "#EAA21D", "#00FF66", "#FF3366", "#1A73E8", "#0A0A0A").random()
                            val randomBg = listOf("#0B0C0E", "#0A0512", "#15181F", "#051A0D", "#FFFFFF", "#F4F6F9").random()
                            viewModel.genFgColor.value = randomFg
                            viewModel.genBgColor.value = randomBg
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isMaterial10Enabled) Color(0xFF00FFCC).copy(alpha = 0.4f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Casino,
                            contentDescription = "Randomize colors",
                            modifier = Modifier.size(16.dp),
                            tint = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Resonate Presets", 
                            fontSize = 11.sp, 
                            fontWeight = FontWeight.Bold,
                            color = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Custom Text inputs for Custom Fg Hex with real color preview circle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    var customFgHex by remember { mutableStateOf(genFgColor) }
                    LaunchedEffect(genFgColor) {
                        customFgHex = genFgColor
                    }
                    OutlinedTextField(
                        value = customFgHex,
                        onValueChange = { newValue ->
                            val cleanVal = newValue.trim()
                            customFgHex = cleanVal
                            if (cleanVal.length == 7 && cleanVal.startsWith("#")) {
                                try {
                                    android.graphics.Color.parseColor(cleanVal)
                                    viewModel.genFgColor.value = cleanVal
                                } catch (e: Exception) {
                                    // ignore until valid
                                }
                            }
                        },
                        label = { Text("Fore Hex") },
                        placeholder = { Text("#00FFCC") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary
                        ),
                        leadingIcon = {
                            val sideColor = try { Color(android.graphics.Color.parseColor(customFgHex)) } catch (e: Exception) { Color.Gray }
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(ChiseledOctagonShape)
                                    .background(sideColor)
                                    .border(1.dp, Color.White.copy(alpha = 0.2f), ChiseledOctagonShape)
                            )
                        }
                    )

                    var customBgHex by remember { mutableStateOf(genBgColor) }
                    LaunchedEffect(genBgColor) {
                        customBgHex = genBgColor
                    }
                    OutlinedTextField(
                        value = customBgHex,
                        onValueChange = { newValue ->
                            val cleanVal = newValue.trim()
                            customBgHex = cleanVal
                            if (cleanVal.length == 7 && cleanVal.startsWith("#")) {
                                try {
                                    android.graphics.Color.parseColor(cleanVal)
                                    viewModel.genBgColor.value = cleanVal
                                } catch (e: Exception) {
                                    // ignore until valid
                                }
                            }
                        },
                        label = { Text("Back Hex") },
                        placeholder = { Text("#0B0C0E") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary
                        ),
                        leadingIcon = {
                            val sideColor = try { Color(android.graphics.Color.parseColor(customBgHex)) } catch (e: Exception) { Color.Gray }
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(ChiseledOctagonShape)
                                    .background(sideColor)
                                    .border(1.dp, Color.White.copy(alpha = 0.2f), ChiseledOctagonShape)
                            )
                        }
                    )
                }

                // Low Contrast Warner indicator block
                val computedContrast = remember(genFgColor, genBgColor) {
                    try {
                        val fg = android.graphics.Color.parseColor(genFgColor)
                        val bg = android.graphics.Color.parseColor(genBgColor)
                        val fgColor = Color(fg)
                        val bgColor = Color(bg)
                        val fgLum = 0.299f * fgColor.red + 0.587f * fgColor.green + 0.114f * fgColor.blue
                        val bgLum = 0.299f * bgColor.red + 0.587f * bgColor.green + 0.114f * bgColor.blue
                        Math.abs(fgLum - bgLum)
                    } catch (e: Exception) {
                        1.0f
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (computedContrast < 0.22f) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Low Mineral Contrast: Shades are too close. Readjust colors for flawless QR scanning.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isMaterial10Enabled) {
                                    Color(0xFF00FFCC).copy(alpha = 0.08f)
                                } else {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                }
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Geological Contrast Verified: Guaranteed bulletproof scanning speed.",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Step 5: Live QR Rendering output with Material 10 3D emission, glowing borders, and holographic sweep
        val animationStateScale by animateFloatAsState(
            targetValue = if (generatedBitmap != null) 1.0f else 0.85f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
            label = "qr_scale_pop"
        )
        val qrAlphaState by animateFloatAsState(
            targetValue = if (generatedBitmap != null) 1.0f else 0.0f,
            animationSpec = tween(500),
            label = "qr_alpha_fade"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = animationStateScale
                    scaleY = animationStateScale
                    alpha = qrAlphaState
                }
                .background(
                    if (isMaterial10Enabled && generatedBitmap != null) {
                        val neonColor = try {
                            Color(android.graphics.Color.parseColor(genFgColor))
                        } catch (e: Exception) {
                            Color(0xFF00FFCC)
                        }
                        Brush.radialGradient(
                            colors = listOf(neonColor.copy(alpha = 0.12f * pulsingAuraAlpha), Color.Transparent)
                        )
                    } else {
                        Brush.linearGradient(colors = listOf(Color.Transparent, Color.Transparent))
                    }
                )
        ) {
            RockFacetedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("qr_code_viewer"),
                backgroundColor = if (isMaterial10Enabled) Color(0xFF0B0C0E) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                borderColor = if (isMaterial10Enabled) Color(0xFF00FFCC).copy(alpha = 0.35f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (generatedBitmap != null) {
                        val finalBgCol = try { Color(android.graphics.Color.parseColor(genBgColor)) } catch (e: Exception) { Color.White }
                        val finalFgCol = try { Color(android.graphics.Color.parseColor(genFgColor)) } catch (e: Exception) { Color.Black }
                        
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(220.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .border(8.dp, finalBgCol, RoundedCornerShape(24.dp))
                                .background(finalBgCol)
                                .padding(12.dp)
                        ) {
                            Image(
                                bitmap = generatedBitmap!!.asImageBitmap(),
                                contentDescription = "Live code generation template",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("qr_code_image_preview")
                            )
 
                            // Holographic dynamic sweeping laser lines
                            if (isMaterial10Enabled) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val laserY = size.height * laserSweepValue
                                    drawLine(
                                        brush = Brush.linearGradient(
                                            colors = listOf(Color.Transparent, finalFgCol.copy(alpha = 0.7f), Color.Transparent)
                                        ),
                                        start = Offset(0f, laserY),
                                        end = Offset(size.width, laserY),
                                        strokeWidth = 6f
                                    )
                                    // Add soft secondary laser echo
                                    val echoY = size.height * (laserSweepValue - 0.08f)
                                    drawLine(
                                        brush = Brush.linearGradient(
                                            colors = listOf(Color.Transparent, finalFgCol.copy(alpha = 0.25f), Color.Transparent)
                                        ),
                                        start = Offset(0f, echoY),
                                        end = Offset(size.width, echoY),
                                        strokeWidth = 3f
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (isMaterial10Enabled) "🔮 MATERIAL 10 RESONATOR COORDS READY" else "Real-time Styled QR Ready",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            color = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary
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
                                    Toast.makeText(context, "Copied content payload", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (isMaterial10Enabled) Color(0xFF00FFCC).copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CopyAll,
                                    contentDescription = "Copy QR payload",
                                    tint = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Copy", 
                                    fontWeight = FontWeight.Bold,
                                    color = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.onBackground
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    generatedBitmap?.let { bitmap ->
                                        ShareUtils.shareBitmap(context, bitmap)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("qr_share_image_button"),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (isMaterial10Enabled) Color(0xFF00FFCC).copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share QR code image",
                                    tint = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Share", 
                                    fontWeight = FontWeight.Bold,
                                    color = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary
                                )
                            }

                            Button(
                                onClick = {
                                    viewModel.saveGeneratedCodeInHistory()
                                    Toast.makeText(context, "Saved to history library", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("qr_generate_save_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary,
                                    contentColor = if (isMaterial10Enabled) Color(0xFF0C0E14) else MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Save, 
                                    contentDescription = "Save template"
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(if (isMaterial10Enabled) Color.Black.copy(alpha = 0.4f) else MaterialTheme.colorScheme.background)
                                .border(
                                    width = 1.dp,
                                    color = if (isMaterial10Enabled) Color(0xFF00FFCC).copy(alpha = 0.25f) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(24.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.QrCode,
                                    contentDescription = "Draft setup icon",
                                    tint = if (isMaterial10Enabled) Color(0xFF00FFCC).copy(alpha = 0.4f) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "Input text or URLs above.",
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(horizontal = 20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ------------------ HISTORY COMPOSABLE ------------------
@Composable
fun HistoryScreen(viewModel: QrViewModel, onSettingsClick: () -> Unit) {
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
            RockChiseledHeader(
                title = "Locker History",
                subtitle = "Manage scanned/built coordinates",
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            if (historyRecords.isNotEmpty()) {
                IconButton(
                    onClick = {
                        viewModel.clearAllHistory()
                        Toast.makeText(context, "Cleared history", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.glassTouchFeedback {
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
                Spacer(modifier = Modifier.width(4.dp))
            }
            IconButton(
                onClick = onSettingsClick,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ),
                modifier = Modifier.glassTouchFeedback { onSettingsClick() }
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.primary
                )
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
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconToggleButton(
                checked = isOnlyFavorites,
                onCheckedChange = { viewModel.isOnlyFavorites.value = it },
                modifier = Modifier
                    .size(48.dp)
                    .liquidGlass(
                        shape = RoundedCornerShape(12.dp),
                        bgAlpha = if (isOnlyFavorites) 0.35f else 0.18f,
                        borderAlphaStart = if (isOnlyFavorites) 0.6f else 0.3f
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
                    val colors = (record.customColorHex ?: "#0A0A0A").split("|")
                    val fgColorHex = colors.getOrNull(0) ?: "#0A0A0A"
                    val bgColorHex = colors.getOrNull(1) ?: "#FFFFFF"
                    val bmap: Bitmap? = remember(record.content) {
                        QrCodeGenerator.generateQrCode(
                            content = record.content,
                            foregroundHexColor = fgColorHex,
                            backgroundHexColor = bgColorHex,
                            style = QrStyle.ROUNDED_DOT
                        )
                    }

                    if (bmap != null) {
                        val finalBgCol = try { Color(android.graphics.Color.parseColor(bgColorHex)) } catch (e: Exception) { Color.White }
                        Image(
                            bitmap = bmap.asImageBitmap(),
                            contentDescription = "Regenerated code",
                            modifier = Modifier
                                .size(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(finalBgCol)
                                .border(4.dp, finalBgCol, RoundedCornerShape(12.dp))
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
                                Toast.makeText(context, "Copied payload text", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy text")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy Code")
                        }

                        if (bmap != null) {
                            OutlinedButton(
                                onClick = {
                                    ShareUtils.shareBitmap(context, bmap)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Share Image")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share Image")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

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
                                context.startActivity(Intent.createChooser(intent, "Share text payload"))
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            imageVector = if (isWeb) Icons.Default.OpenInBrowser else Icons.Default.Send,
                            contentDescription = "Action detail"
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isWeb) "Browse Link" else "Share Plain Text")
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
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .glassTouchFeedback { onSelectCard() },
        backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier
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

    val activePrimary = MaterialTheme.colorScheme.primary
    val activeTertiary = MaterialTheme.colorScheme.tertiary
    val activeBackground = MaterialTheme.colorScheme.background

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
            .background(activeBackground) // Volcanic Obsidian Black Background
            .rockFractureBackground(color = activePrimary, alpha = 0.15f), // Shaded mineral fissures
        contentAlignment = Alignment.Center
    ) {
        // Crystalline ambient back-glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(activePrimary.copy(alpha = 0.22f * gemGlowAlpha), Color.Transparent),
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
                            colors = listOf(activeTertiary, activePrimary, activeTertiary)
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
                                .clip(ChiseledOctagonShape)
                                .background(activePrimary)
                                .padding(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(ChiseledOctagonShape)
                                    .background(activeBackground)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(ChiseledOctagonShape)
                                .background(activeTertiary)
                                .padding(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(ChiseledOctagonShape)
                                    .background(activeBackground)
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
                                .clip(ChiseledOctagonShape)
                                .background(activePrimary)
                                .padding(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(ChiseledOctagonShape)
                                    .background(activeBackground)
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(modifier = Modifier.size(6.dp).clip(ChiseledOctagonShape).background(Color.White))
                            Box(modifier = Modifier.size(6.dp).clip(ChiseledOctagonShape).background(activePrimary))
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
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 4.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "SECURE  •  OFFLINE  •  INSTANT",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = activePrimary,
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
                                    colors = listOf(Color.Transparent, activePrimary, activeTertiary, Color.Transparent)
                                )
                            )
                    )
                }
            }
        }
    }
}

/**
 * A beautiful, futuristic, highly polished settings and about screen with glass card theme options.
 */
@Composable
fun SettingsDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    viewModel: QrViewModel
) {
    if (!showDialog) return

    val themeMode by viewModel.themeMode.collectAsState()
    val dynamicColorEnabled by viewModel.dynamicColorEnabled.collectAsState()
    val colorPreset by viewModel.colorPreset.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "SETTINGS & CO",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        letterSpacing = 1.5.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.glassTouchFeedback { onDismiss() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close dialog",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                // Line Separator
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                )

                // Section 1: Themes
                Text(
                    text = "THEME SETTINGS",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val modes = listOf(
                        Triple("LIGHT", "Light", Icons.Default.LightMode),
                        Triple("DARK", "Dark", Icons.Default.DarkMode),
                        Triple("SYSTEM", "System", Icons.Default.SettingsSuggest)
                    )

                    modes.forEach { (mode, label, icon) ->
                        val isSelected = themeMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .glassTouchFeedback {
                                    viewModel.setThemeMode(mode)
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 2: Dynamic Colors (Material You)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Dynamic Colors",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Futuristic Material You colors",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    Switch(
                        checked = dynamicColorEnabled,
                        onCheckedChange = { viewModel.setDynamicColorEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                        )
                    )
                }

                if (!dynamicColorEnabled) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "LIQUID GLASS PRESET COLOR",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val presets = listOf(
                            "MIDNIGHT" to Triple("Midnight", com.example.ui.theme.MidnightPrimary, com.example.ui.theme.MidnightSecondary),
                            "ARCTIC" to Triple("Arctic", com.example.ui.theme.ArcticPrimary, com.example.ui.theme.ArcticSecondary),
                            "OCEAN" to Triple("Ocean", com.example.ui.theme.OceanPrimary, com.example.ui.theme.OceanSecondary),
                            "AURORA" to Triple("Aurora", com.example.ui.theme.AuroraPrimary, com.example.ui.theme.AuroraSecondary),
                            "EMERALD" to Triple("Emerald", com.example.ui.theme.EmeraldPrimary, com.example.ui.theme.EmeraldSecondary)
                        )

                        presets.forEach { (key, info) ->
                            val (name, col1, col2) = info
                            val isSel = colorPreset == key
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSel) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                                    )
                                    .border(
                                        width = if (isSel) 1.5.dp else 1.dp,
                                        brush = Brush.linearGradient(
                                            colors = if (isSel) listOf(col1, col2) else listOf(col1.copy(alpha = 0.3f), col2.copy(alpha = 0.1f))
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .glassTouchFeedback {
                                        viewModel.setColorPreset(key)
                                    }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    // Circular color representation bubble
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.horizontalGradient(
                                                    colors = listOf(col1, col2)
                                                )
                                            )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 9.sp,
                                        color = if (isSel) col1 else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Line Separator
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .height(0.5.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                )

                // Section 3: About
                Text(
                    text = "ABOUT THE APP",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Start).padding(bottom = 6.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                        .border(
                            width = 0.5.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(14.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Rock QR Engine Pro",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Offline-first, tactile, metamorphic coordinates scanning & building toolkit.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Developer Badge: Created by @sayanthRock
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Created by @sayanthRock",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Done Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .glassTouchFeedback { onDismiss() },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = "DONE",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
