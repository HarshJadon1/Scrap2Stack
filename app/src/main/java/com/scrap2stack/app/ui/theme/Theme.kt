package com.scrap2stack.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

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
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
