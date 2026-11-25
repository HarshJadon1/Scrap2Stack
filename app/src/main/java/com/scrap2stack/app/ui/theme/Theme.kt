package com.scrap2stack.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Immutable
data class BrutalPalette(
    val canvas: Color,
    val surface: Color,
    val surfaceRaised: Color,
    val border: Color,
    val shadow: Color,
    val textPrimary: Color,
    val textMuted: Color,
    val accentOnSurface: Color,
    val dotGrid: Color
)

private val DarkBrutalPalette = BrutalPalette(
    canvas = BrutalCanvasDark,
    surface = BrutalSurfaceDark,
    surfaceRaised = BrutalSurfaceRaised,
    border = BrutalBorderWhite,
    shadow = BrutalShadowBlack,
    textPrimary = Color.White,
    textMuted = Color(0xFF94A3B8),
    accentOnSurface = ElectricMint,
    dotGrid = Color.White.copy(alpha = 0.055f)
)

private val LightBrutalPalette = BrutalPalette(
    canvas = BrutalCanvasLight,
    surface = BrutalSurfaceLight,
    surfaceRaised = Color(0xFFE7E4DB),
    border = BrutalInkBlack,
    shadow = BrutalShadowBlack,
    textPrimary = BrutalInkBlack,
    textMuted = Color(0xFF475569),
    accentOnSurface = RevivalEmeraldDark,
    dotGrid = Color.Black.copy(alpha = 0.085f)
)

val LocalBrutalPalette = staticCompositionLocalOf { DarkBrutalPalette }

val MaterialTheme.brutalColors: BrutalPalette
    @Composable
    get() = LocalBrutalPalette.current

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryIndigo,
    onPrimary = Color.White,
    primaryContainer = PrimaryIndigoDark,
    onPrimaryContainer = Color.White,
    secondary = RevivalEmerald,
    onSecondary = Color.White,
    secondaryContainer = RevivalEmeraldDark,
    onSecondaryContainer = Color.White,
    background = BrutalCanvasDark,
    onBackground = SlateOnSurfaceDark,
    surface = BrutalSurfaceDark,
    onSurface = SlateOnSurfaceDark,
    surfaceVariant = BrutalSurfaceRaised,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = BrutalBorderWhite,
    outlineVariant = Color(0xFF334155),
    error = StatusAbandoned,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryIndigoDark,
    onPrimary = Color.White,
    primaryContainer = PrimaryIndigoLight.copy(alpha = 0.2f),
    onPrimaryContainer = PrimaryIndigoDark,
    secondary = RevivalEmeraldDark,
    onSecondary = Color.White,
    secondaryContainer = RevivalEmeraldLight.copy(alpha = 0.2f),
    onSecondaryContainer = RevivalEmeraldDark,
    background = BrutalCanvasLight,
    onBackground = BrutalInkBlack,
    surface = BrutalSurfaceLight,
    onSurface = BrutalInkBlack,
    surfaceVariant = Color(0xFFE7E4DB),
    onSurfaceVariant = Color(0xFF475569),
    outline = BrutalInkBlack,
    outlineVariant = Color(0xFF94A3B8),
    error = StatusAbandoned,
    onError = Color.White
)

@Composable
fun Scrap2StackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val brutalPalette = if (darkTheme) DarkBrutalPalette else LightBrutalPalette
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.background.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalBrutalPalette provides brutalPalette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
