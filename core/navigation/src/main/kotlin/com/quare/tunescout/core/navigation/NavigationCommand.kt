package com.quare.tunescout.core.navigation

import androidx.navigation3.runtime.NavKey

sealed interface NavigationCommand {
    data class Navigate(
        val route: NavKey,
    ) : NavigationCommand

    data class ReplaceTop(
        val route: NavKey,
    ) : NavigationCommand

    data object Back : NavigationCommand
}
