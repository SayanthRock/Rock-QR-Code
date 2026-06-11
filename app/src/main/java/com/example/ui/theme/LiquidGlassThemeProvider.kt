package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Configuration payload representing the dynamic 'Liquid Glass' theme engine.
 * Maps conceptually to React Context state & Tailwind CSS dynamic variables,
 * providing real-time responsive design values across all child composables.
 */
data class LiquidGlassThemeConfig(
    val primaryColor: Color = Color(0xFF9D4EDD),
    val secondaryColor: Color = Color(0xFF5A189A),
    val glassBlur: Dp = 16.dp,
    val glassOpacity: Float = 0.28f,
    val borderAlphaStart: Float = 0.45f,
    val borderAlphaEnd: Float = 0.15f,
    val isGlowEnabled: Boolean = true,
    val cornerRadius: Dp = 16.dp
)

/**
 * The CompositionLocal variable representing the React-like context container.
 * This exposes the values dynamically across the full Compose hierarchy.
 */
val LocalLiquidGlassThemeConfig: ProvidableCompositionLocal<LiquidGlassThemeConfig> = 
    staticCompositionLocalOf { LiquidGlassThemeConfig() }

/**
 * LiquidGlassThemeProvider acts as the React-equivalent context provider.
 * Allows deep child component nodes to dynamically query, react, and redraw
 * themselves when the theme preset is toggled.
 */
@Composable
fun LiquidGlassThemeProvider(
    config: LiquidGlassThemeConfig,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalLiquidGlassThemeConfig provides config,
        content = content
    )
}
