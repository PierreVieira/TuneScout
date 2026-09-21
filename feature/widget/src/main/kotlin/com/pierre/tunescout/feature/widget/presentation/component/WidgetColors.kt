package com.pierre.tunescout.feature.widget.presentation.component

import androidx.compose.ui.graphics.Color
import androidx.glance.color.ColorProvider
import androidx.glance.unit.ColorProvider
import com.pierre.tunescout.ui.theme.TuneScoutColorPalette
import com.pierre.tunescout.ui.theme.darkColorPalette
import com.pierre.tunescout.ui.theme.lightColorPalette

/**
 * The palette the widgets draw with.
 *
 * A widget is drawn by the launcher, not by the app, so it cannot read the theme the user picked
 * in the app: `RemoteViews` carries a day and a night colour and the system chooses between them.
 * Both values come from the app's own palettes, so a widget never shows a colour the app does not
 * have — it just follows the system's night mode instead of the stored preference.
 */
object WidgetColors {
    val background: ColorProvider = createColorProvider { palette -> palette.sheet }
    val surface: ColorProvider = createColorProvider { palette -> palette.surfaceSubtle }
    val textPrimary: ColorProvider = createColorProvider { palette -> palette.textPrimary }
    val textSecondary: ColorProvider = createColorProvider { palette -> palette.textEmphasis }
    val element: ColorProvider = createColorProvider { palette -> palette.textPrimary }
    val elementDisabled: ColorProvider = createColorProvider { palette -> palette.elementMuted }
    val accent: ColorProvider = createColorProvider { palette -> palette.accent }

    /**
     * @return the colour [select] reads, taken from the light palette by day and from the dark one
     * by night.
     */
    private fun createColorProvider(select: (TuneScoutColorPalette) -> Color): ColorProvider = ColorProvider(
        day = select(lightColorPalette),
        night = select(darkColorPalette),
    )
}
