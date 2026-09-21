package com.pierre.tunescout.feature.player.presentation.model

import com.pierre.tunescout.ui.utils.window.TuneScoutWindowSize

/** How the player lays its artwork out against the details and controls. */
enum class PlayerLayout {
    /** The artwork above the details, as large as the height allows: a phone upright. */
    Stacked,

    /** The artwork beside the details: a window with height to spare for neither on top of the other. */
    SideBySide,

    /**
     * A small artwork beside the title, and the controls under them at full width: a pane too short
     * for a large artwork, where the controls come first.
     */
    Compact,
    ;

    companion object {
        /**
         * The pane beside the tabs is half the window: tall and narrow on a tablet, where the stacked
         * layout reads best, and short on a phone on its side, where only the compact one leaves the
         * controls on screen.
         *
         * @param windowSize the size of the whole window.
         * @param isInDetailPane whether the player is drawn in the pane beside the tabs.
         * @return the layout the player takes.
         */
        fun of(
            windowSize: TuneScoutWindowSize,
            isInDetailPane: Boolean,
        ): PlayerLayout = when {
            isInDetailPane && windowSize.isHeightCompact -> Compact
            isInDetailPane -> Stacked
            windowSize.isSideBySide -> SideBySide
            else -> Stacked
        }
    }
}
