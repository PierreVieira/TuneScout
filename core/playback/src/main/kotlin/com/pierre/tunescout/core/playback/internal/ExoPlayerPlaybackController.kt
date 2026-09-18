package com.pierre.tunescout.core.playback.internal

import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.pierre.tunescout.core.model.PlaybackState
import com.pierre.tunescout.core.model.PlaybackStatus
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
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private val positionTick = 250.milliseconds

internal class ExoPlayerPlaybackController(
    private val player: ExoPlayer,
    private val serviceLauncher: PlaybackServiceLauncher,
    private val scope: CoroutineScope,
) : PlaybackController {
    override val state: StateFlow<PlaybackState>
        field = MutableStateFlow(PlaybackState.Idle)

    private var queue: List<Song> = emptyList()
    private var positionTicker: Job? = null

    init {
        player.addListener(PlayerListener())
    }

    override fun play(
        song: Song,
        queue: List<Song>,
    ) {
        this.queue = queue.ifEmpty { listOf(song) }
        val startIndex = this.queue.indexOfFirst { queued -> queued.id == song.id }.coerceAtLeast(0)
        player.setMediaItems(this.queue.map { queued -> queued.toMediaItem() }, startIndex, 0L)
        player.prepare()
        player.play()
        serviceLauncher.launch()
        publish()
    }

    override fun togglePlayPause() {
        if (player.isPlaying) player.pause() else player.play()
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

    private fun publish() {
        state.update {
            PlaybackState(
                currentSong = player.currentMediaItem
                    ?.mediaId
                    ?.toLongOrNull()
                    ?.let(::findSong),
                queue = queue,
                status = player.toStatus(),
                position = player.currentPosition.coerceAtLeast(0L).milliseconds,
                duration = player.duration.takeIf { duration -> duration > 0L }?.milliseconds ?: Duration.ZERO,
                isRepeatEnabled = player.repeatMode == Player.REPEAT_MODE_ONE,
            )
        }
    }

    private fun findSong(id: Long): Song? = queue.firstOrNull { song -> song.id == id }

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

private fun Song.toMediaItem(): MediaItem = MediaItem
    .Builder()
    .setMediaId(id.toString())
    .setUri(previewUrl)
    .setMediaMetadata(
        MediaMetadata
            .Builder()
            .setTitle(title)
            .setArtist(artistName)
            .setAlbumTitle(albumTitle)
            .setArtworkUri(artwork.mediumUrl.toUri())
            .build(),
    ).build()

private fun Player.toStatus(): PlaybackStatus = when {
    playerError != null -> PlaybackStatus.Failed
    playbackState == Player.STATE_ENDED -> PlaybackStatus.Ended
    playbackState == Player.STATE_BUFFERING -> PlaybackStatus.Buffering
    playbackState == Player.STATE_READY && playWhenReady -> PlaybackStatus.Playing
    playbackState == Player.STATE_READY -> PlaybackStatus.Paused
    else -> PlaybackStatus.Idle
}
