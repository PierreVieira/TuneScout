package com.pierre.tunescout.core.playback.internal

import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.pierre.tunescout.core.model.PlaybackContext
import com.pierre.tunescout.core.model.PlaybackSession
import com.pierre.tunescout.core.model.PlaybackState
import com.pierre.tunescout.core.model.PlaybackStatus
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.playback.ContextStarter
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
    ContextStarter,
    Enqueuer,
    QueueControls,
    TransportControls,
    RestorablePlayback {
    private val state = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    private val positionTick = 250.milliseconds
    private var context: PlaybackContext? = null
    private var restoredEndedEntryId: String? = null
    private var positionTicker: Job? = null

    init {
        player.addListener(
            PlaybackEventListener(
                onPlaybackStarted = ::handlePlaybackStarted,
                onPlaybackStopped = ::stopTicking,
                onPlaybackChanged = ::publish,
                onShuffleModeChanged = ::syncShuffle,
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

    override fun playFromStart(
        songs: List<Song>,
        context: PlaybackContext,
    ) {
        if (songs.isEmpty()) return
        this.context = context
        queue.startContextFromTop(songs)
        startPlaying()
    }

    override fun restore(session: PlaybackSession) {
        if (session.entries.isEmpty()) return
        context = session.context
        queue.restore(
            restoredEntries = session.entries,
            currentEntryId = session.currentEntryId,
            position = session.position,
            isShuffled = session.isShuffleEnabled,
            unshuffledOrder = session.unshuffledOrder,
        )
        restoredEndedEntryId = session.currentEntryId.takeIf { session.hasEnded }
        player.repeatMode = session.repeatMode.toPlayerRepeatMode()
        player.shuffleModeEnabled = session.isShuffleEnabled
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

    override fun clearQueue() {
        queue.clear()
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
            player.playbackState == Player.STATE_ENDED || state.value.hasEnded -> replay()
            else -> resume()
        }
    }

    private fun replay() {
        player.seekTo(0L)
        resume()
    }

    override fun seekTo(position: Duration) {
        restoredEndedEntryId = null
        player.seekTo(position.inWholeMilliseconds)
        publish()
    }

    override fun skipToNext() {
        restoredEndedEntryId = null
        if (player.hasNextMediaItem()) player.seekToNextMediaItem() else player.seekTo(0L)
    }

    override fun skipToPrevious() {
        restoredEndedEntryId = null
        player.seekToPrevious()
    }

    override fun cycleRepeatMode() {
        player.repeatMode = player.repeatMode
            .toRepeatMode()
            .next
            .toPlayerRepeatMode()
        publish()
    }

    /**
     * The queue is rearranged before the player is told, not after. Told first, the player would
     * hand the change to the queue from inside its own callback, and the edits the queue made there
     * would reach the media session late, each paired with where the player ended up — past the end
     * of a timeline that was shortened halfway through putting an album back, which the session
     * refuses. Told last, the player finds the queue already in the order it announces.
     */
    override fun toggleShuffle() {
        val isShuffled = !player.shuffleModeEnabled
        arrangeQueue(isShuffled)
        player.shuffleModeEnabled = isShuffled
        publish()
    }

    /**
     * The player's shuffle mode is what the media session shows, and the notification or a paired
     * device can flip it on their own: the queue follows it from here, whoever changed it. The
     * player itself never reorders anything — it is built with an order that keeps the queue's.
     */
    private fun syncShuffle() {
        arrangeQueue(player.shuffleModeEnabled)
        publish()
    }

    private fun arrangeQueue(isShuffled: Boolean) {
        if (isShuffled) queue.shuffle() else queue.unshuffle()
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
        restoredEndedEntryId = null
        player.play()
        publish()
    }

    private fun publish() {
        val playbackState = player.toPlaybackState(
            entries = queue.entries,
            currentIndex = queue.currentIndex,
            context = context,
            unshuffledOrder = queue.unshuffledOrder,
        )
        state.value = if (isStillEnded(playbackState)) {
            playbackState.copy(status = PlaybackStatus.Ended)
        } else {
            playbackState
        }
    }

    /**
     * A player restored at the end of a song comes back paused there, not ended: the song only
     * stops being ended once something moves the player.
     *
     * @return whether [playbackState] is still on the entry that was restored as ended.
     */
    private fun isStillEnded(playbackState: PlaybackState): Boolean = restoredEndedEntryId != null &&
        playbackState.currentEntry?.id == restoredEndedEntryId &&
        playbackState.status != PlaybackStatus.Failed

    /**
     * The service is launched only now: it has five seconds to promote itself to the foreground,
     * and Media3 can only do that once the player it wraps is actually playing.
     */
    private fun handlePlaybackStarted() {
        restoredEndedEntryId = null
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
