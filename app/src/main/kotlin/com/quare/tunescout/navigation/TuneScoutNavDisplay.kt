package com.quare.tunescout.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.quare.tunescout.core.navigation.NavigationCommand
import com.quare.tunescout.core.navigation.Navigator
import com.quare.tunescout.core.navigation.route.SongsRoute
import org.koin.compose.koinInject

@Composable
fun TuneScoutNavDisplay(navigator: Navigator = koinInject()) {
    val backStack = rememberNavBackStack(SongsRoute)

    LaunchedEffect(navigator) {
        navigator.commands.collect { command ->
            when (command) {
                is NavigationCommand.Navigate -> backStack.add(command.route)

                is NavigationCommand.ReplaceTop -> {
                    backStack.removeLastOrNull()
                    backStack.add(command.route)
                }

                NavigationCommand.Back -> backStack.removeLastOrNull()
            }
        }
    }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider<NavKey> {
            entry<SongsRoute> {
                Placeholder()
            }
        },
    )
}

@Composable
private fun Placeholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "TuneScout",
            style = MaterialTheme.typography.headlineMedium,
        )
    }
}
