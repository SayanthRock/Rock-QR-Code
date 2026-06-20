package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Encapsulated dynamic 'Liquid Glass' theme engine.
 * Consolidates the configuration schema, Local state container context,
 * and standard composition provider into a static object to protect cross-package
 * class compilation paths.
 */
object LiquidGlassTheme {

    data class Config(
        val primaryColor: Color = Color(0xFF9D4EDD),
        val secondaryColor: Color = Color(0xFF5A189A),
        val glassBlur: Dp = 16.dp,
        val glassOpacity: Float = 0.28f,
        val borderAlphaStart: Float = 0.45f,
        val borderAlphaEnd: Float = 0.15f,
        val isGlowEnabled: Boolean = true,
        val cornerRadius: Dp = 16.dp,
        val borderThickness: Dp = 1.5.dp,
        val isDark: Boolean = true,
        val isLiquidGlassEnabled: Boolean = true
    ) {
        val textColor: Color get() = if (isDark) Color.White else Color(0xFF1A1C1E)
        val subTextColor: Color get() = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF1A1C1E).copy(alpha = 0.6f)
        val containerBgColor: Color get() = if (isDark) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.04f)
    }

    val LocalConfig: ProvidableCompositionLocal<Config> = 
        staticCompositionLocalOf { Config() }

    @Composable
    fun Provider(
        config: Config,
        content: @Composable () -> Unit
    ) {
        CompositionLocalProvider(
            LocalConfig provides config,
            content = content
        )
    }
}
