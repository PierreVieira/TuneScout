package com.pierre.tunescout.core.model

/**
 * Where a collection the user can download — an album, a playlist, the liked songs — stands. It is
 * downloaded because the user asked for the collection, not because each of its songs happens to be
 * on the device: a song downloaded on its own does not turn its album's button on.
 */
sealed interface CollectionDownloadState {
    /**
     * How much of the collection is on the device: null when nobody asked for it, and 1 only once
     * every song has arrived.
     */
    val progress: Float?
        get() = when (this) {
            NotDownloaded -> null
            is Downloading -> downloadedCount.toFloat() / totalCount
            Downloaded -> 1f
        }

    data object NotDownloaded : CollectionDownloadState

    /**
     * @property downloadedCount how many of the collection's songs are already on the device.
     * @property totalCount how many songs the collection holds.
     */
    data class Downloading(
        val downloadedCount: Int,
        val totalCount: Int,
    ) : CollectionDownloadState

    data object Downloaded : CollectionDownloadState

    companion object {
        /**
         * @return [NotDownloaded] unless [isRequested], and then [Downloaded] once every one of
         * [songIds] is in [statuses] as [SongDownloadStatus.Downloaded] — which an empty
         * collection already is.
         */
        fun of(
            isRequested: Boolean,
            songIds: List<Long>,
            statuses: Map<Long, SongDownloadStatus>,
        ): CollectionDownloadState {
            if (!isRequested) return NotDownloaded
            val downloadedCount = songIds.count { songId -> statuses[songId] == SongDownloadStatus.Downloaded }
            return if (downloadedCount == songIds.size) {
                Downloaded
            } else {
                Downloading(downloadedCount = downloadedCount, totalCount = songIds.size)
            }
        }
    }
}
