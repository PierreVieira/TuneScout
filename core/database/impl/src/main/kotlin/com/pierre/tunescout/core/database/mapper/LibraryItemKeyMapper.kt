package com.pierre.tunescout.core.database.mapper

import com.pierre.tunescout.core.model.LibraryItemKey

private const val FAVORITES_ITEM_ID = "favorites"
private const val PLAYLIST_ITEM_PREFIX = "playlist:"
private const val ALBUM_ITEM_PREFIX = "album:"
private const val DOWNLOADED_SONGS_ITEM_ID = "downloaded_songs"

internal fun LibraryItemKey.toItemId(): String = when (this) {
    LibraryItemKey.Favorites -> FAVORITES_ITEM_ID
    is LibraryItemKey.Playlist -> "$PLAYLIST_ITEM_PREFIX$playlistId"
    is LibraryItemKey.Album -> "$ALBUM_ITEM_PREFIX$albumId"
    LibraryItemKey.DownloadedSongs -> DOWNLOADED_SONGS_ITEM_ID
}

internal fun String.toLibraryItemKeyOrNull(): LibraryItemKey? = when {
    this == FAVORITES_ITEM_ID -> LibraryItemKey.Favorites

    this == DOWNLOADED_SONGS_ITEM_ID -> LibraryItemKey.DownloadedSongs

    startsWith(PLAYLIST_ITEM_PREFIX) ->
        removePrefix(PLAYLIST_ITEM_PREFIX).toLongOrNull()?.let(LibraryItemKey::Playlist)

    startsWith(ALBUM_ITEM_PREFIX) ->
        removePrefix(ALBUM_ITEM_PREFIX).toLongOrNull()?.let(LibraryItemKey::Album)

    else -> null
}
