package com.pierre.tunescout.core.playback.internal

import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.pierre.tunescout.core.model.NO_QUEUE_INDEX
import com.pierre.tunescout.core.model.PlaybackContext
import com.pierre.tunescout.core.model.PlaybackSession
import com.pierre.tunescout.core.model.PlaybackState
import com.pierre.tunescout.core.model.PlaybackStatus
import com.pierre.tunescout.core.model.QueueEntry
import com.pierre.tunescout.core.model.QueueSource
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.playback.PlaybackController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private val positionTick = 250.milliseconds

internal class ExoPlayerPlaybackController(
    private val player: ExoPlayer,
    private val serviceLauncher: PlaybackServiceLauncher,
    private val mediaItemFactory: MediaItemFactory,
    private val scope: CoroutineScope,
) : PlaybackController,
    RestorablePlayback {
    override val state: StateFlow<PlaybackState>
        field = MutableStateFlow(PlaybackState.Idle)

    private var entries: List<QueueEntry> = emptyList()
    private var context: PlaybackContext? = null
    private var positionTicker: Job? = null

    private val currentIndex: Int
        get() = if (player.mediaItemCount == 0) NO_QUEUE_INDEX else player.currentMediaItemIndex

    init {
        player.addListener(PlayerListener())
    }

    override fun play(
        song: Song,
        songs: List<Song>,
        context: PlaybackContext,
    ) {
        val timeline = buildTimeline(
            songs = songs.ifEmpty { listOf(song) },
            startSongId = song.id,
            carriedEntries = getCarriedEntries(entries, currentIndex),
            createEntryId = ::createEntryId,
        )
        entries = timeline.entries
        this.context = context
        player.setMediaItems(entries.map(mediaItemFactory::createMediaItem), timeline.startIndex, 0L)
        startPlaying()
    }

    override fun restore(session: PlaybackSession) {
        if (session.entries.isEmpty()) return
        entries = session.entries
        context = session.context
        val startIndex = entries
            .indexOfFirst { entry -> entry.id == session.currentEntryId }
            .coerceAtLeast(0)
        player.setMediaItems(
            entries.map(mediaItemFactory::createMediaItem),
            startIndex,
            session.position.inWholeMilliseconds,
        )
        player.repeatMode = if (session.isRepeatEnabled) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
        player.prepare()
        publish()
    }

    override fun queueNext(songs: List<Song>) {
        insert(songs, currentIndex + 1)
    }

    override fun addToQueue(songs: List<Song>) {
        insert(songs, getUserQueueInsertIndex(entries, currentIndex))
    }

    override fun removeFromQueue(entryId: String) {
        val index = entries.indexOfFirst { entry -> entry.id == entryId }
        if (index < 0) return
        entries = entries.filterIndexed { position, _ -> position != index }
        player.removeMediaItem(index)
        publish()
    }

    override fun moveInQueue(
        fromIndex: Int,
        toIndex: Int,
    ) {
        if (fromIndex !in entries.indices || toIndex !in entries.indices || fromIndex == toIndex) return
        entries = entries.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
        player.moveMediaItem(fromIndex, toIndex)
        publish()
    }

    override fun skipTo(entryId: String) {
        val index = entries.indexOfFirst { entry -> entry.id == entryId }
        if (index < 0) return
        player.seekTo(index, 0L)
        resume()
    }

    override fun togglePlayPause() {
        if (player.isPlaying) player.pause() else resume()
    }

    override fun seekTo(position: Duration) {
        player.seekTo(position.inWholeMilliseconds)
        publish()
    }

    override fun skipToNext() {
        if (player.hasNextMediaItem()) player.seekToNextMediaItem() else player.seekTo(0L)
    }

    override fun skipToPrevious() {
        player.seekToPrevious()
    }

    override fun toggleRepeat() {
        player.repeatMode = if (player.repeatMode == Player.REPEAT_MODE_ONE) {
            Player.REPEAT_MODE_OFF
        } else {
            Player.REPEAT_MODE_ONE
        }
        publish()
    }

    private fun insert(
        songs: List<Song>,
        index: Int,
    ) {
        if (songs.isEmpty()) return
        val wasEmpty = entries.isEmpty()
        val added = buildEntries(songs, QueueSource.UserQueue, ::createEntryId)
        entries = entries.take(index) + added + entries.drop(index)
        player.addMediaItems(index, added.map(mediaItemFactory::createMediaItem))
        if (wasEmpty) startPlaying() else publish()
    }

    private fun startPlaying() {
        player.prepare()
        resume()
    }

    private fun resume() {
        player.play()
        serviceLauncher.launch()
        publish()
    }

    private fun createEntryId(): String = UUID.randomUUID().toString()

    private fun publish() {
        state.update {
            PlaybackState(
                entries = entries,
                currentIndex = currentIndex,
                context = context,
                status = player.toStatus(),
                position = player.currentPosition.coerceAtLeast(0L).milliseconds,
                duration = player.duration.takeIf { duration -> duration > 0L }?.milliseconds ?: Duration.ZERO,
                isRepeatEnabled = player.repeatMode == Player.REPEAT_MODE_ONE,
            )
        }
    }

    private fun startTicking() {
        positionTicker?.cancel()
        positionTicker = scope.launch {
            while (isActive) {
                publish()
                delay(positionTick)
            }
        }
    }

    private fun stopTicking() {
        positionTicker?.cancel()
        positionTicker = null
        publish()
    }

    private inner class PlayerListener : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) startTicking() else stopTicking()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            publish()
        }

        override fun onMediaItemTransition(
            mediaItem: MediaItem?,
            reason: Int,
        ) {
            publish()
        }

        override fun onPlayerErrorChanged(error: PlaybackException?) {
            publish()
        }
    }
}

private fun Player.toStatus(): PlaybackStatus = when {
    playerError != null -> PlaybackStatus.Failed
    playbackState == Player.STATE_ENDED -> PlaybackStatus.Ended
    playbackState == Player.STATE_BUFFERING -> PlaybackStatus.Buffering
    playbackState == Player.STATE_READY && playWhenReady -> PlaybackStatus.Playing
    playbackState == Player.STATE_READY -> PlaybackStatus.Paused
    else -> PlaybackStatus.Idle
}
