package com.pierre.tunescout

import androidx.test.platform.app.InstrumentationRegistry

/** The width from which the app lays out two panes: `TuneScoutWindowSize.isTwoPane`. */
private const val TWO_PANE_WIDTH_DP = 800

/** Whether the window the tests run in lays a pane beside the tabs: the player, or the album opened. */
internal val isTwoPaneWindow: Boolean
    get() = InstrumentationRegistry
        .getInstrumentation()
        .targetContext.resources.configuration.screenWidthDp >= TWO_PANE_WIDTH_DP
