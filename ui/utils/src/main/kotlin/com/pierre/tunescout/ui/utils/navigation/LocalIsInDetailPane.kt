package com.pierre.tunescout.ui.utils.navigation

import androidx.compose.runtime.compositionLocalOf

/**
 * Whether the entry being drawn is the detail beside a list, provided by the list-detail scene around
 * its detail pane. A screen that would lay itself out for the whole window reads it to fit the pane
 * instead. It defaults to false: an entry drawn alone has the window to itself.
 */
val LocalIsInDetailPane = compositionLocalOf { false }
