package com.pierre.tunescout.core.model

/**
 * An album without its tracks. The library lists the albums the user liked and never draws their
 * songs, so it asks for this rather than making Room read a track list it would throw away.
 *
 * @property id the id of the album.
 * @property title the name of the album.
 * @property artistName the artist the album is credited to.
 * @property artwork the cover of the album.
 */
data class AlbumSummary(
    val id: Long,
    val title: String,
    val artistName: String,
    val artwork: Artwork,
)
