package com.pierre.tunescout.navigation

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.pierre.tunescout.core.navigation.BackStackController
import com.pierre.tunescout.core.navigation.NavigationCommandCollector
import com.pierre.tunescout.core.navigation.animation.rememberSharedElementNavEntryDecorator
import com.pierre.tunescout.core.navigation.route.SplashRoute
import com.pierre.tunescout.core.navigation.scene.BottomSheetSceneStrategy
import com.pierre.tunescout.feature.addtoplaylist.presentation.navigation.addToPlaylistEntry
import com.pierre.tunescout.feature.album.presentation.navigation.albumEntry
import com.pierre.tunescout.feature.album.presentation.navigation.albumOptionsEntry
import com.pierre.tunescout.feature.library.presentation.navigation.createPlaylistEntry
import com.pierre.tunescout.feature.library.presentation.navigation.favoritesEntry
import com.pierre.tunescout.feature.library.presentation.navigation.favoritesOptionsEntry
import com.pierre.tunescout.feature.library.presentation.navigation.librarySearchEntry
import com.pierre.tunescout.feature.library.presentation.navigation.playlistEntry
import com.pierre.tunescout.feature.library.presentation.navigation.playlistOptionsEntry
import com.pierre.tunescout.feature.miniplayer.presentation.content.MiniPlayerScaffold
import com.pierre.tunescout.feature.player.presentation.navigation.playerEntry
import com.pierre.tunescout.feature.queue.presentation.navigation.queueEntry
import com.pierre.tunescout.feature.songoptions.presentation.navigation.songOptionsEntry
import com.pierre.tunescout.feature.splash.presentation.navigation.splashEntry
import com.pierre.tunescout.feature.themeselection.presentation.navigation.dynamicColorInfoEntry
import com.pierre.tunescout.feature.themeselection.presentation.navigation.themeSelectionEntry
import com.pierre.tunescout.navigation.home.homeEntry
import com.pierre.tunescout.navigation.home.homeNavigationItems
import com.pierre.tunescout.navigation.home.rememberHomeTabsState
import com.pierre.tunescout.ui.component.TuneScoutNavigationSuite
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.utils.animation.LocalSharedTransitionScope
import com.pierre.tunescout.ui.utils.animation.LocalTappedSharedArtworkSurface
import com.pierre.tunescout.ui.utils.animation.rememberTappedSharedArtworkSurface
import com.pierre.tunescout.ui.utils.scroll.LocalHideableBarsState
import com.pierre.tunescout.ui.utils.scroll.rememberHideableBarsState
import com.pierre.tunescout.ui.utils.window.rememberWindowSize

@Composable
fun TuneScoutNavDisplay(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(SplashRoute)
    val backStackController = remember { BackStackController(backStack = backStack) }
    val bottomSheetStrategy = remember { BottomSheetSceneStrategy<NavKey>(containerColor = { TuneScoutColors.sheet }) }
    val dialogStrategy = remember { DialogSceneStrategy<NavKey>() }
    val tabsState = rememberHomeTabsState()

    NavigationCommandCollector(backStackController = backStackController)

    // The layout covers the mini player bar as well as the NavDisplay: the artwork flies between
    // the two, so both halves have to sit in the same shared transition scope.
    SharedTransitionLayout(modifier = modifier) {
        CompositionLocalProvider(
            LocalSharedTransitionScope provides this,
            LocalTappedSharedArtworkSurface provides rememberTappedSharedArtworkSurface(),
            LocalHideableBarsState provides rememberHideableBarsState(),
        ) {
            TuneScoutNavigationSuite(
                items = homeNavigationItems(tabsState = tabsState),
                isVisible = isHomeVisible(backStack),
                windowSize = rememberWindowSize(),
            ) {
                MiniPlayerScaffold(isAllowed = isMiniPlayerAllowed(backStack)) {
                    NavDisplay(
                        backStack = backStack,
                        onBack = backStackController::navigateBack,
                        entryDecorators = listOf(
                            rememberSaveableStateHolderNavEntryDecorator(),
                            rememberViewModelStoreNavEntryDecorator(),
                            rememberSharedElementNavEntryDecorator(),
                        ),
                        sceneStrategies = listOf(bottomSheetStrategy, dialogStrategy),
                        transitionSpec = createNavTransitionSpec(),
                        popTransitionSpec = createNavTransitionSpec(),
                        predictivePopTransitionSpec = createNavPredictivePopTransitionSpec(),
                        entryProvider = entryProvider {
                            splashEntry()
                            homeEntry(tabsState = tabsState)
                            librarySearchEntry()
                            favoritesEntry()
                            favoritesOptionsEntry()
                            playlistEntry()
                            playlistOptionsEntry()
                            createPlaylistEntry()
                            songOptionsEntry()
                            addToPlaylistEntry()
                            playerEntry()
                            queueEntry()
                            albumEntry()
                            albumOptionsEntry()
                            themeSelectionEntry()
                            dynamicColorInfoEntry()
                        },
                    )
                }
            }
        }
    }
}
