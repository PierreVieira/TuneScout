package com.pierre.tunescout

import androidx.test.platform.app.InstrumentationRegistry

/** The width Material starts calling expanded, which is where the app lays out two panes. */
private const val EXPANDED_WIDTH_DP = 840

/** Whether the window the tests run in lays a pane beside the tabs: the player, or the album opened. */
internal val isTwoPaneWindow: Boolean
    get() = InstrumentationRegistry
        .getInstrumentation()
        .targetContext.resources.configuration.screenWidthDp >= EXPANDED_WIDTH_DP
