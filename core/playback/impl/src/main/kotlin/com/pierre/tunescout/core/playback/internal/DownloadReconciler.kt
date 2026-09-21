package com.pierre.tunescout.core.playback.internal

import com.pierre.tunescout.core.database.DownloadLocalDataSource
import com.pierre.tunescout.core.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Keeps the files on the device matching what the user asked to keep. The requests are saved with
 * the library and say nothing about files; every time the songs they add up to change, this fetches
 * the ones missing and drops the ones nothing holds any more. That is how a song added to a
 * downloaded playlist gets downloaded, and how one liked song taken out of the downloaded liked
 * songs is deleted, without either screen knowing about files.
 *
 * @property downloadLocalDataSource the songs the user asked to keep.
 * @property tracker the downloads the player already has.
 * @property downloadCommands where a song is fetched or dropped.
 */
internal class DownloadReconciler(
    private val downloadLocalDataSource: DownloadLocalDataSource,
    private val tracker: TrackedDownloadStates,
    private val downloadCommands: DownloadCommands,
) {
    /**
     * The songs already handed to [downloadCommands] in this run. The tracker learns about a new
     * download a moment after it is added, and a second change arriving in between must not add it
     * again: adding a finished download over starts it anew.
     */
    private val handedOverIds = mutableSetOf<Long>()

    fun start(scope: CoroutineScope) {
        scope.launch {
            downloadLocalDataSource.observeWantedSongs().collect { wanted ->
                reconcile(wanted = wanted, tracked = tracker.awaitTrackedStates())
            }
        }
    }

    /** A download that failed is asked for again, since the change that brought us here may have been the fix. */
    private fun reconcile(
        wanted: List<Song>,
        tracked: Map<Long, TrackedDownloadState>,
    ) {
        val failedIds = tracked.filterValues { state -> state == TrackedDownloadState.Failed }.keys
        val presentIds = tracked.keys + handedOverIds - failedIds
        val wantedIds = wanted.mapTo(mutableSetOf(), Song::id)
        wanted.filterNot { song -> song.id in presentIds }.forEach { song ->
            downloadCommands.add(song)
            handedOverIds += song.id
        }
        (tracked.keys + handedOverIds).filterNot { songId -> songId in wantedIds }.forEach { songId ->
            downloadCommands.remove(songId)
            handedOverIds -= songId
        }
    }
}
