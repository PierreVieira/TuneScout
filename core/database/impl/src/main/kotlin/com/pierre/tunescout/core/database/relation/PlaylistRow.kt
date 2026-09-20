package com.pierre.tunescout.core.database.relation

/**
 * One playlist joined with one of its songs, or with nulls when it has none yet. The rows arrive
 * ordered by the playlist and then by the song's explicit position, so grouping them keeps both
 * orders without a second query.
 *
 * @property playlistId the id of the playlist the row belongs to.
 * @property name the name of that playlist.
 * @property artworkUrl the artwork of the joined song, or null when the playlist has none.
 */
internal data class PlaylistRow(
    val playlistId: Long,
    val name: String,
    val artworkUrl: String?,
)
