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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.QRViewModel
import com.example.ui.theme.LiquidGlassTheme

@Composable
fun DraggableFloatingActionBar(
    viewModel: QRViewModel,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activeTab by viewModel.activeTab.collectAsState()
    val activePreset by viewModel.colorPreset.collectAsState()

    val themeConfig = com.example.ui.theme.LiquidGlassTheme.LocalConfig.current
    val primaryColor = themeConfig.primaryColor
    val secondaryColor = themeConfig.secondaryColor

    var isRotating by remember { mutableStateOf(0f) }
    val rotationAnimation by animateFloatAsState(
        targetValue = isRotating,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessLow),
        label = "palette_spin"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(bottom = 16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .wrapContentSize()
                .testTag("fixed_floating_action_bar")
                .liquidGlass(shape = RoundedCornerShape(28.dp), bgAlpha = 0.40f)
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.70f),
                            secondaryColor.copy(alpha = 0.20f),
                            primaryColor.copy(alpha = 0.60f)
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.Black.copy(alpha = 0.25f))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // SCAN TAB
                val isScanSelected = activeTab == "SCAN"
                Box(
                    modifier = Modifier
                        .testTag("fab_tab_scan")
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            if (isScanSelected) primaryColor.copy(alpha = 0.22f)
                            else Color.Transparent
                        )
                        .border(
                            width = 1.dp,
                            color = if (isScanSelected) primaryColor.copy(alpha = 0.50f) else Color.Transparent,
                            shape = RoundedCornerShape(18.dp)
                        )
                        .height(44.dp)
                        .clickable {
                            com.example.utils.HapticUtils.vibrate(context, 35)
                            viewModel.selectTab("SCAN")
                        }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scan QR Code",
                            tint = if (isScanSelected) primaryColor else Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "SCAN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isScanSelected) Color.White else Color.White.copy(alpha = 0.5f),
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // GENERATE TAB
                val isGenSelected = activeTab == "GENERATE"
                Box(
                    modifier = Modifier
                        .testTag("fab_tab_generate")
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            if (isGenSelected) primaryColor.copy(alpha = 0.22f)
                            else Color.Transparent
                        )
                        .border(
                            width = 1.dp,
                            color = if (isGenSelected) primaryColor.copy(alpha = 0.50f) else Color.Transparent,
                            shape = RoundedCornerShape(18.dp)
                        )
                        .height(44.dp)
                        .clickable {
                            com.example.utils.HapticUtils.vibrate(context, 35)
                            viewModel.selectTab("GENERATE")
                        }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "Generate QR Code",
                            tint = if (isGenSelected) primaryColor else Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "GEN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isGenSelected) Color.White else Color.White.copy(alpha = 0.5f),
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // HISTORY TAB
                val isHistSelected = activeTab == "HISTORY"
                Box(
                    modifier = Modifier
                        .testTag("fab_tab_history")
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            if (isHistSelected) primaryColor.copy(alpha = 0.22f)
                            else Color.Transparent
                        )
                        .border(
                            width = 1.dp,
                            color = if (isHistSelected) primaryColor.copy(alpha = 0.50f) else Color.Transparent,
                            shape = RoundedCornerShape(18.dp)
                        )
                        .height(44.dp)
                        .clickable {
                            com.example.utils.HapticUtils.vibrate(context, 35)
                            viewModel.selectTab("HISTORY")
                        }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "History Logs",
                            tint = if (isHistSelected) primaryColor else Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "HIST",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isHistSelected) Color.White else Color.White.copy(alpha = 0.5f),
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // SETTINGS TAB
                val isSettingsSelected = activeTab == "SETTINGS"
                Box(
                    modifier = Modifier
                        .testTag("fab_tab_settings")
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            if (isSettingsSelected) primaryColor.copy(alpha = 0.22f)
                            else Color.Transparent
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSettingsSelected) primaryColor.copy(alpha = 0.50f) else Color.Transparent,
                            shape = RoundedCornerShape(18.dp)
                        )
                        .height(44.dp)
                        .clickable {
                            com.example.utils.HapticUtils.vibrate(context, 35)
                            viewModel.selectTab("SETTINGS")
                        }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings Panel",
                            tint = if (isSettingsSelected) primaryColor else Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "SETS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSettingsSelected) Color.White else Color.White.copy(alpha = 0.5f),
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .height(24.dp)
                    .width(1.dp)
                    .background(Color.White.copy(alpha = 0.15f))
            )

            Box(
                modifier = Modifier
                    .testTag("fab_theme_cycler")
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(primaryColor.copy(alpha = 0.15f))
                    .border(1.dp, primaryColor.copy(alpha = 0.40f), CircleShape)
                    .clickable {
                        com.example.utils.HapticUtils.vibrate(context, 50)
                        isRotating += 72f
                        val currIdx = AndroidPresetsInfo.indexOfFirst { it.key == activePreset.uppercase() }
                        val nextIdx = if (currIdx == -1) 0 else (currIdx + 1) % AndroidPresetsInfo.size
                        viewModel.setColorPreset(AndroidPresetsInfo[nextIdx].key)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                     imageVector = Icons.Default.Palette,
                     contentDescription = "Cycle themes",
                     tint = primaryColor,
                     modifier = Modifier
                         .size(18.dp)
                         .scale(rotationAnimation.let { 1f + (it % 72f) / 180f })
                )
            }
        }
    }
}
