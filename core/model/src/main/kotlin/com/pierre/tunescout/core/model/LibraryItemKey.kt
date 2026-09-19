package com.pierre.tunescout.core.model

/**
 * What a row of the library points at. The favourites list is not a playlist row, so a key is the
 * one thing a recent search, a navigation argument or a selection can carry for either of them.
 */
sealed interface LibraryItemKey {
    data object Favorites : LibraryItemKey

    data class Playlist(
        val playlistId: Long,
    ) : LibraryItemKey

    data class Album(
        val albumId: Long,
    ) : LibraryItemKey
}
