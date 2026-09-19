package com.pierre.tunescout.core.navigation.route

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * The tab host. It is one entry of the root back stack, and the two tabs it switches between live
 * in a nested display of its own, so a screen pushed from a tab covers the bar like any other.
 */
@Serializable
data object HomeRoute : NavKey
