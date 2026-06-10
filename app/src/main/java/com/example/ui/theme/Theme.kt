package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = OceanTeal,
    secondary = StoneGrey,
    tertiary = EarthAmber,
    background = ObsidianBlack,
    surface = DarkGranite,
    surfaceVariant = ShaleGrey,
    onPrimary = ObsidianBlack,
    onSecondary = LightStoneSurface,
    onBackground = LightStoneBg,
    onSurface = LightStoneBg
  )

private val LightColorScheme =
  lightColorScheme(
    primary = OceanTeal,
    secondary = HeavySlate,
    tertiary = EarthAmber,
    background = LightStoneBg,
    surface = LightStoneSurface,
    surfaceVariant = LightStoneBg,
    onPrimary = LightStoneSurface,
    onSecondary = ObsidianBlack,
    onBackground = ObsidianBlack,
    onSurface = ObsidianBlack
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
