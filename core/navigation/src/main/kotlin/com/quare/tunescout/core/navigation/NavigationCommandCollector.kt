package com.quare.tunescout.core.navigation

import androidx.compose.runtime.Composable
import com.quare.tunescout.ui.utils.ActionCollector
import org.koin.compose.koinInject

@Composable
fun NavigationCommandCollector(
    backStackController: BackStackController,
    navigator: Navigator = koinInject(),
) {
    ActionCollector(navigator.commands) { command ->
        when (command) {
            is NavigationCommand.Navigate -> backStackController.navigate(command.route)
            is NavigationCommand.ReplaceTop -> backStackController.navigateReplacingTop(command.route)
            NavigationCommand.Back -> backStackController.navigateBack()
        }
    }
}
