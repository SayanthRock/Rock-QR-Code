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
import androidx.compose.ui.draw.blur
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
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
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

            // Real-time custom glassmorphism & blur parameters
            val glassBlurVal by viewModel.glassBlurRadius.collectAsState()
            val glassOpacityVal by viewModel.glassOpacity.collectAsState()
            val glassBorderThicknessVal by viewModel.glassBorderThickness.collectAsState()
            val glassGlowEnabledVal by viewModel.glassGlowEnabled.collectAsState()

            // Force high-fidelity dark neon theme across dedicated aesthetic UI
            val useDarkTheme = true

            MyApplicationTheme(
                darkTheme = useDarkTheme,
                dynamicColor = dynamicColorEnabled,
                colorPresetName = colorPreset
            ) {
                // Look up active dynamic preset color specifications
                val activePresetDetails = com.example.ui.components.AndroidPresetsInfo.find {
                    it.key == colorPreset.uppercase()
                } ?: com.example.ui.components.AndroidPresetsInfo[0]

                val animatedGlassPrimary by androidx.compose.animation.animateColorAsState(
                    targetValue = activePresetDetails.primaryColor,
                    animationSpec = androidx.compose.animation.core.tween(500),
                    label = "glass_primary_fade"
                )
                val animatedGlassSecondary by androidx.compose.animation.animateColorAsState(
                    targetValue = activePresetDetails.secondaryColor,
                    animationSpec = androidx.compose.animation.core.tween(500),
                    label = "glass_secondary_fade"
                )

                val glassConfig = com.example.ui.theme.LiquidGlassThemeConfig(
                    primaryColor = animatedGlassPrimary,
                    secondaryColor = animatedGlassSecondary,
                    glassBlur = glassBlurVal.dp,
                    glassOpacity = glassOpacityVal,
                    borderAlphaStart = if (glassGlowEnabledVal) 0.45f else 0.15f,
                    borderAlphaEnd = if (glassGlowEnabledVal) 0.15f else 0.05f,
                    isGlowEnabled = glassGlowEnabledVal,
                    borderThickness = glassBorderThicknessVal.dp
                )

                com.example.ui.theme.LiquidGlassThemeProvider(config = glassConfig) {
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
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QrMainDashboard(viewModel: QrViewModel = viewModel()) {
    val activeTab by viewModel.activeTab.collectAsState()
    // Force high-fidelity dark neon theme across dedicated aesthetic UI
    val useDarkTheme = true
    
    val context = LocalContext.current
    var showSettingsDialog by remember { mutableStateOf(false) }
    val toastMessage by viewModel.toastEvent.collectAsState()

    val bgPhotoUri by viewModel.bgPhotoUri.collectAsState()
    val bgPhotoBlurRadius by viewModel.bgPhotoBlurRadius.collectAsState()
    val bgPhotoEnabled by viewModel.bgPhotoEnabled.collectAsState()
    val bgPhotoBlurEnabled by viewModel.bgPhotoBlurEnabled.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        // Dynamic floating liquid orbit backdrop
        LiquidGlassBackground(
            useDarkTheme = useDarkTheme,
            bgPhotoUri = bgPhotoUri,
            bgPhotoBlurRadius = bgPhotoBlurRadius,
            bgPhotoEnabled = bgPhotoEnabled,
            bgPhotoBlurEnabled = bgPhotoBlurEnabled
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            bottomBar = {}, // Empty bottomBar: using our primary fixed glass floating deck instead for streamlined visuals
            contentWindowInsets = WindowInsets.safeDrawing
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                color = Color.Transparent
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
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

                    // Draggable or fixed-position floating action bar (switches views & cycles themes)
                    DraggableFloatingActionBar(
                        viewModel = viewModel,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                    )
                }

                SettingsDialog(
                    showDialog = showSettingsDialog,
                    onDismiss = { showSettingsDialog = false },
                    viewModel = viewModel
                )
            }
        }

        // Custom toast overlay floating above all content
        com.example.ui.components.CustomToastOverlay(
            toastMessage = toastMessage,
            onDismiss = { viewModel.clearToast() }
        )
    }
}

