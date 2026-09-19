package com.pierre.tunescout.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

private const val SUBTLE_ALPHA = 0.10f
private const val RAISED_ALPHA = 0.20f
private const val EMPHASIS_ALPHA = 0.70f
private const val TERTIARY_ALPHA = 0.60f
private const val SUBTLE_ELEMENT_ALPHA = 0.25f
private const val INDICATOR_ALPHA = 0.25f

internal fun TuneScoutColorPalette.toColorScheme(isDark: Boolean): ColorScheme =
    (if (isDark) darkColorScheme() else lightColorScheme()).copy(
        primary = textPrimary,
        onPrimary = background,
        background = background,
        onBackground = textPrimary,
        surface = background,
        onSurface = textPrimary,
        surfaceVariant = surfaceSubtle,
        onSurfaceVariant = textSecondary,
        surfaceContainer = surfaceSubtle,
        surfaceContainerHigh = surfaceRaised,
        secondaryContainer = accent.copy(alpha = INDICATOR_ALPHA),
        onSecondaryContainer = accent,
        outlineVariant = elementSubtle,
        error = error,
    )

internal fun ColorScheme.toColorPalette(): TuneScoutColorPalette = TuneScoutColorPalette(
    background = background,
    sheet = surfaceContainerHigh,
    surfaceSubtle = onSurface.copy(alpha = SUBTLE_ALPHA),
    surfaceRaised = onSurface.copy(alpha = RAISED_ALPHA),
    skeleton = onSurface.copy(alpha = RAISED_ALPHA),
    textPrimary = onSurface,
    textEmphasis = onSurface.copy(alpha = EMPHASIS_ALPHA),
    textTertiary = onSurface.copy(alpha = TERTIARY_ALPHA),
    textPlaceholder = onSurfaceVariant,
    textSecondary = outline,
    elementMuted = outline,
    elementPlaceholder = onSurfaceVariant,
    elementSubtle = onSurface.copy(alpha = SUBTLE_ELEMENT_ALPHA),
    accent = primary,
    trackActive = primary,
    error = error,
)
