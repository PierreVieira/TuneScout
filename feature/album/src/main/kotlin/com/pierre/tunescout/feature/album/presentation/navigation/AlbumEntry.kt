package com.pierre.tunescout.feature.album.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.pierre.tunescout.core.navigation.route.AlbumRoute
import com.pierre.tunescout.feature.album.presentation.content.AlbumScreen

fun EntryProviderScope<NavKey>.albumEntry() {
    entry<AlbumRoute> { route ->
        AlbumScreen(route = route)
    }
}
