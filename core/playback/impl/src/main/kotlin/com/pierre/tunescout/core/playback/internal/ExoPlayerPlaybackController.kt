package com.pierre.tunescout.core.playback.internal

import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.pierre.tunescout.core.model.PlaybackContext
import com.pierre.tunescout.core.model.PlaybackSession
import com.pierre.tunescout.core.model.PlaybackState
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.playback.Enqueuer
import com.pierre.tunescout.core.playback.ObservablePlayback
import com.pierre.tunescout.core.playback.PlaybackStarter
import com.pierre.tunescout.core.playback.QueueControls
import com.pierre.tunescout.core.playback.TransportControls
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

internal class ExoPlayerPlaybackController(
    private val player: ExoPlayer,
    private val serviceLauncher: PlaybackServiceLauncher,
    private val queue: PlaybackQueue,
    private val scope: CoroutineScope,
) : ObservablePlayback,
    PlaybackStarter,
    Enqueuer,
    QueueControls,
    TransportControls,
    RestorablePlayback {
    private val state = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    private val positionTick = 250.milliseconds
    private var context: PlaybackContext? = null
    private var positionTicker: Job? = null

    init {
        player.addListener(
            PlaybackEventListener(
                onPlaybackStarted = ::handlePlaybackStarted,
                onPlaybackStopped = ::stopTicking,
                onPlaybackChanged = ::publish,
            ),
        )
    }

    override fun observePlaybackState(): StateFlow<PlaybackState> = state

    override fun play(
        song: Song,
        songs: List<Song>,
        context: PlaybackContext,
    ) {
        this.context = context
        queue.startContext(song = song, songs = songs)
        startPlaying()
    }

    override fun restore(session: PlaybackSession) {
        if (session.entries.isEmpty()) return
        context = session.context
        queue.restore(
            restoredEntries = session.entries,
            currentEntryId = session.currentEntryId,
            position = session.position,
        )
        player.repeatMode = if (session.isRepeatEnabled) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
        player.prepare()
        publish()
    }

    override fun playNow(songs: List<Song>) {
        insert(songs, queue::playNow, onInserted = ::resume)
    }

    override fun queueNext(songs: List<Song>) {
        insert(songs, queue::queueNext)
    }

    override fun addToQueue(songs: List<Song>) {
        insert(songs, queue::addToQueue)
    }

    override fun removeFromQueue(entryId: String) {
        queue.remove(entryId)
        publish()
    }

    override fun moveInQueue(
        fromIndex: Int,
        toIndex: Int,
    ) {
        queue.move(fromIndex, toIndex)
        publish()
    }

    override fun skipTo(entryId: String) {
        val index = queue.findIndex(entryId)
        if (index < 0) return
        player.seekTo(index, 0L)
        resume()
    }

    override fun togglePlayPause() {
        when {
            player.isPlaying -> player.pause()
            player.playbackState == Player.STATE_ENDED -> replay()
            else -> resume()
        }
    }

    private fun replay() {
        player.seekTo(0L)
        resume()
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
        insertSongs: (List<Song>) -> Unit,
        onInserted: () -> Unit = ::publish,
    ) {
        if (songs.isEmpty()) return
        val wasEmpty = queue.isEmpty
        insertSongs(songs)
        if (wasEmpty) startPlaying() else onInserted()
    }

    private fun startPlaying() {
        player.prepare()
        resume()
    }

    private fun resume() {
        player.play()
        publish()
    }

    private fun publish() {
        state.value = player.toPlaybackState(
            entries = queue.entries,
            currentIndex = queue.currentIndex,
            context = context,
        )
    }

    private fun handlePlaybackStarted() {
        // Only now: the media service has five seconds to promote itself to the foreground,
        // and Media3 can only do that once the player it wraps is actually playing.
        serviceLauncher.launch()
        startTicking()
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
}
