package com.pierre.tunescout.feature.queue.presentation.mapper

import com.pierre.tunescout.core.model.PlaybackContext
import com.pierre.tunescout.feature.queue.presentation.model.QueueContextTitle

/** @return what the queue says it plays from, or null for a single song, which is not something to play from. */
internal fun PlaybackContext?.toQueueContextTitle(): QueueContextTitle? = when (this) {
    is PlaybackContext.Album -> QueueContextTitle.Custom(title)
    is PlaybackContext.Playlist -> QueueContextTitle.Custom(title)
    PlaybackContext.LikedSongs -> QueueContextTitle.LikedSongs
    PlaybackContext.DownloadedSongs -> QueueContextTitle.DownloadedSongs
    PlaybackContext.RecentlyPlayed -> QueueContextTitle.RecentlyPlayed
    PlaybackContext.SingleSong, null -> null
}
