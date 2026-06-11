package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import android.widget.Toast
import android.content.Intent
import android.net.Uri

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
    backgroundColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
    borderColor: Color = MaterialTheme.colorScheme.primary,
    cornerRadius: Dp = 24.dp,
    borderWidth: Dp = 1.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val borderBrush = Brush.linearGradient(
        colors = listOf(
            borderColor.copy(alpha = 0.45f),
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
            borderColor.copy(alpha = 0.6f)
        )
    )

    Box(
        modifier = modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .border(
                width = borderWidth,
                brush = borderBrush,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius)
            )
            .padding(20.dp)
    ) {
        Column {
            content()
        }
    }
}

/**
 * A multipurpose Liquid Glass modifier that applies a semi-transparent frosted card background,
 * soft organic borders with glossy gradient refractions, and responsive clipping.
 */
fun Modifier.liquidGlass(
    shape: androidx.compose.ui.graphics.Shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    borderAlphaStart: Float = 0.45f,
    borderAlphaEnd: Float = 0.15f,
    bgAlpha: Float = 0.35f
) = composed {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val surfaceColor = MaterialTheme.colorScheme.surface
    
    val borderBrush = Brush.linearGradient(
        colors = listOf(
            primaryColor.copy(alpha = borderAlphaStart),
            secondaryColor.copy(alpha = borderAlphaEnd),
            primaryColor.copy(alpha = borderAlphaStart * 1.3f)
        )
    )
    
    this
        .clip(shape)
        .background(surfaceColor.copy(alpha = bgAlpha))
        .border(
            width = 1.dp,
            brush = borderBrush,
            shape = shape
        )
}

/**
 * A frosted Glassmorphism Button featuring responsive physics tactile click states
 * and a premium, multi-tonal glowing chiseled outline.
 */
@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val alphaVal = if (enabled) 1.0f else 0.5f
    Box(
        modifier = modifier
            .alpha(alphaVal)
            .glassTouchFeedback(onClick = { if (enabled) onClick() })
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
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

/**
 * A highly styled, interactive 'Liquid Glass' card that displays QR code scan results.
 * It features translucent glass backing, diagonal specular gloss highlights,
 * glowing Material-based gradient borders, distinct parsed type layouts (URLs, Wifi, Plain text, UPI),
 * and a tactile actionable design that stays responsive across all sizes.
 */
@Composable
fun LiquidGlassScannerResultCard(
    scannedText: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var isCopied by remember { mutableStateOf(false) }
    LaunchedEffect(isCopied) {
        if (isCopied) {
            delay(2000)
            isCopied = false
        }
    }

    // Parse the QR content type
    val isUrl = scannedText.startsWith("http", ignoreCase = true)
    val isUpi = scannedText.startsWith("upi:", ignoreCase = true)
    val isWifi = scannedText.startsWith("WIFI:", ignoreCase = true)
    val isVcard = scannedText.contains("BEGIN:VCARD", ignoreCase = true)

    val (badgeText, icon, badgeColor) = when {
        isUrl -> Triple("HYPERLINK CONTEXT", Icons.Default.Language, Color(0xFF33CCFF))
        isUpi -> Triple("UPI TRANSACTION BEACON", Icons.Default.AccountBalanceWallet, Color(0xFF00FFCC))
        isWifi -> Triple("WIRELESS NODAL POINT", Icons.Default.Wifi, Color(0xFF9D4EED))
        isVcard -> Triple("DIGITAL IDENTITY VECTORS", Icons.Default.ContactPage, Color(0xFFFF9E00))
        else -> Triple("ALPHANUMERIC METAMORPHIC BEACON", Icons.Default.Description, MaterialTheme.colorScheme.primary)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.28f))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        // Frosted shine gloss reflection overlay
        Canvas(modifier = Modifier.matchParentSize()) {
            // Draw a subtle gloss diagonal swipe across the glass
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.08f),
                        Color.White.copy(alpha = 0.01f),
                        Color.Transparent
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height)
                )
            )
            
            // Draw subtle matrix glass noise particles representing coordinate scans
            val points = listOf(
                Offset(size.width * 0.15f, size.height * 0.25f),
                Offset(size.width * 0.85f, size.height * 0.15f),
                Offset(size.width * 0.45f, size.height * 0.75f),
                Offset(size.width * 0.75f, size.height * 0.65f),
                Offset(size.width * 0.25f, size.height * 0.85f)
            )
            points.forEach { pt ->
                drawCircle(
                    color = Color.White.copy(alpha = 0.2f),
                    radius = 1.5.dp.toPx(),
                    center = pt
                )
            }
        }

        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header bar containing parsed badge and close control button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = badgeText,
                        tint = badgeColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = badgeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = badgeColor,
                        letterSpacing = 0.5.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.07f))
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Large center scanner target/decoded emblem
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "Code Diagnostic Successful",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "DECODER INTEGRATED",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Main output text container with deep liquid-molded gloss background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlass(RoundedCornerShape(16.dp), bgAlpha = 0.15f)
                    .padding(14.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Decoded Coordinates Block:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = scannedText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Highly responsive and tactile action buttons list
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Tactical Copy Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .glassTouchFeedback {
                            clipboardManager.setText(AnnotatedString(scannedText))
                            isCopied = true
                            Toast.makeText(context, "Copied payload to clipboard", Toast.LENGTH_SHORT).show()
                        }
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isCopied) Color(0xFF00FFCC).copy(alpha = 0.15f)
                            else Color.White.copy(alpha = 0.08f)
                        )
                        .border(
                            width = 1.dp,
                            color = if (isCopied) Color(0xFF00FFCC) else Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = "Copy text",
                            tint = if (isCopied) Color(0xFF00FFCC) else Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isCopied) "Copied!" else "Copy",
                            fontWeight = FontWeight.Bold,
                            color = if (isCopied) Color(0xFF00FFCC) else Color.White,
                            fontSize = 12.sp
                        )
                    }
                }

                // Interactive Smart Action Button (Browse Link, Initiate Pay, Import, or Share)
                val destText = when {
                    isUrl -> "Browse"
                    isUpi -> "Transfer"
                    isWifi -> "Connect"
                    isVcard -> "Save Bio"
                    else -> "Share"
                }
                
                val destIcon = when {
                    isUrl -> Icons.Default.Language
                    isUpi -> Icons.Default.AccountBalanceWallet
                    isWifi -> Icons.Default.Wifi
                    isVcard -> Icons.Default.ContactPage
                    else -> Icons.Default.Share
                }

                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .glassTouchFeedback {
                            when {
                                isUrl -> {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(scannedText))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Cannot open web page", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                isUpi -> {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(scannedText))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "No app found to process payment url", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                else -> {
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, scannedText)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share QR Payload"))
                                }
                            }
                        }
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = destIcon,
                            contentDescription = "Context action icon",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = destText,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
