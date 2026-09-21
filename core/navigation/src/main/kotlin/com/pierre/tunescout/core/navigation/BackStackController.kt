package com.pierre.tunescout.core.navigation

import androidx.navigation3.runtime.NavKey

class BackStackController(
    private val backStack: MutableList<NavKey>,
) {
    fun navigate(route: NavKey) {
        if (route != backStack.lastOrNull()) {
            backStack.add(route)
        }
    }

    fun navigateReplacingTop(route: NavKey) {
        if (route != backStack.lastOrNull()) {
            backStack.removeLastOrNull()
            backStack.add(route)
        }
    }

    /**
     * An empty list is ignored: a back stack with nothing in it has no screen to draw, and a deep
     * link that resolved to nothing should leave the app where it already was.
     */
    fun resetTo(routes: List<NavKey>) {
        if (routes.isEmpty() || routes == backStack) return
        backStack.clear()
        backStack.addAll(routes)
    }

    fun navigateBack() {
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
        }
    }
}
