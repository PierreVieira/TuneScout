package com.pierre.tunescout.feature.library.presentation.mapper

import com.pierre.tunescout.core.model.LibraryItemKey
import com.pierre.tunescout.feature.library.domain.model.CollectionKey

fun CollectionKey.toLibraryItemKey(): LibraryItemKey = when (this) {
    CollectionKey.Favorites -> LibraryItemKey.Favorites
    is CollectionKey.Playlist -> LibraryItemKey.Playlist(playlistId = playlistId)
    CollectionKey.DownloadedSongs -> LibraryItemKey.DownloadedSongs
}
