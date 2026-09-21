package com.pierre.tunescout.core.navigation

import androidx.navigation3.runtime.NavKey

sealed interface NavigationCommand {
    data class Navigate(
        val route: NavKey,
    ) : NavigationCommand

    data class ReplaceTop(
        val route: NavKey,
    ) : NavigationCommand

    /**
     * @property routes the whole back stack, root first.
     */
    data class ResetTo(
        val routes: List<NavKey>,
    ) : NavigationCommand

    data object Back : NavigationCommand
}
