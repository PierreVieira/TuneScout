package com.pierre.tunescout.feature.library.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.pierre.tunescout.core.model.LibraryItemKey
import com.pierre.tunescout.core.navigation.route.CreatePlaylistRoute
import com.pierre.tunescout.core.navigation.route.FavoritesRoute
import com.pierre.tunescout.core.navigation.route.LibraryRoute
import com.pierre.tunescout.core.navigation.route.LibrarySearchRoute
import com.pierre.tunescout.core.navigation.route.PlaylistRoute
import com.pierre.tunescout.feature.library.presentation.content.CollectionScreen
import com.pierre.tunescout.feature.library.presentation.content.CreatePlaylistScreen
import com.pierre.tunescout.feature.library.presentation.content.LibraryScreen
import com.pierre.tunescout.feature.library.presentation.content.LibrarySearchScreen

fun EntryProviderScope<NavKey>.libraryEntry() {
    entry<LibraryRoute> {
        LibraryScreen()
    }
}

fun EntryProviderScope<NavKey>.librarySearchEntry() {
    entry<LibrarySearchRoute> {
        LibrarySearchScreen()
    }
}

fun EntryProviderScope<NavKey>.favoritesEntry() {
    entry<FavoritesRoute> {
        CollectionScreen(key = LibraryItemKey.Favorites)
    }
}

fun EntryProviderScope<NavKey>.playlistEntry() {
    entry<PlaylistRoute> { route ->
        CollectionScreen(key = LibraryItemKey.Playlist(playlistId = route.playlistId))
    }
}

fun EntryProviderScope<NavKey>.createPlaylistEntry() {
    entry<CreatePlaylistRoute>(metadata = DialogSceneStrategy.dialog()) {
        CreatePlaylistScreen()
    }
}
