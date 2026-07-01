package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.*
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
import com.example.utils.HapticUtils
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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TOP ARCHIVE TITLE AND CLEAN BUTTON
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Core Archive",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = FontFamily.SansSerif,
                    modifier = Modifier.testTag("history_title")
                )

                if (records.isNotEmpty() || filterType != "ALL" || searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            HapticUtils.vibrate(context, 40)
                            showClearConfirm = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear All Archives",
                            tint = Color.Red.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Text(
                text = "Track, review, or bookmark your scanner history cache.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 16.dp)
            )

            // SEARCH INPUT FILTER BAR
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                placeholder = { Text("Search logs by title, content, or type...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)) },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear Search", tint = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                } else null,
                modifier = Modifier.fillMaxWidth().testTag("search_bar"),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                    focusedIndicatorColor = primaryColor,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // FILTER PILLS BAR
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val filters = listOf(
                    Pair("ALL", "All"),
                    Pair("SCANNED", "Scanned"),
                    Pair("GENERATED", "Forged"),
                    Pair("FAVORITE", "Favs")
                )

                filters.forEach { (key, label) ->
                    val isSelected = filterType == key
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) primaryColor.copy(alpha = 0.20f)
                                else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)
                            )
                            .border(
                                1.dp,
                                if (isSelected) primaryColor.copy(alpha = 0.50f)
                                else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                HapticUtils.vibrate(context, 15)
                                viewModel.historyFilterType.value = key
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
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
                            }
                        )
                    }
                }
            }
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
    onShowAnalytics: () -> Unit
) {
    val context = LocalContext.current
    val formatter = remember { SimpleDateFormat("MMM d, yyyy - hh:mm a", Locale.getDefault()) }
    val dateString = remember(record.timestamp) { formatter.format(Date(record.timestamp)) }

    val icon = when (record.type) {
        "URL" -> Icons.Default.Launch
        "WIFI" -> Icons.Default.Wifi
        "CONTACT" -> Icons.Default.Person
        "EMAIL" -> Icons.Default.Mail
        "PHONE" -> Icons.Default.Phone
        else -> Icons.Default.AlternateEmail
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f),
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("history_record_item"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // INDICATOR TYPE LOGO WITH BACKDROP
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (record.isScanned) Color(0xFF06D6A0).copy(alpha = 0.10f)
                        else primaryColor.copy(alpha = 0.10f)
                    )
                    .border(
                        1.dp,
                        if (record.isScanned) Color(0xFF06D6A0).copy(alpha = 0.3f)
                        else primaryColor.copy(alpha = 0.3f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = record.type,
                    tint = if (record.isScanned) Color(0xFF06D6A0) else primaryColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // TITLE AND DETAILS
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = record.title,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // SCAN vs FORGED ACCENTS
                    Text(
                        text = if (record.isScanned) "SCANNED" else "FORGED",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp,
                        color = if (record.isScanned) Color(0xFF06D6A0).copy(alpha = 0.8f) else primaryColor,
                        modifier = Modifier
                            .background(
                                color = if (record.isScanned) Color(0xFF06D6A0).copy(alpha = 0.08f) else primaryColor.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )

                    if (record.isDynamic) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "DYNAMIC",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp,
                            color = Color(0xFFFFB703),
                            modifier = Modifier
                                .background(
                                    color = Color(0xFFFFB703).copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = record.content,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = dateString,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    fontSize = 10.sp
                )
            }

            // ACTIONS BUTTONS COLUMN OR ROW
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // ANALYTICS DASHBOARD ACTION FOR DYNAMIC CODES
                if (record.isDynamic) {
                    IconButton(onClick = onShowAnalytics) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "View Live Analytics",
                            tint = Color(0xFFFFB703),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // COPY ACTIONS
                IconButton(onClick = { onCopyText(record.content) }) {
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = "Copy Content",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // SHARE COMPANION LINK
                IconButton(onClick = {
                    HapticUtils.vibrate(context, 20)
                    val rawContent = record.content
                    val title = record.title
                    val type = record.type
                    val webLink = "https://sayanthrock.github.io/Rock-QR-Code/share?content=${android.net.Uri.encode(rawContent)}&type=$type&title=${android.net.Uri.encode(title)}"

                    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        this.type = "text/plain"
                        putExtra(android.content.Intent.EXTRA_SUBJECT, "Import Rock QR Code: $title")
                        putExtra(android.content.Intent.EXTRA_TEXT, "Look at my Rock-forged QR code: $webLink")
                    }
                    context.startActivity(android.content.Intent.createChooser(intent, "Share Companion Web Link"))
                }) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Share Web Link",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(17.dp)
                    )
                }

                // BOOKMARK SWITCH
                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        imageVector = if (record.isFavorite) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Toggle bookmark",
                        tint = if (record.isFavorite) Color(0xFFFFB703) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // ERASE FROM LOG
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete Record",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(18.dp)
                    )
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
                    CircularProgressIndicator(color = primaryColor)
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
