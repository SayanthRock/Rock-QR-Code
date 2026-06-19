package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.QRViewModel
import kotlinx.coroutines.delay

// Information on Liquid Presets for the display terminal
data class PresetColorDetail(
    val key: String,
    val name: String,
    val primaryHex: String,
    val secondaryHex: String,
    val primaryColor: Color,
    val secondaryColor: Color
)

val AndroidPresetsInfo = listOf(
    PresetColorDetail("MIDNIGHT", "Midnight Obsidian", "#9D4EED", "#5A189A", Color(0xFF9D4EED), Color(0xFF5A189A)),
    PresetColorDetail("ARCTIC", "Arctic Glacier", "#8ECAE6", "#219EBC", Color(0xFF8ECAE6), Color(0xFF219EBC)),
    PresetColorDetail("OCEAN", "Abyssal Ocean", "#00B4D8", "#03045E", Color(0xFF00B4D8), Color(0xFF03045E)),
    PresetColorDetail("AURORA", "Aurora Glow", "#00FFCC", "#009688", Color(0xFF00FFCC), Color(0xFF009688)),
    PresetColorDetail("EMERALD", "Emerald Forest", "#10B981", "#047857", Color(0xFF10B981), Color(0xFF047857))
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LiquidThemeControlPanel(
    viewModel: QRViewModel,
    modifier: Modifier = Modifier
) {
    val activePreset by viewModel.colorPreset.collectAsState()
    var isExpanded by remember { mutableStateOf(false) }
    var isAutoPlaying by remember { mutableStateOf(false) }

    val currentDetail = AndroidPresetsInfo.find { it.key == activePreset.uppercase() } ?: AndroidPresetsInfo[0]

    // Automated colors loop logic
    LaunchedEffect(isAutoPlaying) {
        if (isAutoPlaying) {
            while (isAutoPlaying) {
                delay(3000)
                val currIdx = AndroidPresetsInfo.indexOfFirst { it.key == activePreset.uppercase() }
                val nextIdx = if (currIdx == -1) 0 else (currIdx + 1) % AndroidPresetsInfo.size
                viewModel.setColorPreset(AndroidPresetsInfo[nextIdx].key)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedContent(
            targetState = isExpanded,
            transitionSpec = {
                (slideInVertically { it / 2 } + fadeIn() togetherWith
                        slideOutVertically { it / 2 } + fadeOut())
                    .using(SizeTransform(clip = false))
            },
            label = "ControlPanelExpansion"
        ) { expanded ->
            if (!expanded) {
                // Compact Floating Badge (Retracted view)
                Box(
                    modifier = Modifier
                        .liquidGlass(shape = RoundedCornerShape(80.dp), bgAlpha = 0.2f)
                        .clickable { isExpanded = true }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(12.dp)
                                .border(1.dp, Color.White, CircleShape),
                            color = currentDetail.primaryColor,
                            shape = CircleShape
                        ) {}
                        
                        Text(
                            text = "LIQUID CODES: ${currentDetail.name.uppercase()}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = 1.sp
                        )

                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Expand control panel",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            } else {
                // Elite expanded HUD dashboard
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlass(shape = RoundedCornerShape(26.dp), bgAlpha = 0.35f)
                        .border(
                            width = 1.6.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    currentDetail.primaryColor.copy(alpha = 0.6f),
                                    currentDetail.secondaryColor.copy(alpha = 0.1f),
                                    Color.White.copy(alpha = 0.15f)
                                )
                            ),
                            shape = RoundedCornerShape(26.dp)
                        )
                        .padding(18.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Title row and Close button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "LIQUID CODES HUD",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace,
                                    color = currentDetail.primaryColor,
                                    letterSpacing = 1.5.sp
                                )
                                Text(
                                    text = "Dynamic Color Cockpit",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Auto looping trigger badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(40.dp))
                                        .background(
                                            if (isAutoPlaying) currentDetail.primaryColor.copy(alpha = 0.2f)
                                            else Color.White.copy(alpha = 0.05f)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isAutoPlaying) currentDetail.primaryColor else Color.White.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(40.dp)
                                        )
                                        .clickable { isAutoPlaying = !isAutoPlaying }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        // Simple pulse dot animation
                                        val infiniteTrans = rememberInfiniteTransition(label = "pulse_play")
                                        val pulseScale by infiniteTrans.animateFloat(
                                            initialValue = 0.8f,
                                            targetValue = 1.3f,
                                            animationSpec = infiniteRepeatable(
                                                animation = tween(1200, easing = FastOutSlowInEasing),
                                                repeatMode = RepeatMode.Reverse
                                            ),
                                            label = "scaling_dot"
                                        )

                                        Surface(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .scale(if (isAutoPlaying) pulseScale else 1f),
                                            color = if (isAutoPlaying) currentDetail.primaryColor else Color.White.copy(alpha = 0.4f),
                                            shape = CircleShape
                                        ) {}

                                        Text(
                                            text = if (isAutoPlaying) "LOOP ON" else "AUTO LOOP",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontFamily = FontFamily.Monospace,
                                            color = if (isAutoPlaying) currentDetail.primaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                }

                                // Main retraction dismiss icon
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.08f))
                                        .clickable { isExpanded = false },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Collapse control panel",
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }

                        // Terminal console display board
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(
                                        text = "console.theme",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = currentDetail.primaryColor
                                    )
                                    Text(
                                        text = "active",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = Color(0xFF00FFCC)
                                    )
                                }
                                Divider(color = Color.White.copy(alpha = 0.05f), thickness = 0.5.dp)
                                Text(
                                    text = ">> --theme-primary: ${currentDetail.primaryHex}",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                                )
                                Text(
                                    text = ">> --theme-secondary: ${currentDetail.secondaryHex}",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                                )
                                Text(
                                    text = ">> themePreset: \"${currentDetail.key}\"",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                                )
                            }
                        }

                        // Swatches selectors
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AndroidPresetsInfo.forEach { preset ->
                                    val isSel = activePreset.uppercase() == preset.key
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(preset.primaryColor)
                                            .border(
                                                width = if (isSel) 2.dp else 1.dp,
                                                color = if (isSel) Color.White else Color.White.copy(alpha = 0.25f),
                                                shape = CircleShape
                                            )
                                            .clickable {
                                                viewModel.setColorPreset(preset.key)
                                            }
                                    )
                                }
                            }

                            // Dynamic arrows to cycle manual
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White.copy(alpha = 0.08f))
                                        .clickable {
                                            val currIdx = AndroidPresetsInfo.indexOfFirst { it.key == activePreset.uppercase() }
                                            val prevIdx = if (currIdx <= 0) AndroidPresetsInfo.size - 1 else currIdx - 1
                                            viewModel.setColorPreset(AndroidPresetsInfo[prevIdx].key)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Prev Preset",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White.copy(alpha = 0.08f))
                                        .clickable {
                                            val currIdx = AndroidPresetsInfo.indexOfFirst { it.key == activePreset.uppercase() }
                                            val nextIdx = (currIdx + 1) % AndroidPresetsInfo.size
                                            viewModel.setColorPreset(AndroidPresetsInfo[nextIdx].key)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = "Next Preset",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
