package com.pierre.tunescout.ui.utils.window

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass

/**
 * The width from which the tabs get a pane beside them. Material's expanded width, 840dp, left out
 * phones on their side that come just short of it — a Galaxy A56 at its default display size is
 * 832dp wide — where the pane still fits: each half keeps at least 360dp once the rail takes its
 * share.
 */
private val twoPaneMinWidth = 800.dp

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
 * @property isWidthExpanded whether the window reaches Material's expanded width, 840dp.
 * @property isHeightCompact whether the window is as short as a phone held sideways.
 * @property isTwoPane whether the window is wide enough to lay a pane beside the tabs: 800dp.
 */
data class TuneScoutWindowSize(
    val isWidthCompact: Boolean,
    val isWidthExpanded: Boolean,
    val isHeightCompact: Boolean,
    val isTwoPane: Boolean,
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

    /**
     * Whether a tab has the width to lay its header in one row. It does wherever the window is side by
     * side, except on a phone on its side with a pane beside the tabs: the tab is left some 370dp
     * there, and the search field between the title and the actions was squeezed to a few letters.
     */
    val isTabHeaderInline: Boolean
        get() = isSideBySide && !(isTwoPane && isHeightCompact)
}

@Composable
fun rememberWindowSize(): TuneScoutWindowSize {
    val width = with(LocalDensity.current) {
        LocalWindowInfo.current.containerSize.width
            .toDp()
    }
    return currentWindowAdaptiveInfoV2().windowSizeClass.toWindowSize(width = width)
}

/**
 * The size class only knows which of Material's buckets the window falls in, so a width between two
 * of them is read off the window itself.
 *
 * @param width the width of the window.
 * @return the roles the window plays.
 */
private fun WindowSizeClass.toWindowSize(width: Dp): TuneScoutWindowSize = TuneScoutWindowSize(
    isWidthCompact = !isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND),
    isWidthExpanded = isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND),
    isHeightCompact = !isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND),
    isTwoPane = width >= twoPaneMinWidth,
)
