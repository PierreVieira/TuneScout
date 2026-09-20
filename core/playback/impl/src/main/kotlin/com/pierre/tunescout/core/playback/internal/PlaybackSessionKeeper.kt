package com.pierre.tunescout.core.playback.internal

import android.util.Log
import com.pierre.tunescout.core.database.PlaybackSessionLocalDataSource
import com.pierre.tunescout.core.model.PlaybackSession
import com.pierre.tunescout.core.model.PlaybackState
import com.pierre.tunescout.core.playback.ObservablePlayback
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch
import kotlin.time.Duration

internal class PlaybackSessionKeeper(
    private val observablePlayback: ObservablePlayback,
    private val restorablePlayback: RestorablePlayback,
    private val playbackSessionLocalDataSource: PlaybackSessionLocalDataSource,
    private val saveInterval: Duration,
) {
    fun start(scope: CoroutineScope) {
        scope.launch {
            restore()
            observablePlayback
                .observePlaybackState()
                .distinctUntilChangedBy(::toCheckpoint)
                .collect(::save)
        }
    }

    private suspend fun restore() {
        try {
            playbackSessionLocalDataSource.find()?.let(restorablePlayback::restore)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Log.e(TAG, "Could not restore the playback session: ${exception.message}")
        }
    }

    private suspend fun save(state: PlaybackState) {
        try {
            playbackSessionLocalDataSource.save(
                PlaybackSession(
                    entries = state.entries,
                    currentEntryId = state.currentEntry?.id,
                    context = state.context,
                    position = state.position,
                    isRepeatEnabled = state.isRepeatEnabled,
                    hasEnded = state.hasEnded,
                ),
            )
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Log.e(TAG, "Could not save the playback session: ${exception.message}")
        }
    }

    private fun toCheckpoint(state: PlaybackState): Checkpoint = Checkpoint(
        entryIds = state.entries.map { entry -> entry.id },
        currentEntryId = state.currentEntry?.id,
        isPlaying = state.isPlaying,
        hasEnded = state.hasEnded,
        isRepeatEnabled = state.isRepeatEnabled,
        positionBucket = state.position.inWholeMilliseconds / saveInterval.inWholeMilliseconds,
    )

    private data class Checkpoint(
        val entryIds: List<String>,
        val currentEntryId: String?,
        val isPlaying: Boolean,
        val hasEnded: Boolean,
        val isRepeatEnabled: Boolean,
        val positionBucket: Long,
    )

    private companion object {
        const val TAG = "PlaybackSessionKeeper"
    }
}
