package com.pierre.tunescout.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.flow.Flow

/**
 * Event bus between ViewModels and the single NavDisplay owned by the app module. Features only
 * emit commands; the back stack is mutated in one place, inside composition.
 */
interface Navigator {
    val commands: Flow<NavigationCommand>

    fun navigate(route: NavKey)

    fun navigateReplacingTop(route: NavKey)

    /**
     * Replaces the whole back stack with [route] and the screens the user would have passed
     * through to reach it, root first.
     */
    fun navigateToDeepLink(route: NavKey)

    fun navigateBack()
}
