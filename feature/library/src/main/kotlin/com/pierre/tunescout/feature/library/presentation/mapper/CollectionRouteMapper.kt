package com.pierre.tunescout.feature.library.presentation.mapper

import androidx.navigation3.runtime.NavKey
import com.pierre.tunescout.core.navigation.route.DownloadedSongsOptionsRoute
import com.pierre.tunescout.core.navigation.route.FavoritesOptionsRoute
import com.pierre.tunescout.core.navigation.route.PlaylistOptionsRoute
import com.pierre.tunescout.feature.library.domain.model.CollectionKey

fun CollectionKey.toOptionsRoute(): NavKey = when (this) {
    CollectionKey.Favorites -> FavoritesOptionsRoute
    is CollectionKey.Playlist -> PlaylistOptionsRoute(playlistId = playlistId)
    CollectionKey.DownloadedSongs -> DownloadedSongsOptionsRoute
}
