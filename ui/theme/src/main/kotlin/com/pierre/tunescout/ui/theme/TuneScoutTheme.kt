package com.pierre.tunescout.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val darkScheme: ColorScheme = darkColorScheme(
    primary = TuneScoutColors.textPrimary,
    onPrimary = TuneScoutColors.background,
    background = TuneScoutColors.background,
    onBackground = TuneScoutColors.textPrimary,
    surface = TuneScoutColors.background,
    onSurface = TuneScoutColors.textPrimary,
    surfaceVariant = TuneScoutColors.white10,
    onSurfaceVariant = TuneScoutColors.textSecondary,
    surfaceContainer = TuneScoutColors.white10,
    surfaceContainerHigh = TuneScoutColors.white20,
    error = TuneScoutColors.error,
)

@Composable
fun TuneScoutTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkScheme,
        typography = tuneScoutTypography,
        content = content,
    )
}
