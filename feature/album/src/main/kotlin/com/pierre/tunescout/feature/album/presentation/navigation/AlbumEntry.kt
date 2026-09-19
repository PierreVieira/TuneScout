package com.pierre.tunescout.feature.album.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.pierre.tunescout.core.navigation.route.AlbumOptionsRoute
import com.pierre.tunescout.core.navigation.route.AlbumRoute
import com.pierre.tunescout.core.navigation.scene.BottomSheetSceneStrategy
import com.pierre.tunescout.feature.album.presentation.content.AlbumOptionsScreen
import com.pierre.tunescout.feature.album.presentation.content.AlbumScreen

fun EntryProviderScope<NavKey>.albumEntry() {
    entry<AlbumRoute> { route ->
        AlbumScreen(route = route)
    }
}

fun EntryProviderScope<NavKey>.albumOptionsEntry() {
    entry<AlbumOptionsRoute>(metadata = BottomSheetSceneStrategy.bottomSheet()) { route ->
        AlbumOptionsScreen(route = route)
    }
}
