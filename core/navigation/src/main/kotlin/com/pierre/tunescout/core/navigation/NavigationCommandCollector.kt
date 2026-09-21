package com.pierre.tunescout.core.navigation

import androidx.compose.runtime.Composable
import com.pierre.tunescout.ui.utils.ActionCollector
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
            is NavigationCommand.ResetTo -> backStackController.resetTo(command.routes)
            NavigationCommand.Back -> backStackController.navigateBack()
        }
    }
}
