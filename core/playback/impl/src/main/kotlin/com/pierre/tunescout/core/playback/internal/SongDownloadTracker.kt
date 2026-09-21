package com.pierre.tunescout.core.playback.internal

import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import com.pierre.tunescout.core.model.SongDownloadStatus
import com.pierre.tunescout.core.playback.ObservableDownloads
import com.pierre.tunescout.core.utils.DispatcherProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Every download [downloadManager] holds, by song. The manager only reports changes, and keeps the
 * finished downloads in an index it never lists by itself, so the index is read once at [start] and
 * the changes are applied on top of it.
 *
 * @property downloadManager the downloads, and where their changes come from.
 * @property dispatcherProvider where the index is read, off the main thread.
 */
@OptIn(UnstableApi::class)
internal class SongDownloadTracker(
    private val downloadManager: DownloadManager,
    private val dispatcherProvider: DispatcherProvider,
) : ObservableDownloads,
    TrackedDownloadStates {
    private val tracked = MutableStateFlow(TrackedDownloads(states = emptyMap(), isLoaded = false))

    private val listener = object : DownloadManager.Listener {
        override fun onDownloadChanged(
            downloadManager: DownloadManager,
            download: Download,
            finalException: Exception?,
        ) {
            track(download)
        }

        override fun onDownloadRemoved(
            downloadManager: DownloadManager,
            download: Download,
        ) {
            val songId = download.request.id.toLongOrNull() ?: return
            tracked.update { current -> current.copy(states = current.states - songId) }
        }
    }

    /**
     * Changes can arrive while the index is still being read: those win over what the index says,
     * since they are newer.
     */
    fun start(scope: CoroutineScope) {
        downloadManager.addListener(listener)
        scope.launch {
            val stored = withContext(dispatcherProvider.io) { loadStoredStatesOrEmpty() }
            tracked.update { current -> TrackedDownloads(states = stored + current.states, isLoaded = true) }
        }
    }

    /**
     * An index that cannot be read leaves every screen waiting on [observeDownloadStatuses], so it
     * counts as empty instead: the songs then show as not downloaded, and the reconciler asks for
     * them again.
     *
     * @return every stored download by song, or none when the index cannot be read.
     */
    private fun loadStoredStatesOrEmpty(): Map<Long, TrackedDownloadState> = try {
        loadStoredStates()
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        Log.e(TAG, "Could not read the downloads: ${exception.message}")
        emptyMap()
    }

    override suspend fun awaitTrackedStates(): Map<Long, TrackedDownloadState> =
        tracked.filter { downloads -> downloads.isLoaded }.first().states

    override fun observeDownloadStatuses(): Flow<Map<Long, SongDownloadStatus>> = tracked
        .filter { downloads -> downloads.isLoaded }
        .map { downloads ->
            buildMap {
                downloads.states.forEach { (songId, state) ->
                    state.toSongDownloadStatusOrNull()?.let { status -> put(songId, status) }
                }
            }
        }.distinctUntilChanged()

    private fun track(download: Download) {
        val songId = download.request.id.toLongOrNull() ?: return
        val state = download.toTrackedStateOrNull()
        tracked.update { current ->
            val states = if (state == null) current.states - songId else current.states + (songId to state)
            current.copy(states = states)
        }
    }

    private fun loadStoredStates(): Map<Long, TrackedDownloadState> = buildMap {
        downloadManager.downloadIndex.getDownloads().use { cursor ->
            while (cursor.moveToNext()) {
                val download = cursor.download
                val songId = download.request.id.toLongOrNull() ?: continue
                download.toTrackedStateOrNull()?.let { state -> put(songId, state) }
            }
        }
    }

    /** @return the state [Download.state] folds into, or null for one on its way out. */
    private fun Download.toTrackedStateOrNull(): TrackedDownloadState? = when (state) {
        Download.STATE_COMPLETED -> TrackedDownloadState.Completed
        Download.STATE_FAILED -> TrackedDownloadState.Failed
        Download.STATE_REMOVING -> null
        else -> TrackedDownloadState.InProgress
    }

    private companion object {
        const val TAG = "SongDownloadTracker"
    }

    /**
     * @property states every download by song.
     * @property isLoaded whether the index has been read, before which [states] only holds the
     * changes reported since [start].
     */
    private data class TrackedDownloads(
        val states: Map<Long, TrackedDownloadState>,
        val isLoaded: Boolean,
    )
}
