package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A highly polished, custom, clean loading animation for 2026.
 * Features an elegant rotating multi-arc orbits and a soft breathing core dot.
 */
@Composable
fun PremiumLoadingAnimation(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.secondary,
    size: Dp = 64.dp,
    strokeWidth: Dp = 4.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading_anim_transition")

    // Slow continuous rotation
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Breathing pulse for the core dot
    val coreScale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "core_pulse"
    )

    // Pulse for arc sweep angles to look organic
    val sweepAngleState by infiniteTransition.animateFloat(
        initialValue = 30f,
        targetValue = 120f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arc_sweep"
    )

    Canvas(
        modifier = modifier
            .size(size)
    ) {
        val width = this.size.width
        val height = this.size.height
        val minSize = kotlin.math.min(width, height)
        val strokePx = strokeWidth.toPx()
        val radius = (minSize - strokePx) / 2f
        val center = Offset(width / 2f, height / 2f)

        // Draw soft background track ring
        drawCircle(
            color = color.copy(alpha = 0.1f),
            radius = radius,
            center = center,
            style = Stroke(width = strokePx)
        )

        // Draw animated outer spinning arc 1 (primary color)
        drawArc(
            brush = Brush.sweepGradient(
                colors = listOf(color.copy(alpha = 0.1f), color, color.copy(alpha = 0.1f)),
                center = center
            ),
            startAngle = rotationAngle,
            sweepAngle = sweepAngleState,
            useCenter = false,
            topLeft = Offset(strokePx / 2f, strokePx / 2f),
            size = Size(minSize - strokePx, minSize - strokePx),
            style = Stroke(width = strokePx, cap = StrokeCap.Round)
        )

        // Draw animated outer spinning arc 2 (secondary opposite color, rotating reverse)
        drawArc(
            color = secondaryColor.copy(alpha = 0.8f),
            startAngle = -rotationAngle + 180f,
            sweepAngle = sweepAngleState * 0.7f,
            useCenter = false,
            topLeft = Offset(strokePx / 2f, strokePx / 2f),
            size = Size(minSize - strokePx, minSize - strokePx),
            style = Stroke(width = strokePx * 0.75f, cap = StrokeCap.Round)
        )

        // Draw breathing core dot
        drawCircle(
            color = color,
            radius = (minSize * 0.16f) * coreScale,
            center = center
        )

        // Draw high-contrast specular flare
        drawCircle(
            color = Color.White.copy(alpha = 0.6f),
            radius = (minSize * 0.05f) * coreScale,
            center = Offset(center.x - minSize * 0.04f, center.y - minSize * 0.04f)
        )
    }
}
