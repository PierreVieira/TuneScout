package com.pierre.tunescout.core.navigation.deeplink

import androidx.navigation3.runtime.NavKey

/**
 * Builds the back stack a deep link lands on, by walking [DeepLinkKey.parent] up from the target
 * until a route that is nobody's child is reached.
 */
class SyntheticBackStackFactory {
    /**
     * @return [route] preceded by its ancestors, root first. A route that is not a [DeepLinkKey]
     * is a root, and comes back on its own.
     */
    fun buildBackStack(route: NavKey): List<NavKey> {
        val backStack = mutableListOf(route)
        var current = route
        while (current is DeepLinkKey) {
            current = current.parent
            backStack.add(0, current)
        }
        return backStack
    }
}
