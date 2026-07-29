package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.utils.HapticUtils
import com.example.viewmodel.QRViewModel

@Composable
fun DraggableFloatingActionBar(
    viewModel: QRViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activeTab by viewModel.activeTab.collectAsState()

    val isScanSelected = activeTab == "SCAN"
    val isGenSelected = activeTab == "GENERATE"
    val isHistSelected = activeTab == "HISTORY"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(bottom = 20.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            shape = RoundedCornerShape(36.dp),
            color = Color(0xFF1E1E22),
            shadowElevation = 12.dp,
            modifier = Modifier
                .wrapContentSize()
                .testTag("fixed_floating_action_bar")
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // SCAN TAB
                Box(
                    modifier = Modifier
                        .testTag("fab_tab_scan")
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (isScanSelected) Color.White else Color.Transparent)
                        .clickable {
                            HapticUtils.vibrate(context, 25)
                            viewModel.selectTab("SCAN")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Scan",
                        tint = if (isScanSelected) Color.Black else Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(22.dp)
                    )
                }

                // CENTER GENERATE + BUTTON
                Box(
                    modifier = Modifier
                        .testTag("fab_tab_generate")
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable {
                            HapticUtils.vibrate(context, 30)
                            viewModel.selectTab("GENERATE")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Generate Code",
                        tint = Color.Black,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // HISTORY TAB
                Box(
                    modifier = Modifier
                        .testTag("fab_tab_history")
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (isHistSelected) Color.White else Color.Transparent)
                        .clickable {
                            HapticUtils.vibrate(context, 25)
                            viewModel.selectTab("HISTORY")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "History",
                        tint = if (isHistSelected) Color.Black else Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

