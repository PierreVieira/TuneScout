package com.pierre.tunescout.navigation

import androidx.navigation3.runtime.NavKey
import com.pierre.tunescout.core.navigation.route.PlayerRoute
import com.pierre.tunescout.core.navigation.route.QueueRoute
import com.pierre.tunescout.core.navigation.route.SplashRoute

/**
 * The mini player is a shortcut back to the player, so it is hidden on the screens that already
 * are the player, on the queue it opens, and on the splash.
 */
internal fun isMiniPlayerAllowed(route: NavKey?): Boolean = when (route) {
    null, SplashRoute, QueueRoute -> false
    is PlayerRoute -> false
    else -> true
}
