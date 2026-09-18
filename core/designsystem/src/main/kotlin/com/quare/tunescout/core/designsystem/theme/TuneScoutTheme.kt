package com.quare.tunescout.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val darkScheme: ColorScheme = darkColorScheme(
    primary = white,
    onPrimary = black,
    background = black,
    onBackground = white,
    surface = black,
    onSurface = white,
    surfaceVariant = surfaceGray,
    onSurfaceVariant = secondaryText,
    surfaceContainer = surfaceGray,
    surfaceContainerHigh = elevatedGray,
    error = errorRed,
)

@Composable
fun TuneScoutTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkScheme,
        content = content,
    )
}
