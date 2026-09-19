package com.pierre.tunescout.core.database.relation

/**
 * One playlist joined with one of its songs, or with nulls when it has none yet. The rows arrive
 * ordered by the playlist and then by the song's explicit position, so grouping them keeps both
 * orders without a second query.
 */
internal data class PlaylistRow(
    val playlistId: Long,
    val name: String,
    val artworkUrl: String?,
)
