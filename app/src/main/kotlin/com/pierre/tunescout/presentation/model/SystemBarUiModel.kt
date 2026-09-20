package com.pierre.tunescout.presentation.model

import androidx.compose.ui.graphics.Color

/**
 * One system bar, described with the same three cases `SystemBarStyle` is built from. The activity
 * turns it into that style; it is kept as plain data here because `SystemBarStyle` has no `equals`
 * and hides its fields, so a state holding one could neither be compared nor checked in a test.
 */
sealed interface SystemBarUiModel {
    /**
     * Dark icons over a light bar.
     *
     * @property scrim the bar's background.
     * @property darkScrim the background used instead on devices that cannot draw dark icons.
     */
    data class Light(
        val scrim: Color,
        val darkScrim: Color,
    ) : SystemBarUiModel

    /**
     * Light icons over a dark bar.
     *
     * @property scrim the bar's background.
     */
    data class Dark(
        val scrim: Color,
    ) : SystemBarUiModel

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
    ) : SystemBarUiModel
}
