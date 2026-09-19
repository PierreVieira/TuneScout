package com.pierre.tunescout.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.pierre.tunescout.core.navigation.BackStackController
import com.pierre.tunescout.core.navigation.NavigationCommandCollector
import com.pierre.tunescout.core.navigation.route.SplashRoute
import com.pierre.tunescout.core.navigation.scene.BottomSheetSceneStrategy
import com.pierre.tunescout.feature.album.presentation.navigation.albumEntry
import com.pierre.tunescout.feature.miniplayer.presentation.content.MiniPlayerScaffold
import com.pierre.tunescout.feature.player.presentation.navigation.playerEntry
import com.pierre.tunescout.feature.queue.presentation.navigation.queueEntry
import com.pierre.tunescout.feature.songs.presentation.navigation.songsEntries
import com.pierre.tunescout.feature.splash.presentation.navigation.splashEntry
import com.pierre.tunescout.ui.theme.TuneScoutColors

@Composable
fun TuneScoutNavDisplay(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(SplashRoute)
    val backStackController = remember { BackStackController(backStack = backStack) }
    val bottomSheetStrategy = remember { BottomSheetSceneStrategy<NavKey>(containerColor = TuneScoutColors.sheet) }

    NavigationCommandCollector(backStackController = backStackController)

    MiniPlayerScaffold(
        isAllowed = isMiniPlayerAllowed(backStack),
        modifier = modifier,
    ) {
        NavDisplay(
            backStack = backStack,
            onBack = backStackController::navigateBack,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            sceneStrategies = listOf(bottomSheetStrategy),
            entryProvider = entryProvider<NavKey> {
                splashEntry()
                songsEntries()
                playerEntry()
                queueEntry()
                albumEntry()
            },
        )
    }
}
