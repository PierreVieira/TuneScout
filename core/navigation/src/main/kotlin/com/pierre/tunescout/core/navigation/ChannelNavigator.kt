package com.pierre.tunescout.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

class ChannelNavigator : Navigator {
    private val channel = Channel<NavigationCommand>(capacity = Channel.UNLIMITED)

    override val commands: Flow<NavigationCommand> = channel.receiveAsFlow()

    override fun navigate(route: NavKey) {
        channel.trySend(NavigationCommand.Navigate(route))
    }

    override fun navigateReplacingTop(route: NavKey) {
        channel.trySend(NavigationCommand.ReplaceTop(route))
    }

    override fun navigateResettingTo(routes: List<NavKey>) {
        channel.trySend(NavigationCommand.ResetTo(routes))
    }

    override fun navigateBack() {
        channel.trySend(NavigationCommand.Back)
    }
}
