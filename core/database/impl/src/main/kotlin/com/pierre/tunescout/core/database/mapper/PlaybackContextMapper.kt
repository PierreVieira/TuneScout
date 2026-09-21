package com.pierre.tunescout.core.database.mapper

import com.pierre.tunescout.core.database.entity.PlaybackSessionEntity
import com.pierre.tunescout.core.model.PlaybackContext

private const val SINGLE_SONG = "SingleSong"
private const val ALBUM = "Album"
private const val PLAYLIST = "Playlist"
private const val LIKED_SONGS = "LikedSongs"
private const val RECENTLY_PLAYED = "RecentlyPlayed"

/**
 * How a [PlaybackContext] is spread over the session's context columns.
 *
 * @property type which kind of context it is, by name.
 * @property id the album or playlist, for the kinds that have one.
 * @property title the album's title or the playlist's name.
 */
internal data class PlaybackContextColumns(
    val type: String,
    val id: Long? = null,
    val title: String? = null,
)

internal fun PlaybackContext?.toColumns(): PlaybackContextColumns = when (this) {
    null, PlaybackContext.SingleSong -> PlaybackContextColumns(type = SINGLE_SONG)
    is PlaybackContext.Album -> PlaybackContextColumns(type = ALBUM, id = id, title = title)
    is PlaybackContext.Playlist -> PlaybackContextColumns(type = PLAYLIST, id = id, title = title)
    PlaybackContext.LikedSongs -> PlaybackContextColumns(type = LIKED_SONGS)
    PlaybackContext.RecentlyPlayed -> PlaybackContextColumns(type = RECENTLY_PLAYED)
}

/**
 * @return the context saved in these columns. A kind this version does not know, or one missing the
 * id it needs, comes back as a single song.
 */
internal fun PlaybackSessionEntity.toPlaybackContext(): PlaybackContext = when (contextType) {
    ALBUM -> contextId?.let { id -> PlaybackContext.Album(id = id, title = contextTitle.orEmpty()) }
    PLAYLIST -> contextId?.let { id -> PlaybackContext.Playlist(id = id, title = contextTitle.orEmpty()) }
    LIKED_SONGS -> PlaybackContext.LikedSongs
    RECENTLY_PLAYED -> PlaybackContext.RecentlyPlayed
    else -> null
} ?: PlaybackContext.SingleSong
