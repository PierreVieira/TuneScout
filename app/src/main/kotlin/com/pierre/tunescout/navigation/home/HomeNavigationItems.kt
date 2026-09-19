package com.pierre.tunescout.navigation.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.pierre.tunescout.ui.component.TuneScoutNavigationItem

@Composable
internal fun homeNavigationItems(tabsState: HomeTabsState): List<TuneScoutNavigationItem> = HomeTab.entries.map { tab ->
    TuneScoutNavigationItem(
        icon = tab.icon,
        label = stringResource(tab.labelRes),
        isSelected = tabsState.selectedTab == tab,
        onClick = { tabsState.switchTo(tab) },
    )
}
