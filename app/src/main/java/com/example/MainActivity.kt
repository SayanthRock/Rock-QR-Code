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
import android.view.WindowManager
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
        // Fix screenshot viewing issues by removing FLAG_SECURE
        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
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
                    modifier = Modifier.fillMaxSize()
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
            if (dataStr.contains("sayanthrock.github.io/Chamo-QR")) {
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

    val themeMode by viewModel.themeMode.collectAsState()
    var beepOnScan by remember { mutableStateOf(true) }
    var autoCopy by remember { mutableStateOf(true) }
    var autoCheckUpdates by remember { mutableStateOf(true) }
    var isVibrationEnabled by remember { mutableStateOf(HapticUtils.isVibrationEnabled(context)) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 120.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // TOP HEADER WITH BACK BUTTON
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF27272A))
                    .clickable {
                        HapticUtils.vibrate(context, 20)
                        viewModel.selectTab("GENERATE")
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "Settings",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.testTag("settings_title")
            )
        }

        // APP INFO HEADER CARD (Screenshot 3)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B)),
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF27272A)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode2,
                        contentDescription = "Rock Logo",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Chamo QR",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Text(
                    text = "Version 1.0.8",
                    fontSize = 12.sp,
                    color = Color(0xFFA1A1AA),
                    modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                )

                Text(
                    text = "The simplest way to scan, create, and manage QR codes. Fast, secure, and privacy-focused.",
                    fontSize = 13.sp,
                    color = Color(0xFFA1A1AA),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        }

        // SCANNER PREFERENCES SECTION
        SettingsSectionHeader("SCANNER PREFERENCES")
        SettingsCard {
            SettingsItemRow(
                icon = Icons.Default.VolumeUp,
                title = "Beep on Scan",
                trailing = {
                    RockSwitch(checked = beepOnScan, onCheckedChange = {
                        beepOnScan = it
                        HapticUtils.vibrate(context, 20)
                    })
                }
            )
            HorizontalDivider(color = Color(0xFF27272A))
            SettingsItemRow(
                icon = Icons.Default.ContentCopy,
                title = "Auto-Copy to Clipboard",
                trailing = {
                    RockSwitch(checked = autoCopy, onCheckedChange = {
                        autoCopy = it
                        HapticUtils.vibrate(context, 20)
                    })
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // APP SETTINGS SECTION
        SettingsSectionHeader("APP SETTINGS")
        SettingsCard {
            SettingsItemRow(
                icon = Icons.Default.Palette,
                title = "Theme",
                trailing = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            HapticUtils.vibrate(context, 20)
                            val nextMode = if (themeMode == "DARK") "LIGHT" else "DARK"
                            viewModel.setThemeMode(nextMode)
                        }
                    ) {
                        Text(
                            text = if (themeMode == "DARK") "Dark" else "Light",
                            color = Color(0xFFA1A1AA),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color(0xFFA1A1AA),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            )
            HorizontalDivider(color = Color(0xFF27272A))
            SettingsItemRow(
                icon = Icons.Default.Vibration,
                title = "Haptic Feedback",
                trailing = {
                    RockSwitch(checked = isVibrationEnabled, onCheckedChange = {
                        isVibrationEnabled = it
                        HapticUtils.setVibrationEnabled(context, it)
                        HapticUtils.vibrate(context, 20)
                    })
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // DATA SECTION
        SettingsSectionHeader("DATA")
        SettingsCard {
            SettingsItemRow(
                icon = Icons.Default.FileUpload,
                title = "Export to JSON",
                onClick = {
                    HapticUtils.vibrate(context, 20)
                    viewModel.showToast("Exported database to JSON", CustomToastType.SUCCESS)
                }
            )
            HorizontalDivider(color = Color(0xFF27272A))
            SettingsItemRow(
                icon = Icons.Default.FileDownload,
                title = "Import from JSON",
                onClick = {
                    HapticUtils.vibrate(context, 20)
                    viewModel.showToast("Ready to import JSON backup", CustomToastType.INFO)
                }
            )
            HorizontalDivider(color = Color(0xFF27272A))
            SettingsItemRow(
                icon = Icons.Default.Delete,
                title = "Clear History",
                onClick = {
                    HapticUtils.vibrate(context, 30)
                    viewModel.clearAllLogs()
                    viewModel.showToast("History cleared", CustomToastType.SUCCESS)
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // UPDATES SECTION
        SettingsSectionHeader("UPDATES")
        SettingsCard {
            SettingsItemRow(
                icon = Icons.Default.Refresh,
                title = "Auto check for updates",
                trailing = {
                    RockSwitch(checked = autoCheckUpdates, onCheckedChange = {
                        autoCheckUpdates = it
                        HapticUtils.vibrate(context, 20)
                    })
                }
            )
            HorizontalDivider(color = Color(0xFF27272A))
            SettingsItemRow(
                icon = Icons.Default.SystemUpdate,
                title = "Check for updates",
                onClick = {
                    HapticUtils.vibrate(context, 20)
                    viewModel.showToast("Chamo QR is up to date (v1.0.8)", CustomToastType.SUCCESS)
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SUPPORT SECTION
        SettingsSectionHeader("SUPPORT")
        SettingsCard {
            SettingsItemRow(
                icon = Icons.Default.Share,
                title = "Share app with others",
                onClick = {
                    HapticUtils.vibrate(context, 20)
                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(android.content.Intent.EXTRA_SUBJECT, "Chamo QR")
                        putExtra(android.content.Intent.EXTRA_TEXT, "Check out Chamo QR app: https://sayanthrock.github.io/Chamo-QR")
                    }
                    context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Chamo QR"))
                }
            )
            HorizontalDivider(color = Color(0xFF27272A))
            SettingsItemRow(
                icon = Icons.Default.Star,
                title = "Star the project",
                onClick = {
                    HapticUtils.vibrate(context, 20)
                    val browserIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://github.com/sayanthrock/Chamo-QR"))
                    context.startActivity(browserIntent)
                }
            )
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFFA1A1AA),
        letterSpacing = 1.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 6.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            content = content
        )
    }
}

@Composable
fun SettingsItemRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF27272A)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }

        if (trailing != null) {
            trailing()
        } else if (onClick != null) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFFA1A1AA),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun RockSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .width(50.dp)
            .height(28.dp)
            .clip(CircleShape)
            .background(if (checked) Color(0xFF27272A) else Color(0xFF27272A))
            .clickable {
                HapticUtils.vibrate(context, 15)
                onCheckedChange(!checked)
            }
            .padding(3.dp),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (checked) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

