package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.utils.HapticUtils
import com.example.viewmodel.QRViewModel
import com.example.viewmodel.CustomToastType

class MainActivity : ComponentActivity() {
    private var qrViewModel: QRViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: QRViewModel = viewModel()
            qrViewModel = viewModel
            val themeMode by viewModel.themeMode.collectAsState()
            val dynamicColorEnabled by viewModel.dynamicColorEnabled.collectAsState()
            val colorPreset by viewModel.colorPreset.collectAsState()

            val useDarkTheme = when (themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            val currentIntent = intent
            LaunchedEffect(currentIntent) {
                handleIntent(currentIntent, viewModel)
            }

            MyApplicationTheme(
                darkTheme = useDarkTheme,
                dynamicColor = dynamicColorEnabled,
                colorPresetName = colorPreset
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .rockFractureBackground(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                ) {
                    LiquidGlassBackground(
                        modifier = Modifier.fillMaxSize(),
                        useDarkTheme = useDarkTheme
                    )

                    MainAppContent(viewModel = viewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        qrViewModel?.let { handleIntent(intent, it) }
    }

    private fun handleIntent(intent: android.content.Intent?, viewModel: QRViewModel) {
        intent?.dataString?.let { dataStr ->
            if (dataStr.contains("sayanthrock.github.io/Rock-QR-Code")) {
                viewModel.importFromDeepLink(dataStr)
                intent.data = null
            }
        }
    }
}

@Composable
fun MainAppContent(
    viewModel: QRViewModel,
    modifier: Modifier = Modifier
) {
    val activeTab by viewModel.activeTab.collectAsState()
    val toastMessage by viewModel.toastEvent.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        // Core View Switcher via crossfade
        Crossfade(
            targetState = activeTab,
            animationSpec = tween(350),
            modifier = Modifier.fillMaxSize(),
            label = "ScreenCrossfade"
        ) { tab ->
            when (tab) {
                "SCAN" -> ScanScreen(viewModel = viewModel)
                "GENERATE" -> GenerateScreen(viewModel = viewModel)
                "HISTORY" -> HistoryScreen(viewModel = viewModel)
                "SETTINGS" -> LocalSettingsScreen(viewModel = viewModel)
            }
        }

        // Custom Bottom Capsule Navigation Panel
        DraggableFloatingActionBar(
            viewModel = viewModel,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Custom High-Fidelity Quartz Toast overlays
        CustomToastOverlay(
            toastMessage = toastMessage,
            onDismiss = { viewModel.clearToast() }
        )
    }
}

@Composable
fun LocalSettingsScreen(
    viewModel: QRViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val activePreset by viewModel.colorPreset.collectAsState()
    val themeConfig = com.example.ui.theme.LiquidGlassTheme.LocalConfig.current
    val primaryColor = themeConfig.primaryColor

    val themeMode by viewModel.themeMode.collectAsState()
    val dynamicColor by viewModel.dynamicColorEnabled.collectAsState()

    val historyEntries by viewModel.historyRecords.collectAsState()
    val favCount = remember(historyEntries) { historyEntries.count { it.isFavorite } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 260.dp), // Clearance for bottom and theme slider drawers
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // TITLE HEADER
        Text(
            text = "Quartz Panel",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            fontFamily = FontFamily.SansSerif,
            modifier = Modifier.fillMaxWidth().testTag("settings_title")
        )

        Text(
            text = "Fine-tune UI metrics, dark theme modes, and manage offline data databases.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 24.dp)
        )

        // LOG STATISTICS CHIPS
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Offline Database Status",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Total Entry Item
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "${historyEntries.size}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        Text(text = "Total Logs", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                    }

                    // Favorites Item
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "$favCount", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFB703))
                        Text(text = "Bookmarks", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // THEME CONFIGURATION LIST
        Text(
            text = "General Preferences",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
        )

        // Theme preference Selector row
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // DARK MODE SWITCH SWITCH
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DarkMode, contentDescription = "Dark Mode", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Theme Scheme", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = if (themeMode == "DARK") "Force dark contrast" else if (themeMode == "LIGHT") "Force light design" else "Default system behavior",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Text-based options switcher
                    Row(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), CircleShape)
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        val modes = listOf("DARK", "LIGHT")
                        modes.forEach { m ->
                            val isMSelected = themeMode == m
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (isMSelected) primaryColor else Color.Transparent)
                                    .clickable {
                                        HapticUtils.vibrate(context, 15)
                                        viewModel.setThemeMode(m)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = m,
                                    color = if (isMSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                // VIBRATION FEEDBACK ROW
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Vibration, contentDescription = "Vibration", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Vibration Feedback", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Enable haptic feedback on actions", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 11.sp)
                        }
                    }

                    var isVibrationEnabled by remember { mutableStateOf(HapticUtils.isVibrationEnabled(context)) }
                    Switch(
                        checked = isVibrationEnabled,
                        onCheckedChange = {
                            isVibrationEnabled = it
                            HapticUtils.setVibrationEnabled(context, it)
                            HapticUtils.vibrate(context, 20)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = primaryColor,
                            checkedTrackColor = primaryColor.copy(alpha = 0.3f),
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            uncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        )
                    )
                }

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                // DYNAMIC COLOR ENGINE ROW
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Palette, contentDescription = "Dynamic Color", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Material You Colors", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Dye fields automatically using wallpaper palette", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 11.sp)
                        }
                    }

                    Switch(
                        checked = dynamicColor,
                        onCheckedChange = {
                            HapticUtils.vibrate(context, 20)
                            viewModel.setDynamicColorEnabled(it)
                            viewModel.showToast(if (it) "Dynamic Colors Switched On" else "Using Premium Presets Custom Colors", CustomToastType.SUCCESS)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = primaryColor,
                            checkedTrackColor = primaryColor.copy(alpha = 0.3f),
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            uncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // DISCLAIMER DETAILS
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = "Secured Offline", tint = Color(0xFF06D6A0), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guaranteed Privacy Protection", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Rock QR Code computes and generates all bitmaps directly on your device. Scanning computations, WiFi credentials, or text fields are processed strictly offline and never uploaded to remote servers. This app is clean, secure, and ad-free.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
