package com.pierre.tunescout.feature.addtoplaylist.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.pierre.tunescout.core.navigation.route.AddToPlaylistRoute
import com.pierre.tunescout.core.navigation.scene.BottomSheetSceneStrategy
import com.pierre.tunescout.feature.addtoplaylist.presentation.content.AddToPlaylistScreen

fun EntryProviderScope<NavKey>.addToPlaylistEntry() {
    entry<AddToPlaylistRoute>(metadata = BottomSheetSceneStrategy.bottomSheet()) { route ->
        AddToPlaylistScreen(route = route)
    }
}
