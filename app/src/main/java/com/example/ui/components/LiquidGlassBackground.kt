package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ui.theme.LocalLiquidGlassThemeConfig
import com.example.ui.theme.LiquidGlassThemeConfig

/**
 * A beautiful, premium animated "Liquid Glass / Frosted Glass" background.
 * Draws orbiting fluid radial neon blobs, a clean digital matrix grid, and
 * a frosted overlay that changes dynamically depending on Dark / Light theme.
 */
@Composable
fun LiquidGlassBackground(
    modifier: Modifier = Modifier,
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    bgPhotoUri: String? = null,
    bgPhotoBlurRadius: Float = 12f,
    bgPhotoEnabled: Boolean = false,
    bgPhotoBlurEnabled: Boolean = true
) {
    val config = LocalLiquidGlassThemeConfig.current
    val infiniteTransition = rememberInfiniteTransition(label = "liquid_glass_orbit")

    // Orbit coordinates for Blob 1 (Cyan/Blue neon aspect)
    val blob1StateX by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = SineIntervalEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob1_x"
    )
    val blob1StateY by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = SineIntervalEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob1_y"
    )

    // Orbit coordinates for Blob 2 (Magenta/Purple neon aspect)
    val blob2StateX by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = SineIntervalEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob2_x"
    )
    val blob2StateY by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(11000, easing = SineIntervalEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob2_y"
    )

    // Orbit coordinates for Blob 3 (Tertiary yellow/cyan accent aspect)
    val blob3StateX by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(21000, easing = SineIntervalEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob3_x"
    )
    val blob3StateY by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = SineIntervalEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob3_y"
    )

    // Slow-sweeping glass reflection sheen sweep mimicking sunlight on physical glass
    val glassSheenShift by infiniteTransition.animateFloat(
        initialValue = -0.3f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(28000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "glass_sheen"
    )

    // Primary background colors
    val baseBgColor = if (useDarkTheme) Color(0xFF0D0F12) else Color(0xFFF3F5F9)
    val dotColor = if (useDarkTheme) Color(0xFF00FFCC).copy(alpha = 0.05f) else Color(0xFF00BD9D).copy(alpha = 0.07f)
    
    val colorBlob1 = MaterialTheme.colorScheme.primary.copy(alpha = if (useDarkTheme) 0.16f else 0.10f)
    val colorBlob2 = MaterialTheme.colorScheme.secondary.copy(alpha = if (useDarkTheme) 0.14f else 0.08f)
    val colorBlob3 = MaterialTheme.colorScheme.tertiary.copy(alpha = if (useDarkTheme) 0.12f else 0.06f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseBgColor)
    ) {
        if (bgPhotoEnabled && !bgPhotoUri.isNullOrEmpty()) {
            AsyncImage(
                model = bgPhotoUri,
                contentDescription = "Custom Background Photo",
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
            // Frosted dark overlay to sustain crisp aesthetic legibility and high contrast
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
            )
        } else {
            // 1. Blurred background fluid glowing orbits
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .blur(radius = config.glassBlur)
            ) {
                val w = size.width
                val h = size.height

                // Draw glowing liquid Blur Orbit 1
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(colorBlob1, Color.Transparent),
                        center = Offset(w * blob1StateX, h * blob1StateY),
                        radius = size.minDimension * 0.45f
                    ),
                    radius = size.minDimension * 0.45f,
                    center = Offset(w * blob1StateX, h * blob1StateY)
                )

                // Draw glowing liquid Blur Orbit 2
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(colorBlob2, Color.Transparent),
                        center = Offset(w * blob2StateX, h * blob2StateY),
                        radius = size.minDimension * 0.5f
                    ),
                    radius = size.minDimension * 0.5f,
                    center = Offset(w * blob2StateX, h * blob2StateY)
                )

                // Draw glowing liquid Blur Orbit 3
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(colorBlob3, Color.Transparent),
                        center = Offset(w * blob3StateX, h * blob3StateY),
                        radius = size.minDimension * 0.4f
                    ),
                    radius = size.minDimension * 0.4f,
                    center = Offset(w * blob3StateX, h * blob3StateY)
                )

                // 3D Glass linear highlight lustre sweeping slowly across the frosted pane
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = if (useDarkTheme) 0.012f else 0.03f),
                            Color.White.copy(alpha = if (useDarkTheme) 0.035f else 0.07f),
                            Color.White.copy(alpha = if (useDarkTheme) 0.012f else 0.03f),
                            Color.Transparent
                        ),
                        start = Offset(w * (glassSheenShift - 0.25f), 0f),
                        end = Offset(w * (glassSheenShift + 0.25f), h)
                    )
                )
            }
        }

        // 2. Cybernetic matrix dot raster pattern representing precision coordinate tracking (Sharp layer)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val dotSpacing = 24.dp.toPx()
            val dotRadius = 1.dp.toPx()
            
            val cols = (w / dotSpacing).toInt() + 1
            val rows = (h / dotSpacing).toInt() + 1
            
            for (col in 0 until cols) {
                for (row in 0 until rows) {
                    val x = col * dotSpacing + dotSpacing / 2f
                    val y = row * dotSpacing + dotSpacing / 2f
                    if (x < w && y < h) {
                        drawCircle(
                            color = dotColor,
                            radius = dotRadius,
                            center = Offset(x, y)
                        )
                    }
                }
            }
        }
    }
}

// Sine interval easing parameter to ensure fluid wave orbits
private val SineIntervalEasing = Easing { fraction ->
    val rad = fraction * Math.PI.toFloat()
    (1f - kotlin.math.cos(rad)) / 2f
}
