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
    primary = MalachitePrimary,
    secondary = ChippedBasalt,
    tertiary = PyriteGold,
    background = ObsidianBlack,
    surface = DeepGranite,
    surfaceVariant = ChippedBasalt,
    onPrimary = ObsidianBlack,
    onSecondary = PolishedCalcite,
    onBackground = QuartzAlabaster,
    onSurface = QuartzAlabaster,
    outline = MineralOreSlate
  )

private val LightColorScheme =
  lightColorScheme(
    primary = MalachitePrimary,
    secondary = SlateSurface,
    tertiary = IronTerracotta,
    background = QuartzAlabaster,
    surface = PolishedCalcite,
    surfaceVariant = SlateSurface,
    onPrimary = PolishedCalcite,
    onSecondary = FossilCharcoal,
    onBackground = FossilCharcoal,
    onSurface = FossilCharcoal,
    outline = PolishedCalcite
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Set dynamic color to false by default to preserve custom rock-branding identity
  dynamicColor: Boolean = false,
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
