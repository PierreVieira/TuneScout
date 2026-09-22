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

    /**
     * The songs the user downloaded one by one, rather than with an album or a playlist. It is no
     * collection of its own: each song in it is there by its own request.
     */
    data object DownloadedSongs : LibraryItemKey
}
