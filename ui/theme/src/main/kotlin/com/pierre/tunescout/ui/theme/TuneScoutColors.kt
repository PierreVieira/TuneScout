package com.pierre.tunescout.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object TuneScoutColors {
    val background: Color
        @Composable get() = LocalTuneScoutColorPalette.current.background

    val sheet: Color
        @Composable get() = LocalTuneScoutColorPalette.current.sheet

    val surfaceSubtle: Color
        @Composable get() = LocalTuneScoutColorPalette.current.surfaceSubtle

    val surfaceRaised: Color
        @Composable get() = LocalTuneScoutColorPalette.current.surfaceRaised

    val skeleton: Color
        @Composable get() = LocalTuneScoutColorPalette.current.skeleton

    val textPrimary: Color
        @Composable get() = LocalTuneScoutColorPalette.current.textPrimary

    val textEmphasis: Color
        @Composable get() = LocalTuneScoutColorPalette.current.textEmphasis

    val textTertiary: Color
        @Composable get() = LocalTuneScoutColorPalette.current.textTertiary

    val textPlaceholder: Color
        @Composable get() = LocalTuneScoutColorPalette.current.textPlaceholder

    val textSecondary: Color
        @Composable get() = LocalTuneScoutColorPalette.current.textSecondary

    val elementMuted: Color
        @Composable get() = LocalTuneScoutColorPalette.current.elementMuted

    val elementPlaceholder: Color
        @Composable get() = LocalTuneScoutColorPalette.current.elementPlaceholder

    val elementSubtle: Color
        @Composable get() = LocalTuneScoutColorPalette.current.elementSubtle

    val trackActive: Color
        @Composable get() = LocalTuneScoutColorPalette.current.trackActive

    val error: Color
        @Composable get() = LocalTuneScoutColorPalette.current.error
}
