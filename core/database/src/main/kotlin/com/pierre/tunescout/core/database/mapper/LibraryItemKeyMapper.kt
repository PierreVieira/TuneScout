package com.pierre.tunescout.core.database.mapper

import com.pierre.tunescout.core.model.LibraryItemKey

private const val FAVORITES_ITEM_ID = "favorites"
private const val PLAYLIST_ITEM_PREFIX = "playlist:"

internal fun LibraryItemKey.toItemId(): String = when (this) {
    LibraryItemKey.Favorites -> FAVORITES_ITEM_ID
    is LibraryItemKey.Playlist -> "$PLAYLIST_ITEM_PREFIX$playlistId"
}

internal fun String.toLibraryItemKeyOrNull(): LibraryItemKey? = when {
    this == FAVORITES_ITEM_ID -> LibraryItemKey.Favorites

    startsWith(PLAYLIST_ITEM_PREFIX) ->
        removePrefix(PLAYLIST_ITEM_PREFIX).toLongOrNull()?.let(LibraryItemKey::Playlist)

    else -> null
}
