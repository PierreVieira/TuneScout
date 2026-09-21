package com.pierre.tunescout.navigation.home

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.pierre.tunescout.core.navigation.route.HomeRoute
import com.pierre.tunescout.core.navigation.scene.ListDetailSceneStrategy

internal fun EntryProviderScope<NavKey>.homeEntry(tabsState: HomeTabsState) {
    entry<HomeRoute>(metadata = ListDetailSceneStrategy.listPane()) {
        HomeContent(tabsState = tabsState)
    }
}
