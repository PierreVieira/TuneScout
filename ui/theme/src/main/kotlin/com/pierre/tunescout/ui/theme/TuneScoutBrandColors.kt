package com.pierre.tunescout.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * The splash palette. It is the one surface that does not follow the selected theme: the system
 * splash window it continues is declared in resources, long before a preference can be read.
 */
object TuneScoutBrandColors {
    val splashBackground: Color = Color(0xFF000000)

    val splashGradientEnd: Color = Color(0xFF00343E)
}
