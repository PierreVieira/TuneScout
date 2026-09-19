package com.pierre.tunescout.ui.theme

import androidx.compose.ui.graphics.Color

val darkColorPalette = TuneScoutColorPalette(
    background = Color(0xFF000000),
    sheet = Color(0xFF262626),
    surfaceSubtle = Color(0x1AFFFFFF),
    surfaceRaised = Color(0x33FFFFFF),
    skeleton = Color(0x33FFFFFF),
    textPrimary = Color(0xFFFFFFFF),
    textEmphasis = Color(0xB3FFFFFF),
    textTertiary = Color(0x99FFFFFF),
    textPlaceholder = Color(0xFFA8A8A8),
    textSecondary = Color(0xFF737373),
    elementMuted = Color(0xFF545454),
    elementPlaceholder = Color(0xFFBFBFBF),
    elementSubtle = Color(0x40FFFFFF),
    trackActive = Color(0x99FFFFFF),
    error = Color(0xFFFF453A),
)

val lightColorPalette = TuneScoutColorPalette(
    background = Color(0xFFFFFFFF),
    sheet = Color(0xFFF2F2F2),
    surfaceSubtle = Color(0x14000000),
    surfaceRaised = Color(0x1F000000),
    skeleton = Color(0x1F000000),
    textPrimary = Color(0xFF000000),
    textEmphasis = Color(0xB3000000),
    textTertiary = Color(0x99000000),
    textPlaceholder = Color(0xFF6B6B6B),
    textSecondary = Color(0xFF8C8C8C),
    elementMuted = Color(0xFFABABAB),
    elementPlaceholder = Color(0xFF595959),
    elementSubtle = Color(0x40000000),
    trackActive = Color(0x99000000),
    error = Color(0xFFD70015),
)

internal fun getStaticColorPalette(isDark: Boolean): TuneScoutColorPalette =
    if (isDark) darkColorPalette else lightColorPalette
