package com.pierre.tunescout.feature.player.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.pierre.tunescout.core.navigation.route.PlayerRoute
import com.pierre.tunescout.feature.player.presentation.content.PlayerScreen

fun EntryProviderScope<NavKey>.playerEntry() {
    entry<PlayerRoute> { route ->
        PlayerScreen(route = route)
    }
}
