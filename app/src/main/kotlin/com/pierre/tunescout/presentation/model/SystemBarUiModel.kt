package com.pierre.tunescout.presentation.model

import androidx.activity.SystemBarStyle
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/**
 * One system bar, described with the same three cases [SystemBarStyle] is built from. It is kept as
 * plain data because [SystemBarStyle] has no `equals` and hides its fields, so a state holding one
 * could neither be compared nor checked in a test. The style is built from that data on read, and
 * being a getter it stays out of the generated `equals`.
 */
sealed interface SystemBarUiModel {
    /** What the activity hands to `enableEdgeToEdge` for this bar. */
    val style: SystemBarStyle

    /**
     * Dark icons over a light bar.
     *
     * @property scrim the bar's background.
     * @property darkScrim the background used instead on devices that cannot draw dark icons.
     */
    data class Light(
        val scrim: Color,
        val darkScrim: Color,
    ) : SystemBarUiModel {
        override val style: SystemBarStyle
            get() = SystemBarStyle.light(scrim.toArgb(), darkScrim.toArgb())
    }

    /**
     * Light icons over a dark bar.
     *
     * @property scrim the bar's background.
     */
    data class Dark(
        val scrim: Color,
    ) : SystemBarUiModel {
        override val style: SystemBarStyle
            get() = SystemBarStyle.dark(scrim.toArgb())
    }

    /**
     * Light or dark as the device is when the style is applied. Whether the device is in dark mode
     * is a configuration value the ViewModel cannot see, so the choice is left to that moment; a
     * dark mode switch recreates the activity, which applies the style again.
     *
     * @property lightScrim the bar's background while the device is light.
     * @property darkScrim the bar's background while the device is dark.
     */
    data class FollowSystem(
        val lightScrim: Color,
        val darkScrim: Color,
    ) : SystemBarUiModel {
        override val style: SystemBarStyle
            get() = SystemBarStyle.auto(lightScrim.toArgb(), darkScrim.toArgb())
    }
}
