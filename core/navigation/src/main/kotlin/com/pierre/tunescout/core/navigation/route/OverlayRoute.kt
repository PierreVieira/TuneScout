package com.pierre.tunescout.core.navigation.route

import androidx.navigation3.runtime.NavKey

/**
 * A route drawn over the one below it instead of replacing it — a bottom sheet. Whatever depends on
 * "which screen is the user on" has to look past these to the screen they are still looking at.
 */
interface OverlayRoute : NavKey
