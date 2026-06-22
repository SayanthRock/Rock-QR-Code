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
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
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
    val themeConfig = LiquidGlassTheme.LocalConfig.current
    val primaryColor = themeConfig.primaryColor
    val secondaryColor = themeConfig.secondaryColor

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var scannedContent by remember { mutableStateOf<String?>(null) }
    var lastScannedTime by remember { mutableStateOf(0L) }
    var isFlashOn by remember { mutableStateOf(false) }
    var cameraControlState by remember { mutableStateOf<CameraControl?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
            viewModel.showToast(
                if (granted) "Camera permission granted" else "Camera permission is required for scanning",
                if (granted) CustomToastType.SUCCESS else CustomToastType.WARNING
            )
        }
    )

    val infiniteTransition = rememberInfiniteTransition(label = "scanner_motion")
    val scannerLineOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanner_line"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("scan_screen_root")
    ) {
        if (!hasCameraPermission) {
            CameraPermissionGate(
                primaryColor = primaryColor,
                onRequestPermission = {
                    HapticUtils.vibrate(context, 40)
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            )
            return@Box
        }

        val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
        DisposableEffect(Unit) {
            onDispose {
                cameraExecutor.shutdown()
            }
        }

        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
                val mainExecutor = ContextCompat.getMainExecutor(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().apply {
                        setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { analysis ->
                            analysis.setAnalyzer(cameraExecutor, QrCodeAnalyzer { result ->
                                mainExecutor.execute {
                                    val now = System.currentTimeMillis()
                                    if (result.isNotBlank() && now - lastScannedTime > 1800L) {
                                        lastScannedTime = now
                                        scannedContent = result
                                        HapticUtils.vibrate(ctx, 80)
                                        viewModel.saveScannedResult(result)
                                        viewModel.showToast("QR decoded successfully", CustomToastType.SUCCESS)
                                    }
                                }
                            })
                        }

                    try {
                        cameraProvider.unbindAll()
                        val camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalyzer
                        )
                        cameraControlState = camera.cameraControl
                    } catch (e: Exception) {
                        viewModel.showToast("Camera failed to start", CustomToastType.ERROR)
                    }
                }, mainExecutor)

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        ScannerOverlay(
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            scannerLineOffset = scannerLineOffset
        )

        ScannerTopBar(
            isFlashOn = isFlashOn,
            onFlashToggle = {
                HapticUtils.vibrate(context, 30)
                cameraControlState?.let { control ->
                    isFlashOn = !isFlashOn
                    control.enableTorch(isFlashOn)
                    viewModel.showToast(
                        if (isFlashOn) "Torch switched on" else "Torch switched off",
                        CustomToastType.INFO
                    )
                } ?: viewModel.showToast("Camera is still starting", CustomToastType.INFO)
            }
        )

        AnimatedVisibility(
            visible = scannedContent != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 90.dp)
                .padding(horizontal = 16.dp)
        ) {
            scannedContent?.let { content ->
                ScanResultCard(
                    content = content,
                    primaryColor = primaryColor,
                    onDismiss = { scannedContent = null },
                    onCopy = {
                        HapticUtils.vibrate(context, 30)
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Decoded QR", content))
                        viewModel.showToast("Copied to clipboard", CustomToastType.SUCCESS)
                    },
                    onOpenUrl = {
                        openScannedUrl(context, content, viewModel)
                    },
                    onShare = {
                        shareScannedText(context, content)
                    }
                )
            }
        }
    }
}

@Composable
private fun CameraPermissionGate(
    primaryColor: Color,
    onRequestPermission: () -> Unit
) {
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
                contentDescription = "Camera access required",
                tint = primaryColor,
                modifier = Modifier.size(42.dp)
            )
        }
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = "Activate Scanner",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.SansSerif
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Camera access is required to scan QR codes. Scanning stays offline on your device.",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.70f),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(52.dp)
                .testTag("request_camera_button")
        ) {
            Text(
                text = "Enable Scanner",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = Color.White
            )
        }
    }
}

@Composable
private fun ScannerOverlay(
    primaryColor: Color,
    secondaryColor: Color,
    scannerLineOffset: Float
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    ) {
        val width = size.width
        val height = size.height
        val squareSize = 250.dp.toPx()
        val left = (width - squareSize) / 2
        val top = (height - squareSize) / 2

        drawRect(
            color = Color.Black.copy(alpha = 0.65f),
            size = size
        )
        drawRoundRect(
            color = Color.Transparent,
            topLeft = Offset(left, top),
            size = Size(squareSize, squareSize),
            cornerRadius = CornerRadius(24f, 24f),
            blendMode = BlendMode.Clear
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(250.dp)
                .border(2.dp, primaryColor.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .offset(y = 247.dp * scannerLineOffset)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
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
}

@Composable
private fun ScannerTopBar(
    isFlashOn: Boolean,
    onFlashToggle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Position QR inside the frame",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 6.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(onClick = onFlashToggle)
                .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                contentDescription = "Toggle torch",
                tint = if (isFlashOn) Color.Yellow else Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ScanResultCard(
    content: String,
    primaryColor: Color,
    onDismiss: () -> Unit,
    onCopy: () -> Unit,
    onOpenUrl: () -> Unit,
    onShare: () -> Unit
) {
    val isUrl = content.trim().startsWith("http://", ignoreCase = true) ||
        content.trim().startsWith("https://", ignoreCase = true) ||
        content.trim().startsWith("www.", ignoreCase = true)

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
        colors = CardDefaults.cardColors(containerColor = Color(0xEB1A1A1A)),
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
                            contentDescription = "Scan success",
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
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss result",
                        tint = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            SelectionContainer {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    maxLines = 6
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onCopy,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy", color = Color.White, fontSize = 13.sp)
                }

                Button(
                    onClick = if (isUrl) onOpenUrl else onShare,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = if (isUrl) Icons.Outlined.Language else Icons.Outlined.IosShare,
                        contentDescription = if (isUrl) "Open link" else "Share text",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isUrl) "Open" else "Share", color = Color.White, fontSize = 13.sp)
                }
            }
        }
    }
}

private fun openScannedUrl(context: Context, content: String, viewModel: QRViewModel) {
    try {
        val rawUrl = content.trim()
        val cleanUrl = if (!rawUrl.startsWith("http://", true) && !rawUrl.startsWith("https://", true)) {
            "https://$rawUrl"
        } else {
            rawUrl
        }
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(cleanUrl)))
    } catch (_: Exception) {
        viewModel.showToast("Failed to open link", CustomToastType.ERROR)
    }
}

private fun shareScannedText(context: Context, content: String) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, content)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share scanned QR"))
}
