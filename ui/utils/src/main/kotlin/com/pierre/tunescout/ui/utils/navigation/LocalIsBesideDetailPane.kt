package com.pierre.tunescout.ui.utils.navigation

import androidx.compose.runtime.compositionLocalOf

/**
 * Whether the entry being drawn is a list with a detail open beside it, provided by the list-detail
 * scene around its list pane. A list that runs a navigation of its own reads it to hand Back to the
 * detail while one is open. It defaults to false: a list drawn alone has nothing beside it.
 */
val LocalIsBesideDetailPane = compositionLocalOf { false }
