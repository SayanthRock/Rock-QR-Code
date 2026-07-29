package com.example.ui.theme

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

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
  colorPresetName: String = "MIDNIGHT",
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> {
        when (colorPresetName.uppercase()) {
          "MIDNIGHT" -> darkColorScheme(
            primary = MidnightPrimary,
            secondary = MidnightSecondary,
            tertiary = MidnightTertiary,
            background = MidnightBackground,
            surface = Color(0xFF121021),
            surfaceVariant = Color(0xFF1A172E),
            onPrimary = Color.White,
            onSecondary = Color(0xFF080711),
            onBackground = Color(0xFFECE6F5),
            onSurface = Color(0xFFECE6F5),
            outline = MidnightSecondary.copy(alpha = 0.35f)
          )
          "ARCTIC" -> darkColorScheme(
            primary = ArcticPrimary,
            secondary = ArcticSecondary,
            tertiary = ArcticTertiary,
            background = ArcticBackground,
            surface = Color(0xFF151B21),
            surfaceVariant = Color(0xFF1C242C),
            onPrimary = Color(0xFF1E262E),
            onSecondary = Color.White,
            onBackground = Color(0xFFEAF3F7),
            onSurface = Color(0xFFEAF3F7),
            outline = ArcticSecondary.copy(alpha = 0.35f)
          )
          "OCEAN" -> darkColorScheme(
            primary = OceanPrimary,
            secondary = OceanSecondary,
            tertiary = OceanTertiary,
            background = OceanBackground,
            surface = Color(0xFF0B1724),
            surfaceVariant = Color(0xFF122438),
            onPrimary = Color.White,
            onSecondary = Color(0xFF050B14),
            onBackground = Color(0xFFE1F4F8),
            onSurface = Color(0xFFE1F4F8),
            outline = OceanSecondary.copy(alpha = 0.35f)
          )
          "AURORA" -> darkColorScheme(
            primary = AuroraPrimary,
            secondary = AuroraSecondary,
            tertiary = AuroraTertiary,
            background = AuroraBackground,
            surface = Color(0xFF0A1C18),
            surfaceVariant = Color(0xFF122E28),
            onPrimary = Color(0xFF050E0C),
            onSecondary = Color.White,
            onBackground = Color(0xFFE5FAF4),
            onSurface = Color(0xFFE5FAF4),
            outline = AuroraSecondary.copy(alpha = 0.35f)
          )
          "EMERALD" -> darkColorScheme(
            primary = EmeraldPrimary,
            secondary = EmeraldSecondary,
            tertiary = EmeraldTertiary,
            background = EmeraldBackground,
            surface = Color(0xFF0F1715),
            surfaceVariant = Color(0xFF172421),
            onPrimary = Color.White,
            onSecondary = Color(0xFF080C0B),
            onBackground = Color(0xFFECEEF2),
            onSurface = Color(0xFFECEEF2),
            outline = EmeraldSecondary.copy(alpha = 0.35f)
          )
          else -> DarkColorScheme
        }
      }

      else -> {
        when (colorPresetName.uppercase()) {
          "MIDNIGHT" -> lightColorScheme(
            primary = MidnightPrimary,
            secondary = MidnightSecondary,
            tertiary = MidnightTertiary,
            background = Color(0xFFF7F5FA),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFEFEBF6),
            onPrimary = Color.White,
            onSecondary = Color.Black,
            onBackground = Color(0xFF1A172E),
            onSurface = Color(0xFF1A172E),
            outline = MidnightPrimary.copy(alpha = 0.3f)
          )
          "ARCTIC" -> lightColorScheme(
            primary = ArcticPrimary,
            secondary = ArcticSecondary,
            tertiary = ArcticTertiary,
            background = Color(0xFFF0F5F8),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFE3EFF5),
            onPrimary = Color(0xFF0F151B),
            onSecondary = Color(0xFF0F151B),
            onBackground = Color(0xFF0F151B),
            onSurface = Color(0xFF0F151B),
            outline = ArcticSecondary.copy(alpha = 0.3f)
          )
          "OCEAN" -> lightColorScheme(
            primary = OceanPrimary,
            secondary = OceanSecondary,
            tertiary = OceanTertiary,
            background = Color(0xFFEFF7FA),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFE0F1F7),
            onPrimary = Color.White,
            onSecondary = Color(0xFF050B14),
            onBackground = Color(0xFF050B14),
            onSurface = Color(0xFF050B14),
            outline = OceanPrimary.copy(alpha = 0.3f)
          )
          "AURORA" -> lightColorScheme(
            primary = AuroraPrimary,
            secondary = AuroraSecondary,
            tertiary = AuroraTertiary,
            background = Color(0xFFEFFBF8),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFE0FAF2),
            onPrimary = Color(0xFF050E0C),
            onSecondary = Color(0xFF050E0C),
            onBackground = Color(0xFF050E0C),
            onSurface = Color(0xFF050E0C),
            outline = AuroraPrimary.copy(alpha = 0.3f)
          )
          "EMERALD" -> lightColorScheme(
            primary = EmeraldPrimary,
            secondary = EmeraldSecondary,
            tertiary = EmeraldTertiary,
            background = Color(0xFFEFFBF8),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFE0FAF4),
            onPrimary = Color.White,
            onSecondary = Color(0xFF080C0B),
            onBackground = Color(0xFF080C0B),
            onSurface = Color(0xFF080C0B),
            outline = EmeraldPrimary.copy(alpha = 0.3f)
          )
          else -> LightColorScheme
        }
      }
    }

  val animatedColorScheme = animateColorScheme(colorScheme)

  val themeConfig = LiquidGlassTheme.Config(
    primaryColor = animatedColorScheme.primary,
    secondaryColor = animatedColorScheme.secondary,
    glassBlur = 18.dp,
    glassOpacity = if (darkTheme) 0.28f else 0.45f,
    borderAlphaStart = 0.55f,
    borderAlphaEnd = 0.18f,
    isDark = darkTheme
  )

  LiquidGlassTheme.Provider(config = themeConfig) {
    MaterialTheme(colorScheme = animatedColorScheme, typography = Typography, content = content)
  }
}

