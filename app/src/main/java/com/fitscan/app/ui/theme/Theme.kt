package com.fitscan.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = WarmGold,
    secondary = GoldAccent,
    background = CharcoalDark,
    surface = SurfaceDark,
    surfaceContainer = SurfaceContainerDark,
    onPrimary = TextOnLight,
    onSecondary = TextOnLight,
    onBackground = OnSurfaceDark,
    onSurface = OnSurfaceDark,
    error = ErrorColor
)

private val LightColorScheme = DarkColorScheme // Defaulting everything to Dark as per requirements

@Composable
fun FitScanTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    // We disable dynamic coloring to enforce the exact design specs from Stitch UI
    val colorScheme = DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
