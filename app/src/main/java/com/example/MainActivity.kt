package com.example

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.WallpaperRecord
import com.example.ui.components.*
import com.example.ui.theme.MyApplicationTheme
import com.example.utils.HapticUtils
import com.example.utils.ShareUtils
import com.example.viewmodel.CategoryItem
import com.example.viewmodel.CustomToastMessage
import com.example.viewmodel.CustomToastType
import com.example.viewmodel.WallpaperItem
import com.example.viewmodel.WallpaperViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.URL

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: WallpaperViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState()
            val dynamicColorEnabled by viewModel.dynamicColorEnabled.collectAsState()
            val colorPreset by viewModel.colorPreset.collectAsState()

            // Blur custom values from ViewModel
            val glassBlurVal by viewModel.glassBlurRadius.collectAsState()
            val glassOpacityVal by viewModel.glassOpacity.collectAsState()
            val glassBorderThicknessVal by viewModel.glassBorderThickness.collectAsState()
            val glassGlowEnabledVal by viewModel.glassGlowEnabled.collectAsState()

            val useDarkTheme = when (themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            MyApplicationTheme(
                darkTheme = useDarkTheme,
                dynamicColor = dynamicColorEnabled,
                colorPresetName = colorPreset
            ) {
                // Background Layer: Blur backdrop with organic glowing Rock patterns
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .rockFractureBackground(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                ) {
                    LiquidGlassBackground(modifier = Modifier.fillMaxSize())

                    MainAppContent(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MainAppContent(
    viewModel: WallpaperViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activeTab by viewModel.activeTab.collectAsState()
    val toastMessage by viewModel.toastEvent.collectAsState()
    val isApplying by viewModel.isApplyingWallpaper.collectAsState()

    var selectedWallpaperForDetail by remember { mutableStateOf<WallpaperItem?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        // Main view depending on active navigation tab
        Crossfade(
            targetState = activeTab,
            animationSpec = tween(350),
            modifier = Modifier.fillMaxSize(),
            label = "ScreenCrossfade"
        ) { tab ->
            when (tab) {
                "EXPLORE" -> ExploreScreen(
                    viewModel = viewModel,
                    onSelectWallpaper = { selectedWallpaperForDetail = it }
                )
                "CATEGORIES" -> CategoriesScreen(viewModel = viewModel)
                "FAVORITES" -> FavoritesScreen(
                    viewModel = viewModel,
                    onSelectWallpaper = { selectedWallpaperForDetail = it }
                )
                "SETTINGS" -> SettingsScreen(viewModel = viewModel)
            }
        }

        // Floating glass control cockpit panel (Settings / Presets live display)
        if (activeTab == "SETTINGS") {
            LiquidThemeControlPanel(
                viewModel = viewModel,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 96.dp)
            )
        }

        // Glare bottom navigation capsule
        DraggableFloatingActionBar(
            viewModel = viewModel,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Custom full-screen loading layer during active Wallpaper apply
        if (isApplying) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.padding(24.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f))
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "Configuring System Wallpaper...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Applying changes, please wait",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        // Full Screen Detailed Dialog Backstage
        if (selectedWallpaperForDetail != null) {
            WallpaperDetailDialog(
                wallpaper = selectedWallpaperForDetail!!,
                viewModel = viewModel,
                onDismiss = { selectedWallpaperForDetail = null }
            )
        }

        // Custom Top Toast Notifications
        CustomToastOverlay(
            toastMessage = toastMessage,
            onDismiss = { viewModel.clearToast() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    viewModel: WallpaperViewModel,
    onSelectWallpaper: (WallpaperItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val wallpapers by viewModel.wallpapers.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val activeCategoryKey by viewModel.selectedCategory.collectAsState()
    val searchQueryVal by viewModel.searchQuery.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(bottom = 80.dp)
    ) {
        // App header display
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "ROCK WALLS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.5.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Discover Geometry",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            IconButton(
                onClick = {
                    HapticUtils.vibrate(context, 40)
                    viewModel.showToast(
                        "Rock Repository Online - v1.0.4 - Structured Assets Enabled",
                        CustomToastType.INFO
                    )
                },
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.CloudSync,
                    contentDescription = "Sync information",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Clean Search Field card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                TextField(
                    value = searchQueryVal,
                    onValueChange = { viewModel.searchQuery.value = it },
                    placeholder = {
                        Text(
                            text = "Search tag, title (e.g. obsidian)...",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.weight(1f)
                )
                if (searchQueryVal.isNotEmpty()) {
                    IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        // Horizontal Category Filter chips deck
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // "ALL" Chips options
            FilterChip(
                selected = activeCategoryKey == null,
                onClick = {
                    HapticUtils.vibrate(context, 20)
                    viewModel.selectCategory(null)
                },
                label = { Text("All Rocks") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    selectedLabelColor = MaterialTheme.colorScheme.primary
                )
            )

            // Category list loops
            categories.forEach { cat ->
                val isSel = activeCategoryKey?.uppercase() == cat.key.uppercase()
                FilterChip(
                    selected = isSel,
                    onClick = {
                        HapticUtils.vibrate(context, 20)
                        viewModel.selectCategory(cat.key)
                    },
                    label = { Text(cat.name) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        selectedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        // Main wallpapers vertical Grid view
        if (wallpapers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterAltOff,
                        contentDescription = "Search empty",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        modifier = Modifier.size(54.dp)
                    )
                    Text(
                        text = "No mineral matching found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Try searching for basalt, geode, zen...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("wallpaper_lazy_grid"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(wallpapers, key = { it.id }) { wall ->
                    WallpaperCard(
                        item = wall,
                        onClick = { onSelectWallpaper(wall) },
                        onToggleFavorite = { viewModel.toggleFavorite(wall) }
                    )
                }
            }
        }
    }
}

@Composable
fun WallpaperCard(
    item: WallpaperItem,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable {
                HapticUtils.vibrate(context, 40)
                onClick()
            }
            .border(
                width = 0.8.dp,
                color = if (item.isFavorite) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) 
                        else Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.45f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Async premium image loading with Coil
            AsyncImage(
                model = item.thumb,
                contentDescription = item.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Premium tag indicator overlay
            if (item.isPremium) {
                Box(
                    modifier = Modifier
                        .padding(10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .border(1.dp, Color(0xFFFFB703).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Premium Icon",
                            tint = Color(0xFFFFB703),
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = "PREMIUM",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFFB703)
                        )
                    }
                }
            }

            // Bottom descriptive glass badge overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                        )
                    )
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = item.category.uppercase(),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Floating micro-star favorite action button
                    val iconVector = if (item.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder
                    val iconColor = if (item.isFavorite) Color(0xFFFF4D4D) else Color.White.copy(alpha = 0.7f)
                    IconButton(
                        onClick = {
                            HapticUtils.vibrate(context, 55)
                            onToggleFavorite()
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.Black.copy(alpha = 0.35f), CircleShape)
                    ) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = "Bookmark",
                            tint = iconColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoriesScreen(
    viewModel: WallpaperViewModel,
    modifier: Modifier = Modifier
) {
    val categories by viewModel.categories.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(bottom = 80.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
            Text(
                text = "CLASSIFICATIONS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.5.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Folders Catalog",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Folder listings
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f).testTag("categories_lazy_column"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(categories, key = { it.key }) { cat ->
                CategoryFolderCard(
                    category = cat,
                    onClick = {
                        HapticUtils.vibrate(context, 40)
                        viewModel.selectCategory(cat.key)
                    }
                )
            }
        }
    }
}

@Composable
fun CategoryFolderCard(
    category: CategoryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Full bleed visual backdrop
            AsyncImage(
                model = category.coverUrl,
                contentDescription = category.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Dynamic blur frosted cover overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .padding(20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = category.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Counter badge capsule
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
                        modifier = Modifier.border(0.8.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${category.count}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "WALLS",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FavoritesScreen(
    viewModel: WallpaperViewModel,
    onSelectWallpaper: (WallpaperItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val favorites by viewModel.favoriteWallpapers.collectAsState()

    val favItemsMapped = favorites.map { record ->
        WallpaperItem(
            id = record.id,
            title = record.title,
            category = record.category,
            url = record.url,
            thumb = record.thumbUrl,
            tags = record.tagsCsv.split(","),
            isPremium = record.isPremium,
            isFavorite = true
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(bottom = 80.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
            Text(
                text = "SAVED SELECTIONS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.5.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "My Bookmarks",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Illustrative empty state stack stones
        if (favItemsMapped.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(horizontal = 32.dp)
                ) {
                    // Procedural illustration draw
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .drawBehind {
                                // Draw three stacked balanced Zen Stones
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.08f),
                                    radius = 48.dp.toPx(),
                                    center = center
                                )
                                // Bottom large stone
                                drawOval(
                                    color = Color.Gray.copy(alpha = 0.4f),
                                    topLeft = Offset(center.x - 35.dp.toPx(), center.y + 12.dp.toPx()),
                                    size = androidx.compose.ui.geometry.Size(70.dp.toPx(), 25.dp.toPx())
                                )
                                // Middle stone
                                drawOval(
                                    color = Color.Gray.copy(alpha = 0.6f),
                                    topLeft = Offset(center.x - 24.dp.toPx(), center.y - 12.dp.toPx()),
                                    size = androidx.compose.ui.geometry.Size(48.dp.toPx(), 22.dp.toPx())
                                )
                                // Top small stone
                                drawOval(
                                    color = Color(0xFF9D4EED).copy(alpha = 0.7f),
                                    topLeft = Offset(center.x - 14.dp.toPx(), center.y - 30.dp.toPx()),
                                    size = androidx.compose.ui.geometry.Size(28.dp.toPx(), 18.dp.toPx())
                                )
                            }
                    )
                    Text(
                        text = "Zen Cairn is empty",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Star beautiful rock materials and crystal geodes to access them offline anytime.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("favorites_lazy_grid"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(favItemsMapped, key = { it.id }) { wall ->
                    WallpaperCard(
                        item = wall,
                        onClick = { onSelectWallpaper(wall) },
                        onToggleFavorite = { viewModel.toggleFavorite(wall) }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    viewModel: WallpaperViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val themeMode by viewModel.themeMode.collectAsState()
    val dynamicColor by viewModel.dynamicColorEnabled.collectAsState()
    val colorPresetName by viewModel.colorPreset.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 220.dp) // Leave roomy spacing for the HUD Slider Cockpit!
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
            Text(
                text = "PREFERENCES",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.5.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Settings Panel",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Section: Visual Themes Group
        Text(
            text = "SYSTEM VIEWPORT",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .border(0.6.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.25f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Theme Options Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Color Canvas Mode",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Set light, dark default background",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("DARK", "LIGHT").forEach { mode ->
                            val isSel = themeMode.uppercase() == mode
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSel) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent)
                                    .border(
                                        width = 0.8.dp,
                                        color = if (isSel) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else Color.Transparent,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        HapticUtils.vibrate(context, 20)
                                        viewModel.setThemeMode(mode)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = mode,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.White else Color.White.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                // Dynamic coloring check (Android 12+ wallpaper dynamic palette extraction)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Material 3 Dynamic Theme",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Acquire ambient palette from OS wallpapers",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    Switch(
                        checked = dynamicColor,
                        onCheckedChange = {
                            HapticUtils.vibrate(context, 35)
                            viewModel.setDynamicColorEnabled(it)
                        }
                    )
                }
            }
        }

        // Section: System Info & License Group
        Text(
            text = "REPO TECHNICAL INFORMATION",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .border(0.6.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.25f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Asset Packaging", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 13.sp)
                    Text(text = "Structured JSON Catalog", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Metadata Version", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 13.sp)
                    Text(text = "v1.0.4 (Versioned Index)", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Local Storage", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 13.sp)
                    Text(text = "Room SQLite Database Enabled", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Asset Resolution", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 13.sp)
                    Text(text = "1080p Full-DPI WebP & JPEG", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun WallpaperDetailDialog(
    wallpaper: WallpaperItem,
    viewModel: WallpaperViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
        ) {
            // Full screen wallpaper presentation backdrop
            AsyncImage(
                model = wallpaper.url,
                contentDescription = wallpaper.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Dynamic translucent black mask on bottom half
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.15f),
                                Color.Black.copy(alpha = 0.4f),
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            )

            // Top action bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Return chevron left back icon
                IconButton(
                    onClick = {
                        HapticUtils.vibrate(context, 35)
                        onDismiss()
                    },
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Return",
                        tint = Color.White
                    )
                }

                // Star toggle favoritism on top right
                IconButton(
                    onClick = {
                        HapticUtils.vibrate(context, 55)
                        viewModel.toggleFavorite(wallpaper)
                    },
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    val isFav = viewModel.wallpapers.collectAsState().value.firstOrNull { it.id == wallpaper.id }?.isFavorite ?: false
                    Icon(
                        imageVector = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Star",
                        tint = if (isFav) Color(0xFFFF4D4D) else Color.White
                    )
                }
            }

            // Bottom glass informative and action hub capsule
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, bottom = 24.dp)
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(26.dp)
                    ),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.65f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header text
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = wallpaper.category.uppercase(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (wallpaper.isPremium) {
                                Text(
                                    text = "★ ULTRA PRESETS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFFFB703),
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = wallpaper.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    // Hashtag row descriptors
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        wallpaper.tags.forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .border(0.6.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                                    .clickable {
                                        HapticUtils.vibrate(context, 30)
                                        viewModel.searchQuery.value = tag
                                        onDismiss() // Go back to Explore filtered list
                                    }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(text = "#$tag", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                            }
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                    // Segmented Action Set
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Apply button: slide bottom sheet with individual configurations list
                        var showApplySheet by remember { mutableStateOf(false) }

                        Button(
                            onClick = {
                                HapticUtils.vibrate(context, 40)
                                showApplySheet = true
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Wallpaper, contentDescription = "Apply", modifier = Modifier.size(16.dp))
                                Text("APPLY TO DEVICE", fontWeight = FontWeight.Black, fontSize = 12.sp)
                            }
                        }

                        // Save directly to device via custom helper
                        var isDownloadingLocal by remember { mutableStateOf(false) }

                        IconButton(
                            onClick = {
                                if (isDownloadingLocal) return@IconButton
                                HapticUtils.vibrate(context, 45)
                                isDownloadingLocal = true
                                viewModel.showToast("Retrieving wallpaper file stream...", CustomToastType.INFO, 2000L)

                                coroutineScope.launch(Dispatchers.IO) {
                                    val bitmap = downloadBitmapStream(wallpaper.url)
                                    withContext(Dispatchers.Main) {
                                        if (bitmap != null) {
                                            ShareUtils.saveBitmapToGallery(
                                                context = context,
                                                bitmap = bitmap,
                                                displayName = "RockWallpaper_${wallpaper.id}_",
                                                isPng = false,
                                                onShowToast = { msg, toastType ->
                                                    viewModel.showToast(msg, toastType)
                                                }
                                            )
                                        } else {
                                            viewModel.showToast("Failed to compile image stream.", CustomToastType.ERROR)
                                        }
                                        isDownloadingLocal = false
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                                .border(0.8.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        ) {
                            if (isDownloadingLocal) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(imageVector = Icons.Default.FileDownload, contentDescription = "Download gallery", tint = Color.White)
                            }
                        }

                        // Apply modal options chooser sheet dialogue
                        if (showApplySheet) {
                            ApplyConfigurationDialog(
                                onDismiss = { showApplySheet = false },
                                onConfigureSet = { target ->
                                    showApplySheet = false
                                    viewModel.applyWallpaper(wallpaper.url, target)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ApplyConfigurationDialog(
    onDismiss: () -> Unit,
    onConfigureSet: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Apply Wallpaper",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // HOMESCREEN Segment
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onConfigureSet("HOMESCREEN") }
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Home, contentDescription = "Home", tint = MaterialTheme.colorScheme.primary)
                        Column {
                            Text(text = "Home Screen Menu Only", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "Apply only to standard system homescreen", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    }
                }

                // LOCKSCREEN Segment
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onConfigureSet("LOCKSCREEN") }
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.LockClock, contentDescription = "Lock", tint = MaterialTheme.colorScheme.primary)
                        Column {
                            Text(text = "Lock Screen Guard Only", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "Apply only to lock screen backdrop protection", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    }
                }

                // BOTH Segment
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onConfigureSet("BOTH") }
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AllInclusive, contentDescription = "Both", tint = MaterialTheme.colorScheme.primary)
                        Column {
                            Text(text = "Apply on Both Screen Canvas", fontWeight = FontWeight.Black, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                            Text(text = "Sync both backdrops with single action", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

// Global scope direct stream thread download function
private fun downloadBitmapStream(urlString: String): Bitmap? {
    return try {
        val url = URL(urlString)
        val connection = url.openConnection()
        connection.connectTimeout = 8000
        connection.readTimeout = 8000
        connection.doInput = true
        connection.getInputStream().use { inputStream: InputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
