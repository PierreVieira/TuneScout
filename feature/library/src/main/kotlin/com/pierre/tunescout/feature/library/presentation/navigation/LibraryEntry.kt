package com.pierre.tunescout.feature.library.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.pierre.tunescout.core.navigation.route.CreatePlaylistRoute
import com.pierre.tunescout.core.navigation.route.DownloadedSongsOptionsRoute
import com.pierre.tunescout.core.navigation.route.DownloadedSongsRoute
import com.pierre.tunescout.core.navigation.route.FavoritesOptionsRoute
import com.pierre.tunescout.core.navigation.route.FavoritesRoute
import com.pierre.tunescout.core.navigation.route.LibraryRoute
import com.pierre.tunescout.core.navigation.route.LibrarySearchRoute
import com.pierre.tunescout.core.navigation.route.PlaylistOptionsRoute
import com.pierre.tunescout.core.navigation.route.PlaylistRoute
import com.pierre.tunescout.core.navigation.scene.BottomSheetSceneStrategy
import com.pierre.tunescout.core.navigation.scene.ListDetailSceneStrategy
import com.pierre.tunescout.feature.library.domain.model.CollectionKey
import com.pierre.tunescout.feature.library.presentation.content.CollectionOptionsScreen
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
    entry<FavoritesRoute>(metadata = ListDetailSceneStrategy.detailPane()) {
        CollectionScreen(key = CollectionKey.Favorites)
    }
}

fun EntryProviderScope<NavKey>.playlistEntry() {
    entry<PlaylistRoute>(metadata = ListDetailSceneStrategy.detailPane()) { route ->
        CollectionScreen(key = CollectionKey.Playlist(playlistId = route.playlistId))
    }
}

fun EntryProviderScope<NavKey>.downloadedSongsEntry() {
    entry<DownloadedSongsRoute>(metadata = ListDetailSceneStrategy.detailPane()) {
        CollectionScreen(key = CollectionKey.DownloadedSongs)
    }
}

fun EntryProviderScope<NavKey>.favoritesOptionsEntry() {
    entry<FavoritesOptionsRoute>(metadata = BottomSheetSceneStrategy.bottomSheet()) {
        CollectionOptionsScreen(key = CollectionKey.Favorites)
    }
}

fun EntryProviderScope<NavKey>.playlistOptionsEntry() {
    entry<PlaylistOptionsRoute>(metadata = BottomSheetSceneStrategy.bottomSheet()) { route ->
        CollectionOptionsScreen(key = CollectionKey.Playlist(playlistId = route.playlistId))
    }
}

fun EntryProviderScope<NavKey>.downloadedSongsOptionsEntry() {
    entry<DownloadedSongsOptionsRoute>(metadata = BottomSheetSceneStrategy.bottomSheet()) {
        CollectionOptionsScreen(key = CollectionKey.DownloadedSongs)
    }
}

fun EntryProviderScope<NavKey>.createPlaylistEntry() {
    entry<CreatePlaylistRoute>(metadata = DialogSceneStrategy.dialog()) {
        CreatePlaylistScreen()
    }
}
