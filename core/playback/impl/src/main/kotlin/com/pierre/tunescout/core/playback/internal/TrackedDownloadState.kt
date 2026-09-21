package com.pierre.tunescout.core.playback.internal

import com.pierre.tunescout.core.model.SongDownloadStatus

/** Where a download the player manages stands, with Media3's states folded into the three that matter here. */
internal enum class TrackedDownloadState {
    /** Waiting for its turn or for a connection, or being fetched. */
    InProgress,

    Completed,

    /** Gave up, and is asked for again the next time the wanted songs change or the app starts. */
    Failed,
    ;

    /** @return what a list shows for the song, or null for a download that brought nothing. */
    fun toSongDownloadStatusOrNull(): SongDownloadStatus? = when (this) {
        InProgress -> SongDownloadStatus.Downloading
        Completed -> SongDownloadStatus.Downloaded
        Failed -> null
    }
}
