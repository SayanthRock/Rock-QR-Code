package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.QrRecord
import com.example.ui.theme.LiquidGlassTheme
import com.example.ui.components.PremiumLoadingAnimation
import com.example.utils.HapticUtils
import com.example.utils.QRGenerator

import com.example.viewmodel.CustomToastType
import com.example.viewmodel.QRViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: QRViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activePreset by viewModel.colorPreset.collectAsState()
    val themeConfig = LiquidGlassTheme.LocalConfig.current
    val primaryColor = themeConfig.primaryColor

    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterType by viewModel.historyFilterType.collectAsState()
    val records by viewModel.historyRecords.collectAsState()

    val isShowingAnalytics by viewModel.isShowingAnalytics.collectAsState()
    val selectedAnalyticsRecord by viewModel.selectedRecordForAnalytics.collectAsState()
    val analyticsData by viewModel.currentAnalyticsData.collectAsState()
    val isLoadingAnalytics by viewModel.isLoadingAnalytics.collectAsState()

    var showClearConfirm by remember { mutableStateOf(false) }
    var selectedDetailRecord by remember { mutableStateOf<QrRecord?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        // TOP HEADER ROW WITH GEAR SETTINGS BUTTON
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "History",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    fontFamily = FontFamily.SansSerif,
                    modifier = Modifier.testTag("history_title")
                )
                Text(
                    text = "Scanned & Generated history",
                    fontSize = 13.sp,
                    color = Color(0xFFA1A1AA),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF27272A))
                    .clickable {
                        HapticUtils.vibrate(context, 20)
                        viewModel.selectTab("SETTINGS")
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // SEGMENTED TAB SELECTOR (Scanned vs Generated)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF18181B))
                .padding(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                val filterScanned = filterType == "SCANNED"
                val filterGenerated = filterType == "GENERATED" || filterType == "ALL"

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (filterScanned) Color.White else Color.Transparent)
                        .clickable {
                            HapticUtils.vibrate(context, 15)
                            viewModel.historyFilterType.value = "SCANNED"
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Scanned",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (filterScanned) Color.Black else Color(0xFFA1A1AA)
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (filterGenerated) Color.White else Color.Transparent)
                        .clickable {
                            HapticUtils.vibrate(context, 15)
                            viewModel.historyFilterType.value = "GENERATED"
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Generated",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (filterGenerated) Color.Black else Color(0xFFA1A1AA)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))


            // RECORDS EMPTY STATE vs LIST VIEW
            if (records.isEmpty()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(bottom = 80.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = "Empty History",
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No records in cache",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "Adjust your filters or search statement." else "Generate or scan physical codes to store them here indefinitely.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag("history_list"),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 120.dp) // Leaves space for bar
                ) {
                    items(records, key = { it.id }) { record ->
                        HistoryCardItem(
                            record = record,
                            primaryColor = primaryColor,
                            onFavoriteToggle = { viewModel.toggleFavorite(record) },
                            onDelete = { viewModel.deleteRecord(record) },
                            onCopyText = { text ->
                                HapticUtils.vibrate(context, 20)
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Copied History Core", text)
                                clipboard.setPrimaryClip(clip)
                                viewModel.showToast("Copied to clipboard", CustomToastType.SUCCESS)
                            },
                            onShowAnalytics = {
                                HapticUtils.vibrate(context, 35)
                                viewModel.fetchAnalytics(record)
                            },
                            onCardClick = {
                                selectedDetailRecord = record
                            }
                        )
                    }
                }
            }
        }

        // QR RECORD DETAIL BOTTOM SHEET
        if (selectedDetailRecord != null) {
            QrDetailBottomSheet(
                record = selectedDetailRecord!!,
                onDismiss = { selectedDetailRecord = null },
                onDelete = {
                    viewModel.deleteRecord(selectedDetailRecord!!)
                    selectedDetailRecord = null
                },
                onCopy = {
                    HapticUtils.vibrate(context, 20)
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Copied QR Content", selectedDetailRecord!!.content)
                    clipboard.setPrimaryClip(clip)
                    viewModel.showToast("Copied link to clipboard", CustomToastType.SUCCESS)
                }
            )
        }


        // PURGE CONFIRMATION BOX
        if (showClearConfirm) {
            AlertDialog(
                onDismissRequest = { showClearConfirm = false },
                title = { Text("Purge History Log?", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) },
                text = { Text("This will permanently clear all generated and scanned QR history from your offline device space. This operation is irreversible.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        onClick = {
                            showClearConfirm = false
                            viewModel.clearAllLogs()
                        }
                    ) {
                        Text("Delete Everything", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearConfirm = false }) {
                        Text("Cancel", color = MaterialTheme.colorScheme.primary)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp)
            )
        }

        // SLIDING BOTTOM SHEET FOR ANALYTICS DASHBOARD
        AnimatedVisibility(
            visible = isShowingAnalytics && selectedAnalyticsRecord != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            selectedAnalyticsRecord?.let { record ->
                AnalyticsDashboardSheet(
                    record = record,
                    analyticsData = analyticsData,
                    isLoading = isLoadingAnalytics,
                    primaryColor = primaryColor,
                    onDismiss = { viewModel.isShowingAnalytics.value = false }
                )
            }
        }
    }
}

