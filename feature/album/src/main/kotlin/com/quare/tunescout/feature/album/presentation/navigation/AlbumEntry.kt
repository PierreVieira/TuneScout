package com.quare.tunescout.feature.album.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.quare.tunescout.core.navigation.route.AlbumRoute
import com.quare.tunescout.feature.album.presentation.content.AlbumScreen

fun EntryProviderScope<NavKey>.albumEntry() {
    entry<AlbumRoute> { route ->
        AlbumScreen(route = route)
    }
}
