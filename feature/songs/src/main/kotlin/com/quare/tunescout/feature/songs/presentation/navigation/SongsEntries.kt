package com.quare.tunescout.feature.songs.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.quare.tunescout.core.navigation.route.SongOptionsRoute
import com.quare.tunescout.core.navigation.route.SongsRoute
import com.quare.tunescout.core.navigation.scene.BottomSheetSceneStrategy
import com.quare.tunescout.feature.songs.presentation.content.SongOptionsScreen
import com.quare.tunescout.feature.songs.presentation.content.SongsScreen

fun EntryProviderScope<NavKey>.songsEntries() {
    entry<SongsRoute> {
        SongsScreen()
    }
    entry<SongOptionsRoute>(metadata = BottomSheetSceneStrategy.bottomSheet()) { route ->
        SongOptionsScreen(route = route)
    }
}
