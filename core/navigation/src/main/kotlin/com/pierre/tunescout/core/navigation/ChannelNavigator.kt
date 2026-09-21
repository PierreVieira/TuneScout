package com.pierre.tunescout.core.navigation

import androidx.navigation3.runtime.NavKey
import com.pierre.tunescout.core.navigation.deeplink.SyntheticBackStackFactory
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

class ChannelNavigator(
    private val syntheticBackStackFactory: SyntheticBackStackFactory,
) : Navigator {
    /**
     * Unlimited so [send] never finds the buffer full: commands sent while nothing is collecting
     * (e.g. during a configuration change) wait here instead of being dropped.
     */
    private val channel = Channel<NavigationCommand>(capacity = Channel.UNLIMITED)

    override val commands: Flow<NavigationCommand> = channel.receiveAsFlow()

    override fun navigate(route: NavKey) {
        send(NavigationCommand.Navigate(route))
    }

    override fun navigateReplacingTop(route: NavKey) {
        send(NavigationCommand.ReplaceTop(route))
    }

    override fun navigateToDeepLink(route: NavKey) {
        send(NavigationCommand.ResetTo(syntheticBackStackFactory.buildBackStack(route)))
    }

    override fun navigateBack() {
        send(NavigationCommand.Back)
    }

    /**
     * Uses `trySend` so the [Navigator] methods stay non-suspending. With an unlimited, never-closed
     * [channel] it always succeeds, so a failure means that invariant broke and a navigation would
     * otherwise be lost silently.
     */
    private fun send(command: NavigationCommand) {
        check(channel.trySend(command).isSuccess) { "Navigation command dropped: $command" }
    }
}
