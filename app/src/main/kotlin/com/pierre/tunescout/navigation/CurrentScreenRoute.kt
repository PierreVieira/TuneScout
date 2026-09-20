package com.pierre.tunescout.navigation

import androidx.navigation3.runtime.NavKey
import com.pierre.tunescout.core.navigation.route.HomeRoute
import com.pierre.tunescout.core.navigation.route.OverlayRoute
import com.pierre.tunescout.core.navigation.route.PlayerRoute
import com.pierre.tunescout.core.navigation.route.SplashRoute

/**
 * A sheet on top of a screen does not change which screen the user is on, so the overlays are
 * looked past: the options sheet opened from the player is still the player.
 *
 * @return the topmost route that is not an overlay, or null when there is none.
 */
internal fun List<NavKey>.findCurrentScreenRouteOrNull(): NavKey? = lastOrNull { route -> route !is OverlayRoute }

/**
 * The mini player is a shortcut back to the player, so it is hidden on the player itself and on the
 * splash.
 *
 * @return whether the current screen may show the mini player.
 */
internal fun List<NavKey>.isMiniPlayerAllowed(): Boolean = when (findCurrentScreenRouteOrNull()) {
    null, SplashRoute -> false
    is PlayerRoute -> false
    else -> true
}

/**
 * The tab bar belongs to the tab host, and to nothing pushed on top of it.
 *
 * @return whether the current screen is the tab host.
 */
internal fun List<NavKey>.isHomeVisible(): Boolean = findCurrentScreenRouteOrNull() == HomeRoute
