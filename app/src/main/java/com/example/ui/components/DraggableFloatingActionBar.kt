package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.QrViewModel
import kotlin.math.roundToInt

@Composable
fun DraggableFloatingActionBar(
    viewModel: QrViewModel,
    modifier: Modifier = Modifier
) {
    val activeTab by viewModel.activeTab.collectAsState()
    val activePreset by viewModel.colorPreset.collectAsState()

    val currentDetail = AndroidPresetsInfo.find { it.key == activePreset.uppercase() } ?: AndroidPresetsInfo[0]

    // Toggle state: Locked (fixed position at bottom-center) vs Unlocked (free draggable floating)
    var isLocked by remember { mutableStateOf(true) }

    // User-drag offsets when unlocked
    var dragOffsetX by remember { mutableStateOf(0f) }
    var dragOffsetY by remember { mutableStateOf(0f) }

    // Spring animation for smooth snap integration
    val animatedOffsetX by animateFloatAsState(
        targetValue = if (isLocked) 0f else dragOffsetX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "drag_snap_x"
    )
    val animatedOffsetY by animateFloatAsState(
        targetValue = if (isLocked) 0f else dragOffsetY,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "drag_snap_y"
    )

    // Interactive preset changer rotation state
    var isRotating by remember { mutableStateOf(0f) }
    val rotationAnimation by animateFloatAsState(
        targetValue = isRotating,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow),
        label = "theme_wheel_rotation"
    )

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val widthPx = with(LocalDensity.current) { constraints.maxWidth.toFloat() }
        val heightPx = with(LocalDensity.current) { constraints.maxHeight.toFloat() }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 86.dp) // Offset above standard tab navigator layout
                .offset {
                    IntOffset(
                        animatedOffsetX.roundToInt(),
                        animatedOffsetY.roundToInt()
                    )
                }
                .pointerInput(isLocked) {
                    if (!isLocked) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            // Bind drag offset to screen boundary cushions (so it does not get dragged off-screen)
                            dragOffsetX = (dragOffsetX + dragAmount.x).coerceIn(-widthPx / 2.2f, widthPx / 2.2f)
                            dragOffsetY = (dragOffsetY + dragAmount.y).coerceIn(-heightPx / 1.15f, heightPx / 12f)
                        }
                    }
                }
        ) {
            // Main Glass Cockpit Floating Control Deck
            Row(
                modifier = Modifier
                    .wrapContentSize()
                    .liquidGlass(shape = RoundedCornerShape(32.dp), bgAlpha = 0.38f)
                    .border(
                        width = 1.6.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                currentDetail.primaryColor.copy(alpha = 0.7f),
                                currentDetail.secondaryColor.copy(alpha = 0.15f),
                                Color.White.copy(alpha = 0.22f)
                            )
                        ),
                        shape = RoundedCornerShape(32.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Tactile Drag Indicator Grip & Lock/Unlock Smart Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = if (isLocked) 0.04f else 0.12f))
                        .clickable {
                            isLocked = !isLocked
                            if (isLocked) {
                                // Reset position with snap animation when locked back
                                dragOffsetX = 0f
                                dragOffsetY = 0f
                            }
                        }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    // Pull Handle dots
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        repeat(3) {
                            Row(horizontalArrangement = Arrangement.spacedBy(1.5.dp)) {
                                repeat(2) {
                                    Box(
                                        modifier = Modifier
                                            .size(3.dp)
                                            .background(
                                                if (isLocked) Color.White.copy(alpha = 0.3f)
                                                else currentDetail.primaryColor,
                                                CircleShape
                                            )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    Icon(
                        imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = "Control bar locking status indicator",
                        tint = if (isLocked) Color.White.copy(alpha = 0.5f) else currentDetail.primaryColor,
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Tiny glowing dividing element
                Box(
                    modifier = Modifier
                        .height(20.dp)
                        .width(1.dp)
                        .background(Color.White.copy(alpha = 0.12f))
                )

                // Views control toggler: Segmented Glass Selector Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.Black.copy(alpha = 0.18f))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // SCAN mode option tag
                    val isScanSelected = activeTab == "SCAN"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isScanSelected) currentDetail.primaryColor.copy(alpha = 0.22f)
                                else Color.Transparent
                            )
                            .border(
                                width = 1.dp,
                                color = if (isScanSelected) currentDetail.primaryColor.copy(alpha = 0.45f) else Color.Transparent,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { viewModel.selectTab("SCAN") }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Active scanning environment lens",
                                tint = if (isScanSelected) currentDetail.primaryColor else Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "SCANNER",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isScanSelected) Color.White else Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }

                    // GENERATE mode option tag
                    val isGenSelected = activeTab == "GENERATE"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isGenSelected) currentDetail.primaryColor.copy(alpha = 0.22f)
                                else Color.Transparent
                            )
                            .border(
                                width = 1.dp,
                                color = if (isGenSelected) currentDetail.primaryColor.copy(alpha = 0.45f) else Color.Transparent,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { viewModel.selectTab("GENERATE") }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = "Qr generator engine tag",
                                tint = if (isGenSelected) currentDetail.primaryColor else Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "GENERATOR",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isGenSelected) Color.White else Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                // Tiny glowing dividing element
                Box(
                    modifier = Modifier
                        .height(20.dp)
                        .width(1.dp)
                        .background(Color.White.copy(alpha = 0.12f))
                )

                // Themes quick cycle selector wheel button
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(currentDetail.primaryColor.copy(alpha = 0.12f))
                        .border(1.dp, currentDetail.primaryColor.copy(alpha = 0.35f), CircleShape)
                        .clickable {
                            // Cycle to the next preset color theme
                            isRotating += 72f // play smooth rotating visual action
                            val currIdx = AndroidPresetsInfo.indexOfFirst { it.key == activePreset.uppercase() }
                            val nextIdx = if (currIdx == -1) 0 else (currIdx + 1) % AndroidPresetsInfo.size
                            viewModel.setColorPreset(AndroidPresetsInfo[nextIdx].key)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Cycle custom color themes preset option",
                        tint = currentDetail.primaryColor,
                        modifier = Modifier
                            .size(16.dp)
                            .scale(rotationAnimation.let { 1f + (it % 72f) / 180f }) // dynamic micro-bounce
                    )
                }
            }
        }
    }
}
