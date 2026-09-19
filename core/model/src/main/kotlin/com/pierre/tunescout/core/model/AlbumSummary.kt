package com.pierre.tunescout.core.model

/**
 * An album without its tracks. The library lists the albums the user liked and never draws their
 * songs, so it asks for this rather than making Room read a track list it would throw away.
 */
data class AlbumSummary(
    val id: Long,
    val title: String,
    val artistName: String,
    val artwork: Artwork,
)
