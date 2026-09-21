package com.pierre.tunescout.core.model

/**
 * What the queue was built on: the ordered songs a tap started from, which shuffle reorders and
 * the songs queued by hand play ahead of.
 */
sealed interface PlaybackContext {
    data object SingleSong : PlaybackContext

    data class Album(
        val id: Long,
        val title: String,
    ) : PlaybackContext

    /**
     * @property id the playlist.
     * @property title its name when it started, which is what the queue goes on calling it.
     */
    data class Playlist(
        val id: Long,
        val title: String,
    ) : PlaybackContext

    data object LikedSongs : PlaybackContext

    /** The songs played last, as the widget's shortcuts offer them. */
    data object RecentlyPlayed : PlaybackContext
}
