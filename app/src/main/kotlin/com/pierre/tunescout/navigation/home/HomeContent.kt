package com.pierre.tunescout.navigation.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.pierre.tunescout.feature.library.presentation.navigation.libraryEntry
import com.pierre.tunescout.feature.songs.presentation.navigation.songsEntry
import com.pierre.tunescout.ui.component.createCrossFadeTransition

/**
 * The tab host lives in `app` because it composes two features, and a feature may never depend on
 * another one. Only the tabs themselves are rendered here: everything reached from a tab — the
 * player, an album, a playlist, a sheet — is pushed onto the root back stack and covers the bar.
 */
@Composable
internal fun HomeContent(
    tabsState: HomeTabsState,
    modifier: Modifier = Modifier,
) {
    NavDisplay(
        entries = tabsState.toDecoratedEntries(
            entryProvider {
                songsEntry()
                libraryEntry()
            },
        ),
        modifier = modifier.fillMaxSize(),
        transitionSpec = { createCrossFadeTransition() },
        popTransitionSpec = { createCrossFadeTransition() },
        predictivePopTransitionSpec = { createCrossFadeTransition() },
        onBack = tabsState::navigateBack,
    )
}
