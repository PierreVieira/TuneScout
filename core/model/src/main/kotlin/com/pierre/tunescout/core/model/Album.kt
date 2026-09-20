package com.pierre.tunescout.core.model

/**
 * An album and its tracks.
 *
 * @property id the iTunes collection id.
 * @property title the album's name.
 * @property artistName the artist the album is credited to.
 * @property artwork the album cover.
 * @property songs the tracks, in track order.
 * @property isComplete whether [songs] is the whole track list. An album the device never looked up
 * is still reachable offline through the tracks it saved on their own — played, liked, queued — and
 * is put together from those, so it holds only them.
 */
data class Album(
    val id: Long,
    val title: String,
    val artistName: String,
    val artwork: Artwork,
    val songs: List<Song>,
    val isComplete: Boolean,
)
