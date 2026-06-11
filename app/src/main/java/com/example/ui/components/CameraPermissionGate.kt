package com.example.ui.components

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.LinkedCamera
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

/**
 * A highly visual and robust camera permission management component that gracefully handles
 * system authorization handshake sequences, checks shouldShowRequestPermissionRationale,
 * registers lifecycle observers to dynamically check permissions when a user returns from
 * system settings, and provides detailed error cards based on current authorization status.
 */
@Composable
fun CameraPermissionGate(
    modifier: Modifier = Modifier,
    onPermissionGranted: () -> Unit,
    onPermissionStatusChanged: ((Boolean) -> Unit)? = null,
    onShowTestPayloadPrompt: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    // Helper to extract the host Activity for rationale querying
    fun Context.findActivity(): Activity? {
        var currentContext = this
        while (currentContext is ContextWrapper) {
            if (currentContext is Activity) {
                return currentContext
            }
            currentContext = currentContext.baseContext
        }
        return null
    }

    val sharedPrefs = remember {
        context.getSharedPreferences("rock_qr_settings", Context.MODE_PRIVATE)
    }

    // Direct permission state tracker
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Propagate status change
    LaunchedEffect(hasCameraPermission) {
        onPermissionStatusChanged?.invoke(hasCameraPermission)
    }

    // Track if a permission attempt occurred historically across app boots (via SharedPreferences)
    var permissionRequestedOnce by remember {
        mutableStateOf(sharedPrefs.getBoolean("camera_permission_requested", false))
    }

    // Launch Android standard permission dialog
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
            sharedPrefs.edit().putBoolean("camera_permission_requested", true).apply()
            permissionRequestedOnce = true
            if (granted) {
                onPermissionGranted()
            }
        }
    )

    // Handle background-to-foreground permission sync (handles when user toggles permission in platform settings)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val currentGranted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
                hasCameraPermission = currentGranted
                if (currentGranted) {
                    onPermissionGranted()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (hasCameraPermission) {
        // Permission was successfully granted, render scan content viewport
        content()
    } else {
        // Query system whether a rationale explanation is required (means they rejected it once already)
        val showRationale = context.findActivity()?.let {
            ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.CAMERA)
        } ?: false

        // Determine current theme color profile based on authorization states
        val themeColor = when {
            !permissionRequestedOnce -> MaterialTheme.colorScheme.primary
            showRationale -> Color(0xFFFFB703) // Deep Warning Amber Yellow for rationales
            else -> Color(0xFFFF4949) // Crimson Alert for completely blocked permissions
        }

        val titleText = when {
            !permissionRequestedOnce -> "Optic Interface Idle"
            showRationale -> "Access Handshake Refused"
            else -> "Optical Streams Severed"
        }

        val statusBadge = when {
            !permissionRequestedOnce -> "PROTOCOLS IDLE"
            showRationale -> "HANDSHAKE TIMEOUT"
            else -> "FEED BLOCKED"
        }

        val descText = when {
            !permissionRequestedOnce -> "To parse QR matrix profiles or UPI address vectors from physical spaces, this app requires a secure connection to your camera lens. All scanning operations run strictly on-device."
            showRationale -> "Aura Parser requires live preview frames to calculate position grids and extract on-screen glyph paths. Granting permission is vital to resume physical matrix scanning."
            else -> "Camera authorization is blocked at the operating system level. Standard system permission popups are restricted. Please navigate to system settings and grant Camera permissions manually."
        }

        val actionBtnText = when {
            !permissionRequestedOnce -> "Authorize Optic Sensors"
            showRationale -> "Re-Authorize Camera Access"
            else -> "Open System App Settings"
        }

        val actionIcon = when {
            !permissionRequestedOnce -> Icons.Default.LinkedCamera
            showRationale -> Icons.Default.Warning
            else -> Icons.Default.Block
        }

        val onActionClick: () -> Unit = {
            if (permissionRequestedOnce && !showRationale) {
                try {
                    val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "Redirect failed. Please open settings manually.", Toast.LENGTH_SHORT).show()
                }
            } else {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        // Render full screen styled glass card backdrop and HUD error readout
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF0F1115)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.28f))
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                themeColor.copy(alpha = 0.5f),
                                Color.Transparent,
                                themeColor.copy(alpha = 0.65f)
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
            ) {
                // Background artistic telemetry dots
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawCircle(
                        color = themeColor.copy(alpha = 0.25f),
                        radius = 6.dp.toPx(),
                        center = Offset(20.dp.toPx(), 20.dp.toPx())
                    )
                    drawCircle(
                        color = themeColor.copy(alpha = 0.15f),
                        radius = 18.dp.toPx(),
                        center = Offset(20.dp.toPx(), 20.dp.toPx())
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Status Badge Indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.align(Alignment.Start)
                    ) {
                        Spacer(modifier = Modifier.width(18.dp))
                        Surface(
                            modifier = Modifier.size(6.dp),
                            color = themeColor,
                            shape = CircleShape
                        ) {}
                        Text(
                            text = statusBadge,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = themeColor,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Pulsing security radar circle
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(themeColor.copy(alpha = 0.12f), CircleShape)
                            .border(1.5.dp, themeColor.copy(alpha = 0.35f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = actionIcon,
                            contentDescription = "Security status indicator",
                            tint = themeColor,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Title Header
                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Descriptive rationale
                    Text(
                        text = descText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.72f),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Custom glass primary interaction button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .glassTouchFeedback(onClick = onActionClick)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        themeColor,
                                        themeColor.copy(alpha = 0.7f)
                                    )
                                )
                            )
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = actionIcon,
                                contentDescription = "Action execution icon",
                                tint = if (themeColor == Color(0xFFFFB703)) Color.Black else Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = actionBtnText,
                                fontWeight = FontWeight.Bold,
                                color = if (themeColor == Color(0xFFFFB703)) Color.Black else Color.White,
                                fontSize = 13.sp
                            )
                        }
                    }

                    if (onShowTestPayloadPrompt != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        onShowTestPayloadPrompt()
                    }
                }
            }
        }
    }
}
