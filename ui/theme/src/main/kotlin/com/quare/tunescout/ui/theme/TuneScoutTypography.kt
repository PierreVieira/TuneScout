package com.quare.tunescout.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val fontFamily = FontFamily.SansSerif

private fun buildDisplayStyle(size: Int): TextStyle = TextStyle(
    fontFamily = fontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = size.sp,
    lineHeight = (size * 1.2f).sp,
)

private fun buildTextStyle(
    size: Int,
    lineHeightFactor: Float = 1.2f,
): TextStyle = TextStyle(
    fontFamily = fontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = size.sp,
    lineHeight = (size * lineHeightFactor).sp,
)

internal val tuneScoutTypography: Typography = Typography(
    displayMedium = buildDisplayStyle(size = 32),
    headlineMedium = buildDisplayStyle(size = 24),
    titleLarge = buildDisplayStyle(size = 20),
    titleMedium = buildDisplayStyle(size = 16),
    bodyLarge = buildTextStyle(size = 16),
    bodyMedium = buildTextStyle(size = 14),
    bodySmall = buildTextStyle(size = 12, lineHeightFactor = 1.4f),
)
