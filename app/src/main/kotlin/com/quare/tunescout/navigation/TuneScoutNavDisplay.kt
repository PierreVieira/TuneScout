package com.quare.tunescout.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.quare.tunescout.core.navigation.BackStackController
import com.quare.tunescout.core.navigation.NavigationCommandCollector
import com.quare.tunescout.core.navigation.route.SongsRoute

@Composable
fun TuneScoutNavDisplay(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(SongsRoute)
    val backStackController = remember { BackStackController(backStack = backStack) }

    NavigationCommandCollector(backStackController = backStackController)

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = backStackController::navigateBack,
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
