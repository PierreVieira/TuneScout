package com.pierre.tunescout.core.navigation.route

import androidx.navigation3.runtime.NavKey

/**
 * A route that opens beside the list it was picked from when the window is wide enough for two panes,
 * instead of covering it. Whatever reads the back stack to know what is on screen has to count the list
 * under it as still drawn there — the navigation rail stays beside the tabs.
 *
 * The marker is for readers of the back stack; the scene itself is chosen from the entry's metadata,
 * `ListDetailSceneStrategy.detailPane()`, so a route that opens as a detail carries both.
 */
interface DetailPaneRoute : NavKey