// ------------------ SCANNER COMPOSABLE ------------------
@Composable
fun ScanScreen(viewModel: QrViewModel, onSettingsClick: () -> Unit) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scannedText by viewModel.scannedText.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()

    // Helper to find the host activity for rationale checking
    fun android.content.Context.findActivity(): android.app.Activity? {
        var currentContext = this
        while (currentContext is android.content.ContextWrapper) {
            if (currentContext is android.app.Activity) {
                return currentContext
            }
            currentContext = currentContext.baseContext
        }
        return null
    }

    val sharedPrefs = remember {
        context.getSharedPreferences("rock_qr_settings", android.content.Context.MODE_PRIVATE)
    }

    // Runtime Permission Handler State Engine
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var permissionRequestedOnce by remember {
        mutableStateOf(sharedPrefs.getBoolean("camera_permission_requested", false))
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
            sharedPrefs.edit().putBoolean("camera_permission_requested", true).apply()
            permissionRequestedOnce = true
        }
    )

    // Automatically check and refresh permission state when user returns from Settings
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                hasCameraPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Show scanned detail bottom dialog/sheet when detected
    var showDetailDialog by remember { mutableStateOf(false) }
    LaunchedEffect(scannedText) {
        if (scannedText != null) {
            com.example.utils.HapticUtils.vibratePattern(context, longArrayOf(0, 80, 50, 80))
            showDetailDialog = true
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // --- 1. FULL SCREEN CAMERA PREVIEW OR GRACEFUL PERMISSION HANDSHAKE CARD ---
        CameraPermissionGate(
            onPermissionGranted = {
                hasCameraPermission = true
            },
            onPermissionStatusChanged = { granted ->
                hasCameraPermission = granted
            },
            onShowToast = { msg, type -> viewModel.showToast(msg, type) },
            onShowTestPayloadPrompt = null
        ) {
            if (isScanning) {
                CameraPreview(
                    onQrScanned = { text ->
                        viewModel.setScannedText(text)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0F1115)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VideocamOff,
                                contentDescription = "Camera Suspended",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Camera Lens Offline",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "The physical scanner is currently suspended. Flip the toggle switch above to activate your lens, and capture QR codes instantly.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.widthIn(max = 300.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.setScanning(true) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "Activate lens",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Activate Lens", fontWeight = FontWeight.Bold)
                        }
                    }
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
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "AURA PARSER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.5.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Chiseled Lens",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isScanning) Color(0xFF00FFCC) else Color.Gray)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (isScanning) "ON" else "OFF",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isScanning) Color(0xFF00FFCC) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Switch(
                            checked = isScanning,
                            onCheckedChange = { viewModel.setScanning(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF00FFCC),
                                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                uncheckedThumbColor = Color.LightGray,
                                uncheckedTrackColor = Color.DarkGray
                            ),
                            modifier = Modifier.graphicsLayer(scaleX = 0.85f, scaleY = 0.85f)
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

        // Removed Floating Glass Mock Parser Panel
    }

    // SCANNED DIALOG RESULT VIEWER (Uses premium, high-blur Liquid Glass custom container card)
    if (showDetailDialog && scannedText != null) {
        Dialog(onDismissRequest = {
            showDetailDialog = false
            viewModel.resetScanner()
        }) {
            LiquidGlassScannerResultCard(
                scannedText = scannedText ?: "",
                onDismiss = {
                    showDetailDialog = false
                    viewModel.resetScanner()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                onShowToast = { msg, type -> viewModel.showToast(msg, type) }
            )
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
    val errorCorrectionLevel by viewModel.errorCorrectionLevel.collectAsState()
    val generatedBitmap by viewModel.generatedBitmap.collectAsState()

    val bgPhotoUri by viewModel.bgPhotoUri.collectAsState()
    val bgPhotoBlurRadius by viewModel.bgPhotoBlurRadius.collectAsState()
    val bgPhotoEnabled by viewModel.bgPhotoEnabled.collectAsState()
    val bgPhotoBlurEnabled by viewModel.bgPhotoBlurEnabled.collectAsState()

    // Parameters
    val plainText by viewModel.plainText.collectAsState()
    val urlLink by viewModel.urlLink.collectAsState()
    val urlValidationError by viewModel.urlValidationError.collectAsState()
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

    val customLogoUri by viewModel.customLogoUri.collectAsState()
    val customLogoBitmap by viewModel.customLogoBitmap.collectAsState()
    val customLogoScale by viewModel.customLogoScale.collectAsState()
    val customLogoShape by viewModel.customLogoShape.collectAsState()

    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setCustomLogoUri(uri.toString())
            viewModel.embedLogo.value = "CUSTOM_IMAGE"
            viewModel.showToast("Custom overlay logo loaded successfully!", com.example.viewmodel.CustomToastType.SUCCESS)
        }
    }

    // Active design mode: "STANDARD" or the futuristic "MATERIAL_10"
    var isMaterial10Enabled by remember { mutableStateOf(true) }
    var exportResolution by remember { mutableStateOf("1024") } // "512", "1024", "2048"
    var exportFormat by remember { mutableStateOf("PNG") } // "PNG", "JPEG"

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
                .liquidGlass(RoundedCornerShape(12.dp))
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

        // Step 2: Responsive input fields inside dynamic 'Liquid Glass' card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("generator_form_liquid_glass_card")
                .liquidGlass(
                    shape = RoundedCornerShape(20.dp)
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
                            isError = urlValidationError != null,
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = null,
                                    tint = if (isMaterial10Enabled) {
                                        if (urlValidationError != null) Color(0xFFFF5555) else Color(0xFF00FFCC)
                                    } else {
                                        if (urlValidationError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                    }
                                )
                            },
                            supportingText = if (urlValidationError != null) {
                                {
                                    Text(
                                        text = urlValidationError!!,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isMaterial10Enabled) Color(0xFFFF5555) else MaterialTheme.colorScheme.error
                                    )
                                }
                            } else null,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                                errorBorderColor = if (isMaterial10Enabled) Color(0xFFFF5555) else MaterialTheme.colorScheme.error
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
                                if (isMaterial10Enabled) Color.Black.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.28f)
                            }
                        )
                        .border(
                            1.dp,
                            if (isSel) {
                                Color.Transparent
                            } else {
                                if (isMaterial10Enabled) Color(0xFF00FFCC).copy(alpha = 0.25f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
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
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val row1 = listOf(
                "NONE" to "Direct Classic",
                "CRYSTAL" to "Chiseled Gem",
                "SPARK" to "Star Spark"
            )
            val row2 = listOf(
                "DIAMOND" to "Cyber Diamond",
                "HEART" to "Love Heart",
                "STAR" to "Magic Star"
            )
            val row3 = listOf(
                "CUSTOM_IMAGE" to "Custom Device Logo 🖼️"
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row1.forEach { (option, label) ->
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
                                    if (isMaterial10Enabled) Color.Black.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.28f)
                                }
                            )
                            .border(
                                1.dp,
                                if (isSel) {
                                    Color.Transparent
                                } else {
                                    if (isMaterial10Enabled) Color(0xFF00FFCC).copy(alpha = 0.25f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
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
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                            },
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row2.forEach { (option, label) ->
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
                                    if (isMaterial10Enabled) Color.Black.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.28f)
                                }
                            )
                            .border(
                                1.dp,
                                if (isSel) {
                                    Color.Transparent
                                } else {
                                    if (isMaterial10Enabled) Color(0xFF00FFCC).copy(alpha = 0.25f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
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
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                            },
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row3.forEach { (option, label) ->
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
                                    if (isMaterial10Enabled) Color.Black.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.28f)
                                }
                            )
                            .border(
                                1.dp,
                                if (isSel) {
                                    Color.Transparent
                                } else {
                                    if (isMaterial10Enabled) Color(0xFF00FFCC).copy(alpha = 0.25f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
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
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                            },
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        if (activeEmbedLogo == "CUSTOM_IMAGE") {
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isMaterial10Enabled) Color(0xFF15181F) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f)
                    )
                    .border(
                        1.dp,
                        if (isMaterial10Enabled) Color(0xFF00FFCC).copy(alpha = 0.15f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
                        RoundedCornerShape(14.dp)
                    )
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Upload Custom Logo",
                            tint = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "Central Custom Image Overlay",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Choose a clean PNG or JPG image from local files.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Image Preview box
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Black.copy(alpha = 0.25f))
                                .border(
                                    1.dp,
                                    if (isMaterial10Enabled) Color(0xFF00FFCC).copy(alpha = 0.3f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                    RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (customLogoUri != null) {
                                AsyncImage(
                                    model = customLogoUri,
                                    contentDescription = "Overlay logo thumbnail",
                                    modifier = Modifier.size(54.dp),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = "No Image Selected",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        // Select buttons
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Button(
                                onClick = { logoPickerLauncher.launch("image/*") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary,
                                    contentColor = if (isMaterial10Enabled) Color(0xFF0B0C0E) else MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoLibrary,
                                        contentDescription = "Pick logo icon",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (customLogoUri != null) "Choose Different Image" else "Select Local Image Logo",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            if (customLogoUri != null) {
                                Text(
                                    text = "Logo loaded into QR core. To ensure bulletproof scan readability, High (H) error correction is auto-enabling.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }

                    if (customLogoUri != null) {
                        Divider(
                            color = if (isMaterial10Enabled) Color(0xFF00FFCC).copy(alpha = 0.15f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        // 1. Clipping Frame Mask
                        Text(
                            text = "Clipping Frame Mask",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val shapes = listOf(
                                "ROUNDED" to "Rounded ▢",
                                "CIRCLE" to "Circular ◯",
                                "SQUARE" to "Sharp Square █"
                            )
                            shapes.forEach { (shape, name) ->
                                val isShapeSel = customLogoShape == shape
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isShapeSel) {
                                                if (isMaterial10Enabled) Color(0xFF00FFCC).copy(alpha = 0.25f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            } else {
                                                Color.Black.copy(alpha = 0.15f)
                                            }
                                        )
                                        .border(
                                            1.dp,
                                            if (isShapeSel) {
                                                if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary
                                            } else {
                                                Color.Transparent
                                            },
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { viewModel.setCustomLogoShape(shape) }
                                        .testTag("custom_logo_shape_$shape"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isShapeSel) {
                                            if (isMaterial10Enabled) Color(0xFF0B0C0E) else MaterialTheme.colorScheme.onPrimary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // 2. Custom Logo Scale Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Overlay Logo Scale: ${(customLogoScale * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { viewModel.setCustomLogoScale((customLogoScale - 0.01f).coerceIn(0.15f, 0.30f)) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Text("-", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary)
                                }
                                IconButton(
                                    onClick = { viewModel.setCustomLogoScale((customLogoScale + 0.01f).coerceIn(0.15f, 0.30f)) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Text("+", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary)
                                }
                            }
                        }

                        Slider(
                            value = customLogoScale,
                            onValueChange = { viewModel.setCustomLogoScale(it) },
                            valueRange = 0.15f..0.30f,
                            colors = SliderDefaults.colors(
                                thumbColor = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary,
                                activeTrackColor = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = if (isMaterial10Enabled) Color.Gray.copy(alpha = 0.2f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(24.dp)
                                .testTag("custom_logo_scale_slider")
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        var customLogoText by remember { mutableStateOf("") }
        LaunchedEffect(activeEmbedLogo) {
            if (activeEmbedLogo !in listOf("NONE", "CRYSTAL", "SPARK", "DIAMOND", "HEART", "STAR", "CUSTOM_IMAGE")) {
                customLogoText = activeEmbedLogo
            } else {
                customLogoText = ""
            }
        }

        OutlinedTextField(
            value = customLogoText,
            onValueChange = { newVal ->
                val trimmed = newVal.take(4) // Keeps center overlay small for scan rate
                customLogoText = trimmed
                if (trimmed.isNotEmpty()) {
                    viewModel.embedLogo.value = trimmed
                } else {
                    viewModel.embedLogo.value = "NONE"
                }
            },
            label = { Text("Custom Center Emblem (Emoji or Initials)") },
            placeholder = { Text("e.g. ❤️, 🚀, QR, ME") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary
            ),
            trailingIcon = {
                if (customLogoText.isNotEmpty()) {
                    IconButton(onClick = {
                        customLogoText = ""
                        viewModel.embedLogo.value = "NONE"
                    }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Clear Custom Logo")
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val emojiPresets = listOf("🔗", "🚀", "🦎", "⚡", "🔥", "🌟", "📱", "📧", "💎", "👾", "❤️")
            emojiPresets.forEach { emojiPreset ->
                val isSelected = activeEmbedLogo == emojiPreset
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isSelected) {
                                if (isMaterial10Enabled) Color(0xFF00FFCC).copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            } else {
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.25f)
                            }
                        )
                        .border(
                            1.dp,
                            if (isSelected) {
                                if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                            },
                            RoundedCornerShape(20.dp)
                        )
                        .clickable {
                            customLogoText = emojiPreset
                            viewModel.embedLogo.value = emojiPreset
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = emojiPreset, fontSize = 16.sp)
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

                // Foreground Customizer Swatches
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "B. Foreground Color Customizer Swatches",
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val fgSwatches = listOf(
                        "Obsidian" to "#0A0A0A",
                        "Cyan" to "#00FFCC",
                        "Violet" to "#CC33FF",
                        "Jade" to "#00FF66",
                        "Gold" to "#EAA21D",
                        "Garnet" to "#FF3366",
                        "Blue" to "#1A73E8",
                        "White" to "#FFFFFF"
                    )
                    fgSwatches.forEach { (name, hex) ->
                        val isMatched = genFgColor.equals(hex, ignoreCase = true)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.genFgColor.value = hex }
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(ChiseledOctagonShape)
                                    .background(Color(android.graphics.Color.parseColor(hex)))
                                    .border(
                                        width = if (isMatched) 2.5.dp else 1.dp,
                                        color = if (isMatched) {
                                            if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary
                                        } else {
                                            Color.White.copy(alpha = 0.25f)
                                        },
                                        shape = ChiseledOctagonShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isMatched) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = if (hex.equals("#FFFFFF", ignoreCase = true)) Color.Black else Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = if (isMatched) 1.0f else 0.6f),
                                fontWeight = if (isMatched) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // Background Customizer Swatches
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "C. Background Color Customizer Swatches",
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val bgSwatches = listOf(
                        "Cotton" to "#FFFFFF",
                        "Pitch" to "#0B0C0E",
                        "Velvet" to "#121316",
                        "Astral" to "#0A0512",
                        "Chamber" to "#051A0D",
                        "Clay" to "#FFFDF9",
                        "Slate" to "#15181F",
                        "Charcoal" to "#333333"
                    )
                    bgSwatches.forEach { (name, hex) ->
                        val isMatched = genBgColor.equals(hex, ignoreCase = true)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.genBgColor.value = hex }
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(ChiseledOctagonShape)
                                    .background(Color(android.graphics.Color.parseColor(hex)))
                                    .border(
                                        width = if (isMatched) 2.5.dp else 1.dp,
                                        color = if (isMatched) {
                                            if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary
                                        } else {
                                            Color.White.copy(alpha = 0.25f)
                                        },
                                        shape = ChiseledOctagonShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isMatched) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = if (hex.equals("#FFFFFF", ignoreCase = true)) Color.Black else Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = if (isMatched) 1.0f else 0.6f),
                                fontWeight = if (isMatched) FontWeight.Bold else FontWeight.Normal
                            )
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

        Spacer(modifier = Modifier.height(16.dp))

        // Step 5: Error Correction Level Control
        Text(
            text = "5. Adjust Error Correction Security",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Choose level of redundancy to survive surface scratches or heavy center logo embedding.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val ecOptions = listOf(
                ErrorCorrectionLevel.L to "Low (7%)",
                ErrorCorrectionLevel.M to "Medium (15%)",
                ErrorCorrectionLevel.Q to "Quarter (25%)",
                ErrorCorrectionLevel.H to "High (30%)"
            )
            ecOptions.forEach { (option, label) ->
                val isSel = errorCorrectionLevel == option
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSel) {
                                if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary
                            } else {
                                if (isMaterial10Enabled) Color.Black.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.28f)
                            }
                        )
                        .border(
                            1.dp,
                            if (isSel) {
                                Color.Transparent
                            } else {
                                if (isMaterial10Enabled) Color(0xFF00FFCC).copy(alpha = 0.25f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                            },
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { viewModel.errorCorrectionLevel.value = option }
                        .testTag("error_correction_$option"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = if (isSel) {
                            if (isMaterial10Enabled) Color(0xFF0B0C0E) else MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                        },
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Informative card detailing correct level utilization
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isMaterial10Enabled) Color(0xFF15181F) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f)
                )
                .border(
                    1.dp,
                    if (isMaterial10Enabled) Color(0xFF00FFCC).copy(alpha = 0.15f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
                    RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val (ecTitle, ecDesc, ecColor) = when (errorCorrectionLevel) {
                    ErrorCorrectionLevel.L -> Triple(
                        "Level L (Low Redundancy)",
                        "Fewer data dots generated. Clearest aesthetic looks but cannot withstand much occlusion. Highly recommended for purely classical or plain minimalist text/URLs without any center branding.",
                        if (isMaterial10Enabled) Color(0xFF00FF66) else MaterialTheme.colorScheme.primary
                    )
                    ErrorCorrectionLevel.M -> Triple(
                        "Level M (Standard Balance)",
                        "A well-rounded classic setting that can withstand minor damage or debris. Ideal standard for business cards and flyers.",
                        if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary
                    )
                    ErrorCorrectionLevel.Q -> Triple(
                        "Level Q (Quarter Shield)",
                        "Strong 25% data reconstruction capability. Capable of surviving center logos or moderate surface degradation without scannability loss.",
                        if (isMaterial10Enabled) Color(0xFFCC33FF) else MaterialTheme.colorScheme.primary
                    )
                    ErrorCorrectionLevel.H -> Triple(
                        "Level H (High Shield Redundancy)",
                        "Ultimate 30% restoration. Crucial for heavy branding overlay emblems (e.g. Star Spark, Gemstone). Ensures scanning continues in hostile physical states.",
                        if (isMaterial10Enabled) Color(0xFFFF3366) else MaterialTheme.colorScheme.primary
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(ecColor)
                    )
                    Text(
                        text = ecTitle,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = ecColor
                    )
                }
                Text(
                    text = ecDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isMaterial10Enabled) Color(0xFFD1D5DB) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                    lineHeight = 15.sp
                )
            }
        }

        // Step 6: Generator Background Photo Suite
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "6. Generator & App Background Photo Suite",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Replace the abstract orbits with rich geological patterns, rock textures, or your own custom photos with full frost blur controls.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        val photoPickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                viewModel.setBgPhotoUri(uri.toString())
                viewModel.setBgPhotoEnabled(true)
                viewModel.showToast("Custom geological backplate applied!", com.example.viewmodel.CustomToastType.SUCCESS)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (isMaterial10Enabled) Color(0xFF15181F) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f)
                )
                .border(
                    1.dp,
                    if (isMaterial10Enabled) Color(0xFF00FFCC).copy(alpha = 0.15f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
                    RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Feature activation toggle switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Background Photo Mode",
                            tint = if (bgPhotoEnabled) {
                                if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            },
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Enable Custom Backplate",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Toggles background graphic overlay",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        }
                    }
                    Switch(
                        checked = bgPhotoEnabled,
                        onCheckedChange = { viewModel.setBgPhotoEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = if (isMaterial10Enabled) Color(0xFF0B0C0E) else Color.White,
                            checkedTrackColor = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.DarkGray.copy(alpha = 0.5f)
                        )
                    )
                }

                if (bgPhotoEnabled) {
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                    // 1. Photos Selectors Row (Presets + Gallery Picker)
                    Text(
                        text = "Select Background Image Preset:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val presets = listOf(
                            Triple("Cosmic Ore", "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=800&q=80", "🌌"),
                            Triple("Neon Agate", "https://images.unsplash.com/photo-1576016770956-debb63d90029?auto=format&fit=crop&w=800&q=80", "🍀"),
                            Triple("Volcanic Ruby", "https://images.unsplash.com/photo-1614728894747-a83421e2b9c9?auto=format&fit=crop&w=800&q=80", "🔥"),
                            Triple("Ice Glacier", "https://images.unsplash.com/photo-1506318137071-a8e063b4bec0?auto=format&fit=crop&w=800&q=80", "❄️"),
                            Triple("Jade Quartz", "https://images.unsplash.com/photo-1518531933037-91b2f5f229cc?auto=format&fit=crop&w=800&q=80", "🍃")
                        )

                        // A highly stylized custom picker item
                        val isCustomPicked = bgPhotoUri != null && !presets.any { it.second == bgPhotoUri }
                        Box(
                            modifier = Modifier
                                .size(width = 110.dp, height = 75.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isCustomPicked) {
                                        if (isMaterial10Enabled) Color(0xFF00FFCC).copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    } else {
                                        Color(0xFF22252C)
                                    }
                                )
                                .border(
                                    width = if (isCustomPicked) 2.dp else 1.dp,
                                    color = if (isCustomPicked) {
                                        if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary
                                    } else {
                                        Color.White.copy(alpha = 0.1f)
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { photoPickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = "Pick Custom Image",
                                    tint = if (isCustomPicked) {
                                        if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary
                                    } else {
                                        Color.White.copy(alpha = 0.70f)
                                    },
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isCustomPicked) "Custom Picked" else "Custom Gallery",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Presets
                        presets.forEach { (name, url, emoji) ->
                            val isSelected = bgPhotoUri == url
                            Box(
                                modifier = Modifier
                                    .size(width = 110.dp, height = 75.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF1E2127))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) {
                                            if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary
                                        } else {
                                            Color.White.copy(alpha = 0.1f)
                                        },
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        viewModel.setBgPhotoUri(url)
                                        viewModel.showToast("$name applied!", com.example.viewmodel.CustomToastType.INFO)
                                    }
                            ) {
                                // Background preview representation (Coil image or stylized overlay)
                                AsyncImage(
                                    model = url,
                                    contentDescription = name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                // Dark mask
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                                            )
                                        )
                                )
                                // Label + Emoji
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(6.dp),
                                    verticalArrangement = Arrangement.SpaceBetween,
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Text(text = emoji, fontSize = 16.sp)
                                    Text(
                                        text = name,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color.Black,
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Apply Gaussian Blur toggle switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.BlurOn,
                                contentDescription = "Gaussian Blur Effect",
                                tint = if (bgPhotoBlurEnabled) {
                                    if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                },
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "Apply Gaussian Blur",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "Blurs background to improve QR code contrast",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                )
                            }
                        }
                        Switch(
                            checked = bgPhotoBlurEnabled,
                            onCheckedChange = { 
                                viewModel.setBgPhotoBlurEnabled(it)
                                if (it) {
                                    viewModel.showToast("Gaussian blur enabled for better QR readability!", com.example.viewmodel.CustomToastType.SUCCESS)
                                } else {
                                    viewModel.showToast("Gaussian blur disabled.", com.example.viewmodel.CustomToastType.INFO)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = if (isMaterial10Enabled) Color(0xFF0B0C0E) else Color.White,
                                checkedTrackColor = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.DarkGray.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.testTag("bg_photo_blur_toggle")
                        )
                    }

                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                    // 2. Blur controls with + and - quick adjustments and full precision Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Backplate Frost Blur Radius:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                        )
                        Text(
                            text = if (bgPhotoBlurRadius < 0.2f) "0 dp (Sharp Clear)" else "${bgPhotoBlurRadius.toInt()} dp",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary
                        )
                    }

                    // Dynamic slider control
                    Slider(
                        value = bgPhotoBlurRadius,
                        onValueChange = { viewModel.setBgPhotoBlurRadius(it) },
                        valueRange = 0f..25f,
                        colors = SliderDefaults.colors(
                            thumbColor = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary,
                            activeTrackColor = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = Color.Gray.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Quick tactile "Add Blur / Remove Blur" controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Remove Blur completely (Sharp)
                        Button(
                            onClick = {
                                viewModel.setBgPhotoBlurRadius(0f)
                                viewModel.showToast("Background sharp mode active (0 dp blur)", com.example.viewmodel.CustomToastType.INFO)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isMaterial10Enabled) Color.Black.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onBackground
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (bgPhotoBlurRadius < 0.2f) {
                                    if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary
                                } else {
                                    Color.Transparent
                                }
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = "Remove Blur",
                                    modifier = Modifier.size(14.dp)
                                )
                                Text("Remove Blur", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Add soft blur minus
                        Button(
                            onClick = {
                                val current = (bgPhotoBlurRadius - 4f).coerceAtLeast(0f)
                                viewModel.setBgPhotoBlurRadius(current)
                            },
                            enabled = bgPhotoBlurRadius > 0f,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Black.copy(alpha = 0.15f),
                                contentColor = MaterialTheme.colorScheme.onBackground
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(0.5f)
                        ) {
                            Text("- Blur", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        // Add soft blur plus
                        Button(
                            onClick = {
                                val current = (bgPhotoBlurRadius + 4f).coerceAtMost(25f)
                                viewModel.setBgPhotoBlurRadius(current)
                            },
                            enabled = bgPhotoBlurRadius < 25f,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Black.copy(alpha = 0.15f),
                                contentColor = MaterialTheme.colorScheme.onBackground
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(0.5f)
                        ) {
                            Text("+ Blur", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        // Max frost blur
                        Button(
                            onClick = {
                                viewModel.setBgPhotoBlurRadius(16f)
                                viewModel.showToast("Deep glass frost applied (16 dp blur)", com.example.viewmodel.CustomToastType.INFO)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isMaterial10Enabled) Color.Black.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onBackground
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (kotlin.math.abs(bgPhotoBlurRadius - 16f) < 0.5f) {
                                    if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary
                                } else {
                                    Color.Transparent
                                }
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BlurOn,
                                    contentDescription = "Apply Frosted Blur",
                                    modifier = Modifier.size(14.dp)
                                )
                                Text("Frosted (16dp)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Step 5: Live QR Rendering output with Material 10 3D emission, glowing borders, and holographic sweep
        val animationStateScale by animateFloatAsState(
            targetValue = 1.0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
            label = "qr_scale_pop"
        )
        val qrAlphaState by animateFloatAsState(
            targetValue = 1.0f,
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
                        
                        // Header title for real-time renderer status
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary)
                            )
                            Text(
                                text = "REAL-TIME RENDERING ENGINE ACTIVE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.2.sp
                            )
                        }

                        // The Real-Time Unified Readability Simulator Preview
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.Black.copy(alpha = 0.45f))
                                .border(
                                    1.dp,
                                    if (isMaterial10Enabled) Color(0xFF00FFCC).copy(alpha = 0.15f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                    RoundedCornerShape(20.dp)
                                )
                        ) {
                            // 1. Live Ambient Simulated Background photo layer
                            if (bgPhotoEnabled && !bgPhotoUri.isNullOrEmpty()) {
                                AsyncImage(
                                    model = bgPhotoUri,
                                    contentDescription = "Simulated Background Photo",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .let {
                                            if (bgPhotoBlurEnabled && bgPhotoBlurRadius > 0.1f) {
                                                it.blur(bgPhotoBlurRadius.dp)
                                            } else {
                                                it
                                            }
                                        },
                                    contentScale = ContentScale.Crop
                                )
                                // Dark frosted mask to mimic the overall app look and optimize scanning
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.45f))
                                )
                            } else {
                                // Default abstract fluid neon gradient simulation
                                val baseGrad = if (isSystemInDarkTheme()) {
                                    listOf(Color(0xFF0D0F12), Color(0xFF1E2631))
                                } else {
                                    listOf(Color(0xFF1E2631), Color(0xFF0D0F12))
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Brush.linearGradient(baseGrad))
                                )
                            }
                            
                            // 2. The Solid Contrast Backplate QR Code Container
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(195.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .border(6.dp, finalBgCol, RoundedCornerShape(18.dp))
                                    .background(finalBgCol)
                                    .padding(10.dp)
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
                                                colors = listOf(Color.Transparent, finalFgCol.copy(alpha = 0.35f), Color.Transparent)
                                            ),
                                            start = Offset(0f, laserY),
                                            end = Offset(size.width, laserY),
                                            strokeWidth = 6f
                                        )
                                        // Soft secondary laser echo
                                        val echoY = size.height * (laserSweepValue - 0.08f)
                                        drawLine(
                                            brush = Brush.linearGradient(
                                                colors = listOf(Color.Transparent, finalFgCol.copy(alpha = 0.15f), Color.Transparent)
                                            ),
                                            start = Offset(0f, echoY),
                                            end = Offset(size.width, echoY),
                                            strokeWidth = 3f
                                        )
                                    }
                                }
                            }
                            
                            // 3. Floating Overlay badges with current active properties
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(8.dp)
                                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (bgPhotoEnabled) Icons.Default.BlurOn else Icons.Default.Image,
                                    contentDescription = "Background Status",
                                    tint = if (bgPhotoEnabled) Color(0xFF00FFCC) else Color.Gray,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = if (bgPhotoEnabled) {
                                        if (bgPhotoBlurEnabled) "${bgPhotoBlurRadius.toInt()}dp glass blur" else "sharp backdrop"
                                    } else {
                                        "standard mode"
                                    },
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            // Dynamic contrast checker badge
                            val (badgeColor, badgeLabel) = remember(bgPhotoEnabled, bgPhotoBlurEnabled, bgPhotoBlurRadius) {
                                when {
                                    !bgPhotoEnabled -> Color(0xFF00FFCC) to "Perfect 100%"
                                    !bgPhotoBlurEnabled -> Color(0xFFFFCC00) to "Scan Risk (No Blur)"
                                    bgPhotoBlurRadius < 6f -> Color(0xFF00D1FF) to "Normal 85%"
                                    else -> Color(0xFF00FFCC) to "Optimal 100%"
                                }
                            }
                            
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                                    .border(1.dp, badgeColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (badgeColor == Color(0xFFFFCC00)) Icons.Default.Warning else Icons.Default.CheckCircle,
                                    contentDescription = "Readability rating icon",
                                    tint = badgeColor,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "Contrast: $badgeLabel",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
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

                        // --- EXPORT CANVAS SETTINGS PANEL ---
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "EXPORT ENGINE PARAMETERS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            // Resolution Selectors Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Canvas Resolution",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val resolutions = listOf("512", "1024", "2048")
                                    resolutions.forEach { res ->
                                        val isSelected = exportResolution == res
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (isSelected) {
                                                        if (isMaterial10Enabled) Color(0xFF00FFCC).copy(alpha = 0.2f)
                                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                                    } else Color.Transparent
                                                )
                                                .border(
                                                    1.dp,
                                                    if (isSelected) {
                                                        if (isMaterial10Enabled) Color(0xFF00FFCC)
                                                        else MaterialTheme.colorScheme.primary
                                                    } else Color.White.copy(alpha = 0.12f),
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .clickable { exportResolution = res }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = if (res == "512") "512px" else if (res == "1024") "1024px (HD)" else "2048px (UHD)",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) {
                                                    if (isMaterial10Enabled) Color(0xFF00FFCC)
                                                    else MaterialTheme.colorScheme.primary
                                                } else Color.White.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Format Selectors Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "File Encoding",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf("PNG", "JPEG").forEach { fmt ->
                                        val isSelected = exportFormat == fmt
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (isSelected) {
                                                        if (isMaterial10Enabled) Color(0xFF00FFCC).copy(alpha = 0.2f)
                                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                                    } else Color.Transparent
                                                )
                                                .border(
                                                    1.dp,
                                                    if (isSelected) {
                                                        if (isMaterial10Enabled) Color(0xFF00FFCC)
                                                        else MaterialTheme.colorScheme.primary
                                                    } else Color.White.copy(alpha = 0.12f),
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .clickable { exportFormat = fmt }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = fmt,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) {
                                                    if (isMaterial10Enabled) Color(0xFF00FFCC)
                                                    else MaterialTheme.colorScheme.primary
                                                } else Color.White.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val content = viewModel.getFormattedContent()
                                    clipboardManager.setText(AnnotatedString(content))
                                    viewModel.showToast("Copied content payload", com.example.viewmodel.CustomToastType.SUCCESS)
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
                                    val size = exportResolution.toIntOrNull() ?: 1024
                                    val styleEnum = genStyle
                                    val content = viewModel.getFormattedContent()
                                    val highResBmap = QrCodeGenerator.generateQrCode(
                                        content = content,
                                        width = size,
                                        height = size,
                                        foregroundHexColor = genFgColor,
                                        backgroundHexColor = genBgColor,
                                        style = styleEnum,
                                        embedLogo = activeEmbedLogo,
                                        errorCorrection = errorCorrectionLevel,
                                        customLogoBitmap = customLogoBitmap,
                                        customLogoScale = customLogoScale,
                                        customLogoShape = customLogoShape
                                    )
                                    highResBmap?.let { bitmap ->
                                        val ext = if (exportFormat == "PNG") "png" else "jpg"
                                        ShareUtils.shareBitmap(
                                            context = context, 
                                            bitmap = bitmap, 
                                            fileName = "shared_qr_${size}.$ext",
                                            onShowToast = { msg, type -> viewModel.showToast(msg, type) }
                                        )
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
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.saveGeneratedCodeInHistory()
                                    viewModel.showToast("Saved to history library", com.example.viewmodel.CustomToastType.SUCCESS)
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

                            Button(
                                onClick = {
                                    val size = exportResolution.toIntOrNull() ?: 1024
                                    val styleEnum = genStyle
                                    val content = viewModel.getFormattedContent()
                                    val highResBmap = QrCodeGenerator.generateQrCode(
                                        content = content,
                                        width = size,
                                        height = size,
                                        foregroundHexColor = genFgColor,
                                        backgroundHexColor = genBgColor,
                                        style = styleEnum,
                                        embedLogo = activeEmbedLogo,
                                        errorCorrection = errorCorrectionLevel,
                                        customLogoBitmap = customLogoBitmap,
                                        customLogoScale = customLogoScale,
                                        customLogoShape = customLogoShape
                                    )
                                    highResBmap?.let { bitmap ->
                                        val prefix = if (isMaterial10Enabled) "Material10_QR_" else "Rock_QR_"
                                        ShareUtils.saveBitmapToGallery(
                                            context = context,
                                            bitmap = bitmap,
                                            displayName = prefix,
                                            isPng = (exportFormat == "PNG"),
                                            onShowToast = { msg, type -> viewModel.showToast(msg, type) }
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("qr_generate_download_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isMaterial10Enabled) Color(0xFF00FFCC).copy(alpha = 0.25f) else MaterialTheme.colorScheme.secondary,
                                    contentColor = if (isMaterial10Enabled) Color(0xFF00FFCC) else MaterialTheme.colorScheme.onSecondary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download, 
                                    contentDescription = "Download to Gallery"
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Download", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        val isUrlFormat = genFormat == "URL"
                        val isUrlNotEmptyAndInvalid = isUrlFormat && urlLink.trim().isNotEmpty() && urlValidationError != null
                        
                        Box(
                            modifier = Modifier
                                .size(220.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(
                                    if (isUrlNotEmptyAndInvalid) {
                                        if (isMaterial10Enabled) Color(0xFF1F0D10) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
                                    } else {
                                        if (isMaterial10Enabled) Color.Black.copy(alpha = 0.4f) else MaterialTheme.colorScheme.background
                                    }
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isUrlNotEmptyAndInvalid) {
                                        if (isMaterial10Enabled) Color(0xFFFF3366).copy(alpha = 0.7f) else MaterialTheme.colorScheme.error
                                    } else {
                                        if (isMaterial10Enabled) Color(0xFF00FFCC).copy(alpha = 0.25f) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f)
                                    },
                                    shape = RoundedCornerShape(24.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                if (isUrlNotEmptyAndInvalid) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Url validate error",
                                        tint = if (isMaterial10Enabled) Color(0xFFFF3366) else MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Invalid URL Address",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isMaterial10Enabled) Color(0xFFFF3366) else MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = urlValidationError ?: "Please check format",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                        textAlign = TextAlign.Center
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.QrCode,
                                        contentDescription = "Draft setup icon",
                                        tint = if (isMaterial10Enabled) Color(0xFF00FFCC).copy(alpha = 0.4f) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                                        modifier = Modifier.size(56.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Input text or URLs above.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isMaterial10Enabled) Color.White.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                        modifier = Modifier.padding(horizontal = 20.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Web Share & Canvas API Portal Section
        val activePayloadText = viewModel.getFormattedContent()
        com.example.ui.components.WebQrSandboxPanel(
            payloadText = activePayloadText,
            fgColorHex = genFgColor,
            bgColorHex = genBgColor,
            ecLevel = errorCorrectionLevel.name,
            isMaterial10Enabled = isMaterial10Enabled,
            modifier = Modifier.padding(bottom = 96.dp),
            onShowToast = { msg, type -> viewModel.showToast(msg, type) }
        )
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
                        viewModel.showToast("Cleared history", com.example.viewmodel.CustomToastType.INFO)
                    },
                    modifier = Modifier.glassTouchFeedback {
                        viewModel.clearAllHistory()
                        viewModel.showToast("Cleared history", com.example.viewmodel.CustomToastType.INFO)
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
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                cornerRadius = 24.dp
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
                                viewModel.showToast("Copied payload text", com.example.viewmodel.CustomToastType.SUCCESS)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy text")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy")
                        }

                        if (bmap != null) {
                            OutlinedButton(
                                onClick = {
                                    ShareUtils.shareBitmap(
                                        context = context, 
                                        bitmap = bmap,
                                        onShowToast = { msg, type -> viewModel.showToast(msg, type) }
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Share Image")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share")
                            }

                            OutlinedButton(
                                onClick = {
                                    ShareUtils.saveBitmapToGallery(
                                        context = context, 
                                        bitmap = bmap,
                                        onShowToast = { msg, type -> viewModel.showToast(msg, type) }
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = "Download Image")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save")
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
                                    viewModel.showToast("Cannot open link", com.example.viewmodel.CustomToastType.ERROR)
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

    var showDevProfile by remember { mutableStateOf(false) }
    val themeMode by viewModel.themeMode.collectAsState()
    val dynamicColorEnabled by viewModel.dynamicColorEnabled.collectAsState()
    val colorPreset by viewModel.colorPreset.collectAsState()

    if (showDevProfile) {
        DeveloperProfileDialog(
            onDismiss = { showDevProfile = false }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .heightIn(max = 560.dp),
            backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
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

                // Scrollable container for settings options
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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

                // Section 3: Glassmorphism & Blur Customization Style Improvement
                Text(
                    text = "GLASSMOPHISM & BLUR STYLING",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
                )

                // Sub-item 1: Opacity
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    val activeOpacity by viewModel.glassOpacity.collectAsState()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Glass Translucency",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${(activeOpacity * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val levels = listOf(
                            0.12f to "Clear",
                            0.28f to "Default",
                            0.45f to "Frosty",
                            0.70f to "Velvet"
                        )
                        levels.forEach { (level, lbl) ->
                            val isSel = kotlin.math.abs(activeOpacity - level) < 0.05f
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSel) MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .glassTouchFeedback {
                                        viewModel.setGlassOpacity(level)
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = lbl,
                                    fontSize = 11.sp,
                                    color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Sub-item 2: Blur Radius
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    val activeBlur by viewModel.glassBlurRadius.collectAsState()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Backdrop Orbits Blur",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${activeBlur.toInt()} dp",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val blurs = listOf(
                            0f to "Off",
                            8f to "Soft",
                            16f to "Balanced",
                            28f to "Deep"
                        )
                        blurs.forEach { (radius, lbl) ->
                            val isSel = kotlin.math.abs(activeBlur - radius) < 0.5f
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSel) MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .glassTouchFeedback {
                                        viewModel.setGlassBlurRadius(radius)
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = lbl,
                                    fontSize = 11.sp,
                                    color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Sub-item 3: Trim Border Width & Glow Switch side by side
                val activeBorderThickness by viewModel.glassBorderThickness.collectAsState()
                val activeGlow by viewModel.glassGlowEnabled.collectAsState()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Border Trim Segment
                    Column(
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Text(
                            text = "Border Refraction",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val trims = listOf(
                                0.1f to "Invisible",
                                1.0f to "Slim",
                                2.2f to "Thick"
                            )
                            trims.forEach { (trim, lbl) ->
                                val isSel = kotlin.math.abs(activeBorderThickness - trim) < 0.2f
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSel) MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .glassTouchFeedback {
                                            viewModel.setGlassBorderThickness(trim)
                                        }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = lbl,
                                        fontSize = 11.sp,
                                        color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    // Glow Accents Toggle
                    Column(
                        modifier = Modifier.weight(0.7f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Neon Glow",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (activeGlow) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                                )
                                .clickable { viewModel.setGlassGlowEnabled(!activeGlow) }
                                .padding(horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = if (activeGlow) Icons.Default.Star else Icons.Default.StarHalf,
                                contentDescription = "Glow toggle",
                                tint = if (activeGlow) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = if (activeGlow) "ACTIVE" else "MUTED",
                                fontSize = 10.sp,
                                color = if (activeGlow) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Bold
                            )
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Rock QR Engine Pro",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "v4.0 STABLE",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
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
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { showDevProfile = true }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Developer info",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Created by @sayanthRock 🚀",
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                                Text(
                                    text = "Tap to View Tech Profile 📋",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }

                } // Close the Scrollable options container Column

                Spacer(modifier = Modifier.height(16.dp))

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

@Composable
fun DeveloperProfileDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val customCyan = Color(0xFF00FFCC)
    // Safe launcher function for web links
    val openUrl: (String) -> Unit = { url ->
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open link: $url", Toast.LENGTH_SHORT).show()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .heightIn(max = 620.dp),
            backgroundColor = Color(0xFF0B0C0E).copy(alpha = 0.95f),
            borderColor = customCyan.copy(alpha = 0.4f)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header section with close icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "// DEV_CREDENTIAL",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = customCyan,
                            letterSpacing = 1.2.sp
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.glassTouchFeedback { onDismiss() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close profile",
                            tint = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                // Line Separator
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .height(1.dp)
                        .background(customCyan.copy(alpha = 0.25f))
                )

                // Main content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile picture and header metadata
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(customCyan.copy(alpha = 0.15f))
                            .border(1.5.dp, customCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "SR",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 28.sp,
                            color = customCyan
                        )
                        // A small pulse dot in the corner
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00FFCC))
                                .border(1.5.dp, Color(0xFF0B0C0E), CircleShape)
                                .align(Alignment.BottomEnd)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Sayanth Rock",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = "Senior Software Engineer — FAANG Projects",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = customCyan,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Location",
                            tint = Color.Gray,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Thrissur, Kerala, India 📍",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Real-world external interactive action deck (48dp touch targets)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Portfolio Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(customCyan.copy(alpha = 0.12f))
                                .border(1.dp, customCyan.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .clickable { openUrl("https://sayanthrock.github.io/Rock-QR-Code/") },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Language, contentDescription = "Portfolio", tint = customCyan, modifier = Modifier.size(14.dp))
                                Text("Portfolio", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = customCyan)
                            }
                        }

                        // GitHub Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                .clickable { openUrl("https://github.com/SayanthRock") },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Language, contentDescription = "GitHub", tint = Color.White, modifier = Modifier.size(14.dp))
                                Text("GitHub", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        // LinkedIn Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0077B5).copy(alpha = 0.15f))
                                .border(1.dp, Color(0xFF0077B5).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .clickable { openUrl("https://linkedin.com/in/sayanth") },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Person, contentDescription = "LinkedIn", tint = Color(0xFF0077B5), modifier = Modifier.size(14.dp))
                                Text("LinkedIn", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0077B5))
                            }
                        }

                        // Email Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFEA4335).copy(alpha = 0.12f))
                                .border(1.dp, Color(0xFFEA4335).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .clickable { openUrl("mailto:sayanthsmeppayurvaliyaparambil@gmail.com") },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Email, contentDescription = "Email", tint = Color(0xFFEA4335), modifier = Modifier.size(14.dp))
                                Text("Email", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEA4335))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // About section Card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.04f))
                            .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "PROFILE SUMMARY",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = customCyan,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Text(
                            text = "I am a professional software engineer with expertise in AI/ML, full stack development, and enterprise-grade product engineering. My focus lies in building scalable, secure, and high-performance applications with a strong product mindset.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f),
                            textAlign = TextAlign.Start,
                            lineHeight = 16.sp
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Focus Areas
                        val focuses = listOf("AI/ML Systems", "Full Stack", "Cloud Native", "Open Source")
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                        ) {
                            focuses.forEach { focus ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(customCyan.copy(alpha = 0.08f))
                                        .border(0.5.dp, customCyan.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = focus,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = customCyan,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tech Stack Card with horizontal scroll bands
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.04f))
                            .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "CORE TECH STACK",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = customCyan,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )

                        val categories = listOf(
                            "Languages" to listOf("Kotlin", "Java", "TypeScript", "Python", "C++", "Go", "Rust"),
                            "Frontend & Styling" to listOf("React", "Vue", "Angular", "Tailwind CSS", "Jetpack Compose"),
                            "Backend & DB" to listOf("Node.js", "Express", "Django", "Flask", "PostgreSQL", "MongoDB"),
                            "Infra / Cloud / DevOps" to listOf("AWS", "GCP", "Docker", "Kubernetes", "GitHub Actions")
                        )

                        categories.forEachIndexed { idx, (catName, items) ->
                            if (idx > 0) Spacer(modifier = Modifier.height(10.dp))
                            
                            Text(
                                text = catName.uppercase(),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.padding(bottom = 4.dp)
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                            ) {
                                items.forEach { tech ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.White.copy(alpha = 0.03f))
                                            .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = tech,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White.copy(alpha = 0.9f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // AI/ML Expertise Table
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.04f))
                            .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "AI / ML EXPERTISE",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = customCyan,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )

                        val aiDomains = listOf(
                            Triple("Computer Vision", "Advanced", "OCR pipelines, Object detection, Images"),
                            Triple("Natural Language Processing", "Advanced", "Transformers, Conversational AI, Sentiment"),
                            Triple("Predictive Modeling", "Intermediate", "Time-series forecasting, Anomaly detection"),
                            Triple("Reinforcement Learning", "Intermediate", "Simulation, optimization constraints")
                        )

                        aiDomains.forEach { (domain, level, desc) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1.8f)) {
                                    Text(
                                        text = domain,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = desc,
                                        fontSize = 9.sp,
                                        color = Color.Gray
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (level == "Advanced") customCyan.copy(alpha = 0.15f) else Color(0xFFFFCC00).copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = level,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (level == "Advanced") customCyan else Color(0xFFFFCC00),
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color.White.copy(alpha = 0.05f))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Featured Project & FAANG Experience Timeline
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.04f))
                            .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "FEATURED WORK",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = customCyan,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Project 1
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Black.copy(alpha = 0.3f))
                                .border(0.5.dp, customCyan.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Rock QR Code v4.0",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = customCyan,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(customCyan.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "v4.0 STABLE",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = customCyan,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "High-performance QR engine featuring automated CI/CD releases via GitHub Actions. Brand assets are meticulously corrected, pre-releases aligned, and redundant archives streamlined in the latest stable production release.",
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.85f),
                                    lineHeight = 14.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Builds: APK Configured",
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = "•",
                                        fontSize = 9.sp,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = "CI/CD: GitHub Actions",
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = customCyan.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Experience Item
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(customCyan)
                            )
                            Column {
                                Text(
                                    text = "Software Engineer — FAANG-level Products",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "2020 – PRESENT",
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = customCyan,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Designed high-scale backends, integrated machine learning pipelines, and optimized build actions / deployment models globally.",
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.75f),
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Certifications & Awards
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.04f))
                            .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "CREDENTIALS & AWARDS",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = customCyan,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        val certifications = listOf("AWS Certified", "Oracle Certified", "NPTEL Certified", "Cisco Certified")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            certifications.forEach { cert ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White.copy(alpha = 0.05f))
                                        .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Badge",
                                            tint = customCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = cert.replace(" Certified", ""),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                        Text(
                                            text = "Certified",
                                            fontSize = 7.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Awards
                        val awards = listOf(
                            "Open Source Contributor" to "Maintainer of Rock QR Code",
                            "Hackathon Winner" to "AI/ML Innovation Challenge",
                            "Academic Excellence" to "Top 1% in CS Engineering"
                        )
                        awards.forEach { (title, subtitle) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Award icon",
                                    tint = Color(0xFFFFCC00),
                                    modifier = Modifier.size(14.dp)
                                )
                                Column {
                                    Text(
                                        text = title,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = subtitle,
                                        fontSize = 9.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Close Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .glassTouchFeedback { onDismiss() },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = customCyan,
                        contentColor = Color(0xFF0C0E14)
                    )
                ) {
                    Text(
                        text = "CLOSE PROFILE",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
