package com.pierre.tunescout.core.navigation.reorder

import kotlinx.serialization.Serializable

/** A list of songs the user can put in an order of their own. */
@Serializable
sealed interface ReorderTarget {
    @Serializable
    data class Album(
        val albumId: Long,
    ) : ReorderTarget

    @Serializable
    data class Playlist(
        val playlistId: Long,
    ) : ReorderTarget
}
