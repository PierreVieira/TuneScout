package com.pierre.tunescout.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * The splash gradient ends on a deep teal that is unreadable as a highlight, so the accent keeps
 * its hue and takes the lightness each palette needs. With dynamic colors on, the accent comes
 * from the wallpaper instead — see [toColorPalette].
 *
 * The container is the same hue with most of the chroma taken out, the way Material derives a
 * secondary container: it is what a selected navigation item sits on, which should read as a
 * surface the accent tinted rather than as the accent itself.
 */
private val darkAccent = Color(0xFF22C39B)
private val lightAccent = Color(0xFF007558)
private val darkAccentContainer = Color(0xFF334D46)
private val lightAccentContainer = Color(0xFFCDE4DE)

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
    textSecondary = Color(0xFF8E8E8E),
    elementMuted = Color(0xFF545454),
    elementPlaceholder = Color(0xFFBFBFBF),
    elementSubtle = Color(0x40FFFFFF),
    accent = darkAccent,
    accentContainer = darkAccentContainer,
    trackActive = Color(0x99FFFFFF),
    error = Color(0xFFFF5A50),
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
    textPlaceholder = Color(0xFF616161),
    textSecondary = Color(0xFF696969),
    elementMuted = Color(0xFFABABAB),
    elementPlaceholder = Color(0xFF595959),
    elementSubtle = Color(0x40000000),
    accent = lightAccent,
    accentContainer = lightAccentContainer,
    trackActive = Color(0x99000000),
    error = Color(0xFFD70015),
)