@Composable
fun HistoryCardItem(
    record: QrRecord,
    primaryColor: Color,
    onFavoriteToggle: () -> Unit,
    onDelete: () -> Unit,
    onCopyText: (String) -> Unit,
    onShowAnalytics: () -> Unit,
    onCardClick: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                HapticUtils.vibrate(context, 20)
                onCardClick()
            }
            .testTag("history_record_item"),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF18181B)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Text(
                    text = record.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = record.content,
                    fontSize = 12.sp,
                    color = Color(0xFFA1A1AA),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF3B1825))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = record.type,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF75C3)
                        )
                    }

                    // Source Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF152B42))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (record.isScanned) "SCAN" else "GENERATE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8)
                        )
                    }

                    // Time Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF143224))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "a moment ago",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4ADE80)
                        )
                    }
                }
            }

            // QR CODE THUMBNAIL IN WHITE SQUARE FRAME
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                val thumbBitmap = remember(record.content) {
                    QRGenerator.generate(
                        text = record.content,
                        style = record.selectedStyle
                    )
                }

                Image(
                    bitmap = thumbBitmap.asImageBitmap(),
                    contentDescription = "QR Thumbnail",
                    modifier = Modifier.fillMaxSize()
                )
            }

        }
    }
}

@Composable
fun QrDetailBottomSheet(
    record: QrRecord,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.70f))
            .clickable(onClick = onDismiss)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color(0xFF18181B))
                .clickable(enabled = false) {}
                .padding(20.dp)
        ) {
            // Drag handle pill
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF3F3F46))
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Badges row
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF3B1825))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = record.type,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF75C3)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF152B42))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = if (record.isScanned) "SCAN" else "GENERATE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF143224))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "a moment ago",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4ADE80)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Header Row (Title + Edit icon left, QR Thumbnail right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f).padding(end = 12.dp)
                ) {
                    Text(
                        text = record.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Title",
                        tint = Color(0xFFA1A1AA),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val thumbBitmap = remember(record.content) {
                        QRGenerator.generate(
                            text = record.content,
                            style = record.selectedStyle
                        )
                    }

                    Image(
                        bitmap = thumbBitmap.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier.fillMaxSize()
                    )
                }

            }

            Spacer(modifier = Modifier.height(20.dp))

            // Content Container Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF27272A))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = record.content,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Secure Connection • HTTPS",
                                fontSize = 11.sp,
                                color = Color(0xFFA1A1AA)
                            )
                        }
                    }

                    IconButton(onClick = onCopy) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Link",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Primary Button: Open in Browser
            Button(
                onClick = {
                    HapticUtils.vibrate(context, 20)
                    try {
                        val uri = android.net.Uri.parse(record.content)
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // Ignore invalid URIs
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Open in Browser", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Secondary Buttons Row (Delete & Share)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        onDelete()
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF27272A),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delete", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = {
                        HapticUtils.vibrate(context, 20)
                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_TEXT, record.content)
                        }
                        context.startActivity(android.content.Intent.createChooser(intent, "Share QR Code"))
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF27272A),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}


@Composable
fun AnalyticsDashboardSheet(
    record: QrRecord,
    analyticsData: com.example.data.api.AnalyticsResponse?,
    isLoading: Boolean,
    primaryColor: Color,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.70f))
            .clickable(enabled = true, onClick = onDismiss)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(primaryColor.copy(alpha = 0.3f), Color.Transparent)
                    ),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                )
                .clickable(enabled = false) {}
                .padding(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Redirection Analytics",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = record.title,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = {
                        HapticUtils.vibrate(context, 15)
                        onDismiss()
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Dashboard",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    PremiumLoadingAnimation(color = primaryColor)
                }
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                ) {
                    // Big Counters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Total Scans Card
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(primaryColor.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                                .border(1.dp, primaryColor.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = (analyticsData?.scanCount ?: record.scanCount).toString(),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                            Text(
                                text = "Total Scans",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Unique visitors Card
                        val distinctCount = analyticsData?.scans?.map { it.visitorId }?.distinct()?.size ?: 0
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0xFF06D6A0).copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                                .border(1.dp, Color(0xFF06D6A0).copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (distinctCount > 0) distinctCount.toString() else "—",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF06D6A0)
                            )
                            Text(
                                text = "Unique Users",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    val scans = analyticsData?.scans ?: emptyList()

                    if (scans.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Awaiting first redirect scan trigger...",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    } else {
                        // OS Distribution Section
                        AnalyticsMetricSection(
                            title = "User Operating Systems",
                            groupData = scans.groupBy { it.os }.mapValues { it.value.size },
                            total = scans.size,
                            accentColor = primaryColor
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Browser Distribution Section
                        AnalyticsMetricSection(
                            title = "Browser Clients",
                            groupData = scans.groupBy { it.browser }.mapValues { it.value.size },
                            total = scans.size,
                            accentColor = Color(0xFFFFB703)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Locations Section
                        AnalyticsMetricSection(
                            title = "Visitor Geolocation",
                            groupData = scans.groupBy { it.location }.mapValues { it.value.size },
                            total = scans.size,
                            accentColor = Color(0xFF2196F3)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Live activity logs list
                        Text(
                            text = "Real-Time Scan Log",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        val formatter = remember { SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault()) }
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            scans.forEach { scan ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = scan.visitorId,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${scan.os} • ${scan.browser}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = scan.location,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = primaryColor
                                        )
                                        Text(
                                            text = formatter.format(Date(scan.timestamp)),
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
}

@Composable
fun AnalyticsMetricSection(
    title: String,
    groupData: Map<String, Int>,
    total: Int,
    accentColor: Color
) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 12.dp)
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        groupData.entries.sortedByDescending { it.value }.forEach { (name, count) ->
            val percentage = if (total > 0) count.toFloat() / total else 0f
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "$count (${(percentage * 100).toInt()}%)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(percentage)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                }
            }
        }
    }
}