@Composable
fun animateColorScheme(targetColorScheme: ColorScheme): ColorScheme {
  val primary by animateColorAsState(targetValue = targetColorScheme.primary, animationSpec = tween(500, easing = LinearOutSlowInEasing), label = "primary")
  val secondary by animateColorAsState(targetValue = targetColorScheme.secondary, animationSpec = tween(500, easing = LinearOutSlowInEasing), label = "secondary")
  val tertiary by animateColorAsState(targetValue = targetColorScheme.tertiary, animationSpec = tween(500, easing = LinearOutSlowInEasing), label = "tertiary")
  val background by animateColorAsState(targetValue = targetColorScheme.background, animationSpec = tween(500, easing = LinearOutSlowInEasing), label = "background")
  val surface by animateColorAsState(targetValue = targetColorScheme.surface, animationSpec = tween(500, easing = LinearOutSlowInEasing), label = "surface")
  val surfaceVariant by animateColorAsState(targetValue = targetColorScheme.surfaceVariant, animationSpec = tween(500, easing = LinearOutSlowInEasing), label = "surfaceVariant")
  val onPrimary by animateColorAsState(targetValue = targetColorScheme.onPrimary, animationSpec = tween(500, easing = LinearOutSlowInEasing), label = "onPrimary")
  val onSecondary by animateColorAsState(targetValue = targetColorScheme.onSecondary, animationSpec = tween(500, easing = LinearOutSlowInEasing), label = "onSecondary")
  val onBackground by animateColorAsState(targetValue = targetColorScheme.onBackground, animationSpec = tween(500, easing = LinearOutSlowInEasing), label = "onBackground")
  val onSurface by animateColorAsState(targetValue = targetColorScheme.onSurface, animationSpec = tween(500, easing = LinearOutSlowInEasing), label = "onSurface")
  val outline by animateColorAsState(targetValue = targetColorScheme.outline, animationSpec = tween(500, easing = LinearOutSlowInEasing), label = "outline")
  val error by animateColorAsState(targetValue = targetColorScheme.error, animationSpec = tween(500, easing = LinearOutSlowInEasing), label = "error")
  val onError by animateColorAsState(targetValue = targetColorScheme.onError, animationSpec = tween(500, easing = LinearOutSlowInEasing), label = "onError")
  val errorContainer by animateColorAsState(targetValue = targetColorScheme.errorContainer, animationSpec = tween(500, easing = LinearOutSlowInEasing), label = "errorContainer")
  val onErrorContainer by animateColorAsState(targetValue = targetColorScheme.onErrorContainer, animationSpec = tween(500, easing = LinearOutSlowInEasing), label = "onErrorContainer")
  
  return targetColorScheme.copy(
    primary = primary,
    secondary = secondary,
    tertiary = tertiary,
    background = background,
    surface = surface,
    surfaceVariant = surfaceVariant,
    onPrimary = onPrimary,
    onSecondary = onSecondary,
    onBackground = onBackground,
    onSurface = onSurface,
    outline = outline,
    error = error,
    onError = onError,
    errorContainer = errorContainer,
    onErrorContainer = onErrorContainer
  )
}
