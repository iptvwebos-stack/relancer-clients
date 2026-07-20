package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

enum class AppTheme(val displayName: String, val isDark: Boolean) {
  DEFAULT("Défaut (M3 Violet)", false),
  COSMIC_DARK("Sombre Cosmique 🌌", true),
  MODERN_LIGHT("Clair Moderne ☀️", false),
  GOLDEN_TWILIGHT("Crépuscule Doré 🌅", true),
  ZEN_FOREST("Forêt Zen 🌲", false),
  OCEAN_BLUE("Bleu Océan 🌊", false)
}

private val DarkColorScheme =
  darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
  )

// Premium custom themes
private val CosmicDarkColorScheme = darkColorScheme(
  primary = Color(0xFFB09FFF),
  onPrimary = Color(0xFF1E0066),
  primaryContainer = Color(0xFF3C1E99),
  onPrimaryContainer = Color(0xFFEADBFF),
  secondary = Color(0xFF82B1FF),
  onSecondary = Color(0xFF002F6C),
  secondaryContainer = Color(0xFF004494),
  onSecondaryContainer = Color(0xFFD7E2FF),
  tertiary = Color(0xFF80DEEA),
  onTertiary = Color(0xFF00363D),
  tertiaryContainer = Color(0xFF004F58),
  onTertiaryContainer = Color(0xFFB2EBFF),
  background = Color(0xFF0F0D1E),
  onBackground = Color(0xFFF1EFFF),
  surface = Color(0xFF1B1836),
  onSurface = Color(0xFFF1EFFF),
  surfaceVariant = Color(0xFF2E2A52),
  onSurfaceVariant = Color(0xFFC9C4E0),
  outline = Color(0xFF938F99),
  error = Color(0xFFFFB4AB),
  onError = Color(0xFF690005)
)

private val ModernLightColorScheme = lightColorScheme(
  primary = Color(0xFF005FAF),
  onPrimary = Color(0xFFFFFFFF),
  primaryContainer = Color(0xFFD4E3FF),
  onPrimaryContainer = Color(0xFF001C3A),
  secondary = Color(0xFF006874),
  onSecondary = Color(0xFFFFFFFF),
  secondaryContainer = Color(0xFF97F0FF),
  onSecondaryContainer = Color(0xFF001F24),
  tertiary = Color(0xFF7D5260),
  onTertiary = Color(0xFFFFFFFF),
  tertiaryContainer = Color(0xFFFFD8E4),
  onTertiaryContainer = Color(0xFF31111D),
  background = Color(0xFFF8F9FA),
  onBackground = Color(0xFF1A1C1E),
  surface = Color(0xFFFFFFFF),
  onSurface = Color(0xFF1A1C1E),
  surfaceVariant = Color(0xFFDFE2EB),
  onSurfaceVariant = Color(0xFF43474E),
  outline = Color(0xFF73777F),
  error = Color(0xFFBA1A1A),
  onError = Color(0xFFFFFFFF)
)

private val GoldenTwilightColorScheme = darkColorScheme(
  primary = Color(0xFFFFB74D),
  onPrimary = Color(0xFF4E2600),
  primaryContainer = Color(0xFF703800),
  onPrimaryContainer = Color(0xFFFFDDB8),
  secondary = Color(0xFFFFD54F),
  onSecondary = Color(0xFF3D2E00),
  secondaryContainer = Color(0xFF574300),
  onSecondaryContainer = Color(0xFFFFE082),
  tertiary = Color(0xFFE57373),
  onTertiary = Color(0xFF56000E),
  tertiaryContainer = Color(0xFF7E171C),
  onTertiaryContainer = Color(0xFFFFDAD9),
  background = Color(0xFF1A1412),
  onBackground = Color(0xFFFFF3E0),
  surface = Color(0xFF281E19),
  onSurface = Color(0xFFFFF3E0),
  surfaceVariant = Color(0xFF3F3027),
  onSurfaceVariant = Color(0xFFD6C2B4),
  outline = Color(0xFF9F8D83),
  error = Color(0xFFFFB4AB),
  onError = Color(0xFF690005)
)

private val ZenForestColorScheme = lightColorScheme(
  primary = Color(0xFF2E7D32),
  onPrimary = Color(0xFFFFFFFF),
  primaryContainer = Color(0xFFA5D6A7),
  onPrimaryContainer = Color(0xFF003300),
  secondary = Color(0xFF689F38),
  onSecondary = Color(0xFFFFFFFF),
  secondaryContainer = Color(0xFFDCEDC8),
  onSecondaryContainer = Color(0xFF213300),
  tertiary = Color(0xFF00796B),
  onTertiary = Color(0xFFFFFFFF),
  tertiaryContainer = Color(0xFFB2DFDB),
  onTertiaryContainer = Color(0xFF002B25),
  background = Color(0xFFF1F8E9),
  onBackground = Color(0xFF1B1F1B),
  surface = Color(0xFFFFFFFF),
  onSurface = Color(0xFF1B1F1B),
  surfaceVariant = Color(0xFFE1E5DC),
  onSurfaceVariant = Color(0xFF444841),
  outline = Color(0xFF747970),
  error = Color(0xFFBA1A1A),
  onError = Color(0xFFFFFFFF)
)

private val OceanBlueColorScheme = lightColorScheme(
  primary = Color(0xFF0288D1),
  onPrimary = Color(0xFFFFFFFF),
  primaryContainer = Color(0xFFB3E5FC),
  onPrimaryContainer = Color(0xFF001F2D),
  secondary = Color(0xFF0097A7),
  onSecondary = Color(0xFFFFFFFF),
  secondaryContainer = Color(0xFFB2EBF2),
  onSecondaryContainer = Color(0xFF002024),
  tertiary = Color(0xFF00BCD4),
  onTertiary = Color(0xFFFFFFFF),
  tertiaryContainer = Color(0xFFE0F7FA),
  onTertiaryContainer = Color(0xFF001A1D),
  background = Color(0xFFE1F5FE),
  onBackground = Color(0xFF0D1C24),
  surface = Color(0xFFFFFFFF),
  onSurface = Color(0xFF0D1C24),
  surfaceVariant = Color(0xFFD9E3EC),
  onSurfaceVariant = Color(0xFF3F4850),
  outline = Color(0xFF6F7881),
  error = Color(0xFFBA1A1A),
  onError = Color(0xFFFFFFFF)
)

@Composable
fun MyApplicationTheme(
  appTheme: AppTheme = AppTheme.DEFAULT,
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when (appTheme) {
      AppTheme.DEFAULT -> {
        if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
          val context = LocalContext.current
          if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        } else {
          if (darkTheme) DarkColorScheme else LightColorScheme
        }
      }
      AppTheme.COSMIC_DARK -> CosmicDarkColorScheme
      AppTheme.MODERN_LIGHT -> ModernLightColorScheme
      AppTheme.GOLDEN_TWILIGHT -> GoldenTwilightColorScheme
      AppTheme.ZEN_FOREST -> ZenForestColorScheme
      AppTheme.OCEAN_BLUE -> OceanBlueColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
