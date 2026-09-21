package com.pierre.tunescout.core.navigation

import androidx.navigation3.runtime.NavKey
import com.pierre.tunescout.core.navigation.deeplink.SyntheticBackStackFactory
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

class ChannelNavigator(
    private val syntheticBackStackFactory: SyntheticBackStackFactory,
) : Navigator {
    private val channel = Channel<NavigationCommand>(capacity = Channel.UNLIMITED)

    override val commands: Flow<NavigationCommand> = channel.receiveAsFlow()

    override fun navigate(route: NavKey) {
        channel.trySend(NavigationCommand.Navigate(route))
    }

    override fun navigateReplacingTop(route: NavKey) {
        channel.trySend(NavigationCommand.ReplaceTop(route))
    }

    override fun navigateToDeepLink(route: NavKey) {
        channel.trySend(NavigationCommand.ResetTo(syntheticBackStackFactory.buildBackStack(route)))
    }

    override fun navigateBack() {
        channel.trySend(NavigationCommand.Back)
    }
}
