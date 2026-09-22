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
import com.pierre.tunescout.core.navigation.animation.FadeSceneDecoratorStrategy
import com.pierre.tunescout.core.navigation.animation.createSceneFadeTransform
import com.pierre.tunescout.core.navigation.animation.rememberSharedElementNavEntryDecorator
import com.pierre.tunescout.core.navigation.route.HomeRoute
import com.pierre.tunescout.core.navigation.scene.BottomSheetSceneStrategy
import com.pierre.tunescout.core.navigation.scene.ListDetailSceneStrategy
import com.pierre.tunescout.feature.addtoplaylist.presentation.navigation.addToPlaylistEntry
import com.pierre.tunescout.feature.album.presentation.navigation.albumEntry
import com.pierre.tunescout.feature.album.presentation.navigation.albumOptionsEntry
import com.pierre.tunescout.feature.audiosearch.presentation.navigation.audioSearchEntry
import com.pierre.tunescout.feature.library.presentation.navigation.createPlaylistEntry
import com.pierre.tunescout.feature.library.presentation.navigation.downloadedSongsEntry
import com.pierre.tunescout.feature.library.presentation.navigation.downloadedSongsOptionsEntry
import com.pierre.tunescout.feature.library.presentation.navigation.favoritesEntry
import com.pierre.tunescout.feature.library.presentation.navigation.favoritesOptionsEntry
import com.pierre.tunescout.feature.library.presentation.navigation.librarySearchEntry
import com.pierre.tunescout.feature.library.presentation.navigation.playlistEntry
import com.pierre.tunescout.feature.library.presentation.navigation.playlistOptionsEntry
import com.pierre.tunescout.feature.miniplayer.presentation.content.MiniPlayerScaffold
import com.pierre.tunescout.feature.player.presentation.content.NowPlayingScreen
import com.pierre.tunescout.feature.player.presentation.navigation.playerEntry
import com.pierre.tunescout.feature.queue.presentation.navigation.queueEntry
import com.pierre.tunescout.feature.songoptions.presentation.navigation.songOptionsEntry
import com.pierre.tunescout.feature.themeselection.presentation.navigation.dynamicColorInfoEntry
import com.pierre.tunescout.feature.themeselection.presentation.navigation.themeSelectionEntry
import com.pierre.tunescout.navigation.home.homeEntry
import com.pierre.tunescout.navigation.home.homeNavigationItems
import com.pierre.tunescout.navigation.home.rememberHomeTabsState
import com.pierre.tunescout.ui.component.TuneScoutNavigationSuiteScaffold
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.utils.animation.LocalSharedArtworkDestination
import com.pierre.tunescout.ui.utils.animation.LocalSharedTransitionScope
import com.pierre.tunescout.ui.utils.animation.LocalTappedSharedArtworkSurface
import com.pierre.tunescout.ui.utils.animation.rememberSharedArtworkDestination
import com.pierre.tunescout.ui.utils.animation.rememberTappedSharedArtworkSurface
import com.pierre.tunescout.ui.utils.scroll.LocalHideableBarsState
import com.pierre.tunescout.ui.utils.scroll.rememberHideableBarsState
import com.pierre.tunescout.ui.utils.window.rememberWindowSize

/**
 * The shared transition layout covers the mini player bar as well as the NavDisplay: the artwork
 * flies between the two, so both halves have to sit in the same shared transition scope.
 *
 * On a wide window the tab host always has a pane beside it: the player, which takes the place of the
 * mini player there, or whatever detail was opened over it. With a detail, the mini player comes back
 * under the tabs only: the two-pane scene draws it in the list pane, so the detail keeps its full height.
 *
 * The list-detail strategy comes after the overlays: a sheet opened over the two panes is drawn over
 * both of them, which `NavDisplay` works out by asking the strategies again for what is under it.
 */
@Composable
fun TuneScoutNavigationContent(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(HomeRoute)
    val backStackController = remember { BackStackController(backStack = backStack) }
    val bottomSheetStrategy = remember { BottomSheetSceneStrategy<NavKey>(containerColor = { TuneScoutColors.sheet }) }
    val dialogStrategy = remember { DialogSceneStrategy<NavKey>() }
    val fadeStrategy = remember { FadeSceneDecoratorStrategy<NavKey>() }
    val tabsState = rememberHomeTabsState()
    val windowSize = rememberWindowSize()
    val isTwoPane = windowSize.isTwoPane
    val listDetailStrategy = remember(isTwoPane) {
        ListDetailSceneStrategy<NavKey>(
            isTwoPane = isTwoPane,
            emptyDetailPane = { NowPlayingScreen() },
            listPaneDecorator = { content ->
                MiniPlayerScaffold(isAllowed = backStack.isMiniPlayerInListPaneAllowed(isTwoPane = isTwoPane)) {
                    content()
                }
            },
        )
    }

    NavigationCommandCollector(backStackController = backStackController)

    SharedTransitionLayout(modifier = modifier) {
        CompositionLocalProvider(
            LocalSharedTransitionScope provides this,
            LocalTappedSharedArtworkSurface provides rememberTappedSharedArtworkSurface(),
            LocalSharedArtworkDestination provides rememberSharedArtworkDestination(),
            LocalHideableBarsState provides rememberHideableBarsState(),
        ) {
            TuneScoutNavigationSuiteScaffold(
                items = homeNavigationItems(tabsState = tabsState),
                isVisible = backStack.isHomeVisible(isTwoPane = isTwoPane),
                windowSize = windowSize,
            ) {
                MiniPlayerScaffold(isAllowed = backStack.isMiniPlayerAcrossWindowAllowed(isTwoPane = isTwoPane)) {
                    NavDisplay(
                        backStack = backStack,
                        onBack = backStackController::navigateBack,
                        entryDecorators = listOf(
                            rememberSaveableStateHolderNavEntryDecorator(),
                            rememberViewModelStoreNavEntryDecorator(),
                            rememberSharedElementNavEntryDecorator(),
                        ),
                        sceneStrategies = listOf(bottomSheetStrategy, dialogStrategy, listDetailStrategy),
                        sceneDecoratorStrategies = listOf(fadeStrategy),
                        transitionSpec = { createSceneFadeTransform() },
                        popTransitionSpec = { createSceneFadeTransform() },
                        predictivePopTransitionSpec = { createSceneFadeTransform() },
                        entryProvider = entryProvider {
                            homeEntry(tabsState = tabsState)
                            audioSearchEntry()
                            librarySearchEntry()
                            favoritesEntry()
                            favoritesOptionsEntry()
                            playlistEntry()
                            playlistOptionsEntry()
                            downloadedSongsEntry()
                            downloadedSongsOptionsEntry()
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
