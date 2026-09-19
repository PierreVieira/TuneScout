package com.pierre.tunescout.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class TuneScoutColorPalette(
    val background: Color,
    val sheet: Color,
    val surfaceSubtle: Color,
    val surfaceRaised: Color,
    val skeleton: Color,
    val textPrimary: Color,
    val textEmphasis: Color,
    val textTertiary: Color,
    val textPlaceholder: Color,
    val textSecondary: Color,
    val elementMuted: Color,
    val elementPlaceholder: Color,
    val elementSubtle: Color,
    val accent: Color,
    val trackActive: Color,
    val error: Color,
)

val LocalTuneScoutColorPalette = staticCompositionLocalOf { darkColorPalette }
