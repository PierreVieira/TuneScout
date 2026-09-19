package com.pierre.tunescout.navigation

import androidx.navigation3.runtime.NavKey
import com.pierre.tunescout.core.navigation.route.OverlayRoute
import com.pierre.tunescout.core.navigation.route.PlayerRoute
import com.pierre.tunescout.core.navigation.route.SplashRoute

/**
 * The mini player is a shortcut back to the player, so it is hidden on the player itself and on the
 * splash. A sheet on top of a screen does not change which screen that is, so overlays are looked
 * past: the options sheet opened from the player is still the player.
 */
internal fun isMiniPlayerAllowed(backStack: List<NavKey>): Boolean =
    when (backStack.lastOrNull { route -> route !is OverlayRoute }) {
        null, SplashRoute -> false
        is PlayerRoute -> false
        else -> true
    }
