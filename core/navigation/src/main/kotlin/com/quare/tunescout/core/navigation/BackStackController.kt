package com.quare.tunescout.core.navigation

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

    fun navigateBack() {
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
        }
    }
}
