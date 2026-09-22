package com.pierre.tunescout.feature.library.presentation.mapper

import androidx.navigation3.runtime.NavKey
import com.pierre.tunescout.core.model.LibraryItemKey
import com.pierre.tunescout.core.navigation.route.AlbumRoute
import com.pierre.tunescout.core.navigation.route.DownloadedSongsRoute
import com.pierre.tunescout.core.navigation.route.FavoritesRoute
import com.pierre.tunescout.core.navigation.route.PlaylistRoute

fun LibraryItemKey.toRoute(): NavKey = when (this) {
    LibraryItemKey.Favorites -> FavoritesRoute
    is LibraryItemKey.Playlist -> PlaylistRoute(playlistId = playlistId)
    is LibraryItemKey.Album -> AlbumRoute(albumId = albumId)
    LibraryItemKey.DownloadedSongs -> DownloadedSongsRoute
}
