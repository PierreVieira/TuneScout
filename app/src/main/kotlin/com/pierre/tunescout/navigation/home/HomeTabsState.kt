package com.pierre.tunescout.navigation.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator

/**
 * The state behind the tab host: one back stack per tab, and which one is on screen.
 *
 * The entries of the first tab are kept in the list while another tab is selected, so the nested
 * display has something to animate back to and the system back gesture lands on the first tab —
 * the behaviour Android asks of a tabbed app.
 */
@Stable
internal class HomeTabsState(
    private val backStacks: Map<HomeTab, NavBackStack<NavKey>>,
    private val selectedIndexState: MutableIntState,
) {
    private var selectedIndex by selectedIndexState
    private val startTab = HomeTab.entries.first()

    val selectedTab: HomeTab
        get() = HomeTab.entries[selectedIndex]

    fun switchTo(tab: HomeTab) {
        selectedIndex = tab.ordinal
    }

    fun navigateBack() {
        selectedIndex = startTab.ordinal
    }

    @Composable
    fun toDecoratedEntries(entryProvider: (NavKey) -> NavEntry<NavKey>): List<NavEntry<NavKey>> {
        val decoratedEntries = backStacks.mapValues { (_, backStack) ->
            rememberDecoratedNavEntries(
                backStack = backStack,
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
                entryProvider = entryProvider,
            )
        }
        val tabsInUse = if (selectedTab == startTab) listOf(startTab) else listOf(startTab, selectedTab)
        return tabsInUse.flatMap { tab -> decoratedEntries.getValue(tab) }
    }
}

@Composable
internal fun rememberHomeTabsState(): HomeTabsState {
    val backStacks = HomeTab.entries.associateWith { tab -> rememberNavBackStack(tab.route) }
    val selectedIndexState = rememberSaveable { mutableIntStateOf(0) }
    return remember { HomeTabsState(backStacks = backStacks, selectedIndexState = selectedIndexState) }
}
