package com.pierre.tunescout.ui.utils.window

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowSizeClass

/**
 * The single responsiveness ruler of the app. Screens read a role off this instead of comparing the
 * constraints they happen to be measured with, so a navigation rail taking width on the side cannot
 * change what the content believes the window is.
 *
 * It is resolved once, in the `*Screen` composable, and passed down: the `*Content` composables are
 * rendered on their own by the screenshot generators and the Compose tests, where there is no real
 * window to measure.
 *
 * @property isWidthCompact whether the window is as narrow as a phone held upright.
 * @property isWidthExpanded whether the window is wide enough to lay panes side by side.
 * @property isHeightCompact whether the window is as short as a phone held sideways.
 */
data class TuneScoutWindowSize(
    val isWidthCompact: Boolean,
    val isWidthExpanded: Boolean,
    val isHeightCompact: Boolean,
) {
    /**
     * A window wide enough for a rail beside the content. Material only puts the bar back on a
     * compact width, which is the phone held upright.
     */
    val hasNavigationRail: Boolean
        get() = !isWidthCompact

    /**
     * A window with more width than height to spend: the phone turned sideways, or a tablet wide
     * enough that a stacked layout would strand its content on one side. Headers lay themselves out
     * in a row, and the player puts its artwork beside the controls.
     */
    val isSideBySide: Boolean
        get() = isHeightCompact || isWidthExpanded
}

@Composable
fun rememberWindowSize(): TuneScoutWindowSize = currentWindowAdaptiveInfoV2().windowSizeClass.toWindowSize()

private fun WindowSizeClass.toWindowSize(): TuneScoutWindowSize = TuneScoutWindowSize(
    isWidthCompact = !isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND),
    isWidthExpanded = isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND),
    isHeightCompact = !isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND),
)
