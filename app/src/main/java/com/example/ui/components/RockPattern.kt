package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer

/**
 * A modifier that draws subtle, procedural geometric fracture facets and geode lines.
 * This represents crystalline crevices and mineral growth sheets in metamorphic rock.
 */
fun Modifier.rockFractureBackground(
    color: Color,
    alpha: Float = 0.08f,
    strokeWidth: Dp = 1.25.dp
) = this.drawBehind {
    val width = size.width
    val height = size.height
    val strokePx = strokeWidth.toPx()

    // Line 1: Primary jagged horizontal fracture split
    val horizCrack = Path().apply {
        moveTo(0f, height * 0.22f)
        lineTo(width * 0.28f, height * 0.14f)
        lineTo(width * 0.44f, height * 0.32f)
        lineTo(width * 0.72f, height * 0.20f)
        lineTo(width * 0.88f, height * 0.38f)
        lineTo(width, height * 0.26f)
    }
    
    // Line 2: Vertically shifting crack intersection
    val vertCrack = Path().apply {
        moveTo(width * 0.44f, height * 0.32f)
        lineTo(width * 0.38f, height * 0.58f)
        lineTo(width * 0.52f, height * 0.76f)
        lineTo(width * 0.46f, height)
    }

    // Line 3: Small corner crystalline cleavage shard at top right
    val shardTopRight = Path().apply {
        moveTo(width * 0.82f, 0f)
        lineTo(width * 0.90f, height * 0.12f)
        lineTo(width, height * 0.06f)
    }

    // Line 4: Jagged bottom segment
    val shardBottomLeft = Path().apply {
        moveTo(0f, height * 0.78f)
        lineTo(width * 0.18f, height * 0.86f)
        lineTo(width * 0.32f, height * 0.72f)
        lineTo(width * 0.38f, height * 0.58f)
    }

    // Paint the paths
    drawPath(
        path = horizCrack,
        color = color.copy(alpha = alpha),
        style = Stroke(width = strokePx, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
    drawPath(
        path = vertCrack,
        color = color.copy(alpha = alpha),
        style = Stroke(width = strokePx, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
    drawPath(
        path = shardTopRight,
        color = color.copy(alpha = alpha * 1.2f),
        style = Stroke(width = strokePx, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
    drawPath(
        path = shardBottomLeft,
        color = color.copy(alpha = alpha),
        style = Stroke(width = strokePx, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Grain dots representing crystalline minerals/pyrite sparkling inside
    val dots = listOf(
        Offset(width * 0.12f, height * 0.40f),
        Offset(width * 0.82f, height * 0.65f),
        Offset(width * 0.64f, height * 0.15f),
        Offset(width * 0.25f, height * 0.85f),
        Offset(width * 0.92f, height * 0.35f)
    )
    dots.forEach { dot ->
        drawCircle(
            color = color.copy(alpha = alpha * 1.8f),
            radius = 1.5.dp.toPx(),
            center = dot
        )
    }
}

/**
 * A beautiful geode shape which provides hexagonal chiseled corner profiles
 * rather than simple modern curves, expressing an active rock-carving aesthetic.
 */
val ChiseledOctagonShape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    // Use a responsive corner size based on the widget dimensions
    val corner = (size.minDimension * 0.08f).coerceAtMost(48f)
    
    moveTo(corner, 0f)
    lineTo(w - corner, 0f)
    lineTo(w, corner)
    
    lineTo(w, h - corner)
    lineTo(w - corner, h)
    lineTo(corner, h)
    
    lineTo(0f, h - corner)
    lineTo(0f, corner)
    close()
}

/**
 * A stylized card with faceted geometric chamfers and procedural fractures.
 */
@Composable
fun RockFacetedCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(ChiseledOctagonShape)
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = ChiseledOctagonShape
            )
            .rockFractureBackground(
                color = MaterialTheme.colorScheme.primary,
                alpha = 0.08f
            )
            .padding(16.dp)
    ) {
        Column {
            content()
        }
    }
}

/**
 * An industrial style header label decorated with chiseled boundaries.
 */
@Composable
fun RockChiseledHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                // Bottom decorative double slate line
                val strokeW = 1.dp.toPx()
                drawLine(
                    color = accentColor.copy(alpha = 0.35f),
                    start = Offset(0f, size.height - 4f),
                    end = Offset(size.width, size.height - 4f),
                    strokeWidth = strokeW
                )
                drawLine(
                    color = accentColor.copy(alpha = 0.15f),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = strokeW
                )
            }
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title.uppercase(),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                letterSpacing = 0.5.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }
        
        // Stylized gem node decorative marker
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(ChiseledOctagonShape)
                .background(accentColor)
        )
    }
}

/**
 * Elegant Glassmorphism card designed with high transparency,
 * glossy bordering, and comfortable organic rounded corners.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f),
    borderColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
            )
            .padding(20.dp)
    ) {
        Column {
            content()
        }
    }
}

/**
 * Animated physics-based scaling, alpha shifting, and touch feeling
 * specifically tailored for sleek tactile button and card elements.
 */
@Composable
fun Modifier.glassTouchFeedback(
    onClick: () -> Unit
): Modifier {
    val interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val animatedScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        label = "glass_touch_scale"
    )
    
    val animatedAlpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1.0f,
        label = "glass_touch_alpha"
    )

    return this
        .scale(animatedScale)
        .graphicsLayer {
            alpha = animatedAlpha
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null, // Implements tactile movement instead of simple flat color ripple
            onClick = onClick
        )
}
