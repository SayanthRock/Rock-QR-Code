package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.viewmodel.CustomToastMessage
import com.example.viewmodel.CustomToastType
import com.example.ui.theme.LocalLiquidGlassThemeConfig
import com.example.ui.theme.LiquidGlassThemeConfig
import kotlinx.coroutines.delay

@Composable
fun CustomToastOverlay(
    toastMessage: CustomToastMessage?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (toastMessage != null) {
        LaunchedEffect(toastMessage.id) {
            delay(toastMessage.durationMs)
            onDismiss()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 96.dp, top = 64.dp, start = 24.dp, end = 24.dp), // Safe breathing space
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = toastMessage != null,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeIn(animationSpec = tween(300)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(250)
            ) + fadeOut(animationSpec = tween(200))
        ) {
            if (toastMessage != null) {
                CustomToastCard(toastMessage = toastMessage)
            }
        }
    }
}

@Composable
fun CustomToastCard(
    toastMessage: CustomToastMessage,
    modifier: Modifier = Modifier
) {
    val themeConfig = LocalLiquidGlassThemeConfig.current
    
    // Choose appropriate icon and colors based on ToastType
    val (icon, accentColor) = when (toastMessage.type) {
        CustomToastType.SUCCESS -> Pair(Icons.Default.CheckCircle, Color(0xFF00FFCC)) // Bright Neo-mint green
        CustomToastType.INFO -> Pair(Icons.Default.Info, themeConfig.primaryColor)
        CustomToastType.ERROR -> Pair(Icons.Default.Error, Color(0xFFFF5555)) // Vibrant Crimson Red
        CustomToastType.WARNING -> Pair(Icons.Default.Warning, Color(0xFFFFB703)) // Warm amber yellow
    }

    // Glass backdrop Brush
    val glassBgBrush = Brush.verticalGradient(
        colors = listOf(
            Color.Black.copy(alpha = 0.55f),
            Color.Black.copy(alpha = 0.35f)
        )
    )

    Card(
        modifier = modifier
            .testTag("custom_toast_card")
            .widthIn(max = 450.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = accentColor.copy(alpha = 0.3f),
                spotColor = accentColor.copy(alpha = 0.45f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .background(glassBgBrush)
                .border(
                    width = 1.5.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.61f),
                            accentColor.copy(alpha = 0.15f)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "${toastMessage.type} Icon",
                tint = accentColor,
                modifier = Modifier
                    .size(28.dp)
                    .testTag("toast_icon")
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = toastMessage.message,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("toast_message")
            )
        }
    }
}
