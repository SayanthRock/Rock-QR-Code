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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

    var showClearConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
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
                color = themeConfig.textColor,
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
            color = themeConfig.subTextColor,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 16.dp)
        )

        // SEARCH INPUT FILTER BAR
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.searchQuery.value = it },
            placeholder = { Text("Search logs by title, content, or type...", color = themeConfig.subTextColor) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = themeConfig.subTextColor) },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear Search", tint = themeConfig.textColor)
                    }
                }
            } else null,
            modifier = Modifier.fillMaxWidth().testTag("search_bar"),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedTextColor = themeConfig.textColor,
                unfocusedTextColor = themeConfig.textColor,
                focusedContainerColor = themeConfig.containerBgColor,
                unfocusedContainerColor = themeConfig.containerBgColor,
                focusedIndicatorColor = primaryColor,
                unfocusedIndicatorColor = themeConfig.textColor.copy(alpha = 0.10f)
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
                            else themeConfig.containerBgColor
                        )
                        .border(
                            1.dp,
                            if (isSelected) primaryColor.copy(alpha = 0.50f)
                            else themeConfig.textColor.copy(alpha = 0.08f),
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
                        color = if (isSelected) primaryColor else themeConfig.subTextColor
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
                        .background(themeConfig.containerBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = "Empty History",
                        tint = themeConfig.subTextColor.copy(alpha = 0.35f),
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No records in cache",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeConfig.textColor.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (searchQuery.isNotEmpty()) "Adjust your filters or search statement." else "Generate or scan physical codes to store them here indefinitely.",
                    fontSize = 12.sp,
                    color = themeConfig.subTextColor,
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
            title = { Text("Purge History Log?", color = themeConfig.textColor, fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently clear all generated and scanned QR history from your offline device space. This operation is irreversible.", color = themeConfig.subTextColor) },
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
                    Text("Cancel", color = themeConfig.subTextColor)
                }
            },
            containerColor = if (themeConfig.isDark) Color(0xFF1E1E1E) else Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun HistoryCardItem(
    record: QrRecord,
    primaryColor: Color,
    onFavoriteToggle: () -> Unit,
    onDelete: () -> Unit,
    onCopyText: (String) -> Unit
) {
    val context = LocalContext.current
    val themeConfig = LiquidGlassTheme.LocalConfig.current
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
                color = themeConfig.textColor.copy(alpha = 0.08f),
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("history_record_item"),
        colors = CardDefaults.cardColors(
            containerColor = themeConfig.containerBgColor
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
                        color = themeConfig.textColor,
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
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = record.content,
                    color = themeConfig.subTextColor,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = dateString,
                    color = themeConfig.subTextColor.copy(alpha = 0.6f),
                    fontSize = 10.sp
                )
            }

            // ACTIONS BUTTONS COLUMN OR ROW
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // COPY ACTIONS
                IconButton(onClick = { onCopyText(record.content) }) {
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = "Copy Content",
                        tint = themeConfig.subTextColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // SHARE COMPANION LINK
                IconButton(onClick = {
                    HapticUtils.vibrate(context, 20)
                    val rawContent = record.content
                    val title = record.title
                    val type = record.type
                    val webLink = "https://sayanthrock.github.io/Rock-QR-Code/share/?content=${android.net.Uri.encode(rawContent)}&type=$type&title=${android.net.Uri.encode(title)}"

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
                        tint = themeConfig.subTextColor,
                        modifier = Modifier.size(17.dp)
                    )
                }

                // BOOKMARK SWITCH
                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        imageVector = if (record.isFavorite) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Toggle bookmark",
                        tint = if (record.isFavorite) Color(0xFFFFB703) else themeConfig.subTextColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // ERASE FROM LOG
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete Record",
                        tint = themeConfig.subTextColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
