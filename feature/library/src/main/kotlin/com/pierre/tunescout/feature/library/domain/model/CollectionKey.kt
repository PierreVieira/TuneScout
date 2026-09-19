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
}
