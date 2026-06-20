package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ui.theme.LiquidGlassTheme
import com.example.utils.HapticUtils
import com.example.utils.QrCodeAnalyzer
import com.example.viewmodel.CustomToastType
import com.example.viewmodel.QRViewModel
import java.util.concurrent.Executors

@Composable
fun ScanScreen(
    viewModel: QRViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
            if (granted) {
                viewModel.showToast("Camera Permission Granted!", CustomToastType.SUCCESS)
            } else {
                viewModel.showToast("Camera is required to use the real-time scanner.", CustomToastType.WARNING)
            }
        }
    )

    val activePreset by viewModel.colorPreset.collectAsState()
    val themeConfig = LiquidGlassTheme.LocalConfig.current
    val primaryColor = themeConfig.primaryColor
    val secondaryColor = themeConfig.secondaryColor

    // Scanner state
    var scannedContent by remember { mutableStateOf<String?>(null) }
    var lastScannedTime by remember { mutableStateOf(0L) }
    var isFlashOn by remember { mutableStateOf(false) }
    var cameraControlState by remember { mutableStateOf<CameraControl?>(null) }

    // Pulse animation for HUD frame
    val infiniteTransition = rememberInfiniteTransition(label = "hud_pulse")
    val scanTrackerOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanner_tracker"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("scan_screen_root")
    ) {
        if (!hasCameraPermission) {
            // GORGEOUS CAMERA PERMISSION GATE WITH CRYSTAL STONES INSPIRED STYLING
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(primaryColor.copy(alpha = 0.15f))
                        .border(1.5.dp, primaryColor.copy(alpha = 0.40f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Camera Access Required",
                        tint = primaryColor,
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "Activate Crystal Lens",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeConfig.textColor,
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.SansSerif
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "To decode QR barcodes instantly, authorize camera access. Scanning is completed fully offline to respect your data privacy.",
                    fontSize = 14.sp,
                    color = themeConfig.subTextColor,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        HapticUtils.vibrate(context, 40)
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(52.dp)
                        .testTag("request_camera_button")
                ) {
                    Text(
                        text = "Enable Offline Scanner",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }
        } else {
            // CAMERA PREVIEW AND SCAN HUD LAYER
            val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx).apply {
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                        }
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().apply {
                                setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val imageAnalyzer = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                                .also {
                                    it.setAnalyzer(
                                        cameraExecutor,
                                        QrCodeAnalyzer { result ->
                                            val currentTime = System.currentTimeMillis()
                                            if (result.isNotEmpty() && (currentTime - lastScannedTime > 2000)) {
                                                lastScannedTime = currentTime
                                                HapticUtils.vibrate(context, 80)
                                                scannedContent = result
                                                viewModel.saveScannedResult(result)
                                                viewModel.showToast("QR Decoded Successfully!", CustomToastType.SUCCESS)
                                            }
                                        }
                                    )
                                }

                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                            try {
                                cameraProvider.unbindAll()
                                val camera = cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageAnalyzer
                                )
                                cameraControlState = camera.cameraControl
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // ROCK ENCLOSED BARCODE MASK OVERLAY
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val squareSize = 250.dp.toPx()

                    val left = (width - squareSize) / 2
                    val top = (height - squareSize) / 2

                    // Transparent dark layer surrounding the scan box
                    drawRect(
                        color = Color.Black.copy(alpha = 0.65f),
                        size = size
                    )

                    // punch hole in context
                    drawRoundRect(
                        color = Color.Transparent,
                        topLeft = Offset(left, top),
                        size = Size(squareSize, squareSize),
                        cornerRadius = CornerRadius(24f, 24f),
                        blendMode = BlendMode.Clear
                    )
                }

                // SUBTLE CORNER LIGHTING DESIGN
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(250.dp)
                            .border(width = 2.dp, color = primaryColor.copy(alpha = 0.40f), shape = RoundedCornerShape(12.dp))
                    ) {
                        // Moving Quartz laser line
                        val yOffset = 250.dp * scanTrackerOffset
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .offset(y = yOffset)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            primaryColor,
                                            secondaryColor,
                                            primaryColor,
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                    }
                }

                // TOP SCREEN INFO BAR
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .align(Alignment.TopCenter),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Position QR inside the frame",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.45f), shape = RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // FLASH LIGHT SWITCH CAPSULE
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .clickable {
                                HapticUtils.vibrate(context, 30)
                                cameraControlState?.let { controller ->
                                    isFlashOn = !isFlashOn
                                    controller.enableTorch(isFlashOn)
                                    viewModel.showToast(
                                        if (isFlashOn) "Torch Switched On" else "Torch Switched Off",
                                        CustomToastType.INFO
                                    )
                                }
                            }
                            .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Toggle Torch",
                            tint = if (isFlashOn) Color.Yellow else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // SCREEN OVERLAY RESULT DRAWER (IF SCAN HAS RESULT DETECTED)
                AnimatedVisibility(
                    visible = scannedContent != null,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 90.dp) // Leave spacing for navigation bar capsule
                        .padding(horizontal = 16.dp)
                ) {
                    scannedContent?.let { content ->
                        // Premium frosted card display
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    brush = Brush.linearGradient(
                                        listOf(primaryColor.copy(alpha = 0.5f), Color.White.copy(alpha = 0.05f))
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .testTag("scanned_result_card"),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xEB1A1A1A)
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(primaryColor.copy(alpha = 0.20f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Success",
                                                tint = primaryColor,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Scan Parsed",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            HapticUtils.vibrate(context, 20)
                                            scannedContent = null
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Dismiss",
                                            tint = Color.White.copy(alpha = 0.6f)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Content field box
                                SelectionContainer {
                                    Text(
                                        text = content,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.9f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                Color.White.copy(alpha = 0.05f),
                                                RoundedCornerShape(12.dp)
                                            )
                                            .border(
                                                1.dp,
                                                Color.White.copy(alpha = 0.1f),
                                                RoundedCornerShape(12.dp)
                                            )
                                            .padding(14.dp),
                                        maxLines = 6
                                    )
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                // Quick operational row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // COPY BUTTON
                                    Button(
                                        onClick = {
                                            HapticUtils.vibrate(context, 30)
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("Decoded QR", content)
                                            clipboard.setPrimaryClip(clip)
                                            viewModel.showToast("Copied to clipboard", CustomToastType.SUCCESS)
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.White.copy(alpha = 0.12f)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.ContentCopy,
                                            contentDescription = "Copy",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Copy", color = Color.White, fontSize = 13.sp)
                                    }

                                    // ACTION NAVIGATE (ONLY IF WEB LINK)
                                    val isUrl = content.trim().startsWith("http://", ignoreCase = true) ||
                                            content.trim().startsWith("https://", ignoreCase = true) ||
                                            content.trim().startsWith("www.", ignoreCase = true)

                                    if (isUrl) {
                                        Button(
                                            onClick = {
                                                HapticUtils.vibrate(context, 40)
                                                try {
                                                    val rawUrl = content.trim()
                                                    val cleanUrl = if (!rawUrl.startsWith("http://") && !rawUrl.startsWith("https://")) {
                                                        "https://$rawUrl"
                                                    } else rawUrl
                                                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(cleanUrl))
                                                    context.startActivity(browserIntent)
                                                } catch (e: Exception) {
                                                    viewModel.showToast("Failed to launch browser", CustomToastType.ERROR)
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = primaryColor
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Language,
                                                contentDescription = "Open Link",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Go To URL", color = Color.White, fontSize = 13.sp)
                                        }
                                    } else {
                                        // Standard text share
                                        Button(
                                            onClick = {
                                                HapticUtils.vibrate(context, 30)
                                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "text/plain"
                                                    putExtra(Intent.EXTRA_TEXT, content)
                                                }
                                                context.startActivity(Intent.createChooser(shareIntent, "Share Scanned QR"))
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = primaryColor
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.IosShare,
                                                contentDescription = "Share text",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Share", color = Color.White, fontSize = 13.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Inline fallback since Compose SelectionContainer can sometimes cause runtime errors in some versions
@Composable
fun SelectionContainer(content: @Composable () -> Unit) {
    androidx.compose.foundation.text.selection.SelectionContainer {
        content()
    }
}
