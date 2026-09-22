package com.pierre.tunescout.feature.themeselection.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.ui.theme.Theme
import com.pierre.tunescout.ui.theme.TuneScoutColorPalette
import com.pierre.tunescout.ui.theme.colorPalette

private val previewHeight = 56.dp
private val previewCornerRadius = 8.dp
private val previewPadding = 7.dp
private val lineSpacing = 4.dp
private val lineCornerRadius = 3.dp
private val firstLineHeight = 7.dp
private val otherLineHeight = 5.dp
private val accentWidth = 36.dp
private val accentHeight = 12.dp
private const val FIRST_LINE_WIDTH_FRACTION = 0.55f
private const val SECOND_LINE_WIDTH_FRACTION = 0.85f
private const val THIRD_LINE_WIDTH_FRACTION = 0.7f
private const val OTHER_LINES_ALPHA = 0.7f
private const val SPLIT_STOP = 0.5f
private const val STATIC_ACCENT_ALPHA = 0.6f

@Composable
internal fun ThemePreviewCard(
    theme: Theme,
    isDynamicColorEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = theme.toPreviewColors(
        light = colorPalette(isDark = false, isDynamicColorEnabled = isDynamicColorEnabled),
        dark = colorPalette(isDark = true, isDynamicColorEnabled = isDynamicColorEnabled),
        accentAlpha = if (isDynamicColorEnabled) 1f else STATIC_ACCENT_ALPHA,
    )
    Column(
        modifier = modifier
            .height(previewHeight)
            .clip(RoundedCornerShape(previewCornerRadius))
            .background(colors.background)
            .padding(previewPadding),
        verticalArrangement = Arrangement.spacedBy(lineSpacing),
    ) {
        PreviewLine(
            color = colors.firstLine,
            widthFraction = FIRST_LINE_WIDTH_FRACTION,
            height = firstLineHeight,
        )
        PreviewLine(
            color = colors.otherLines,
            widthFraction = SECOND_LINE_WIDTH_FRACTION,
            height = otherLineHeight,
        )
        PreviewLine(
            color = colors.otherLines,
            widthFraction = THIRD_LINE_WIDTH_FRACTION,
            height = otherLineHeight,
        )
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(width = accentWidth, height = accentHeight)
                .clip(RoundedCornerShape(previewCornerRadius))
                .background(colors.accent),
        )
    }
}

@Composable
private fun PreviewLine(
    color: Color,
    widthFraction: Float,
    height: Dp,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .height(height)
            .clip(RoundedCornerShape(lineCornerRadius))
            .background(color),
    )
}

private fun Theme.toPreviewColors(
    light: TuneScoutColorPalette,
    dark: TuneScoutColorPalette,
    accentAlpha: Float,
): ThemePreviewColors = when (this) {
    Theme.LIGHT -> light.toPreviewColors(accentAlpha)

    Theme.DARK -> dark.toPreviewColors(accentAlpha)

    Theme.SYSTEM -> ThemePreviewColors(
        background = Brush.linearGradient(
            SPLIT_STOP to light.background,
            SPLIT_STOP to dark.background,
        ),
        firstLine = light.textSecondary,
        otherLines = light.textSecondary.copy(alpha = OTHER_LINES_ALPHA),
        accent = light.toPreviewAccent(accentAlpha),
    )
}

private fun TuneScoutColorPalette.toPreviewColors(accentAlpha: Float): ThemePreviewColors = ThemePreviewColors(
    background = SolidColor(background),
    firstLine = textSecondary,
    otherLines = textSecondary.copy(alpha = OTHER_LINES_ALPHA),
    accent = toPreviewAccent(accentAlpha),
)

/**
 * The static accent is loud at this size, so it is mixed into the background to calm it down. The
 * dynamic one keeps [alpha] at 1: the system palette already picked how loud it is.
 *
 * @return the accent at [alpha], made opaque over this palette's background.
 */
private fun TuneScoutColorPalette.toPreviewAccent(alpha: Float): Color =
    accent.copy(alpha = alpha).compositeOver(background)
