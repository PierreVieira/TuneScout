package com.pierre.tunescout.feature.library.domain.model

/**
 * What a collection screen shows. A liked album is a library item too, but it opens the album
 * screen rather than a list of its own, so it is not one of these.
 */
sealed interface CollectionKey {
    data object Favorites : CollectionKey

    data class Playlist(
        val playlistId: Long,
    ) : CollectionKey

    /** The songs the user downloaded one by one, rather than with an album or a playlist. */
    data object DownloadedSongs : CollectionKey
}
