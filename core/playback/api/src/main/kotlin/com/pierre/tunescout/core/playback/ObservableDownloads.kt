package com.pierre.tunescout.core.playback

import com.pierre.tunescout.core.model.SongDownloadStatus
import kotlinx.coroutines.flow.Flow

/**
 * The songs on their way to the device or already on it, followed as the files come and go. Asking
 * to keep a song is not done here: that is a request saved with the library, which the player
 * follows on its own.
 */
fun interface ObservableDownloads {
    /** @return how far each song the user asked to keep has got, by song id; a song absent has no download. */
    fun observeDownloadStatuses(): Flow<Map<Long, SongDownloadStatus>>
}
