package com.pierre.tunescout.core.playback.internal

import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player

internal class PlaybackEventListener(
    private val onPlaybackStarted: () -> Unit,
    private val onPlaybackStopped: () -> Unit,
    private val onPlaybackChanged: () -> Unit,
    private val onShuffleModeChanged: () -> Unit,
) : Player.Listener {
    override fun onIsPlayingChanged(isPlaying: Boolean) {
        if (isPlaying) onPlaybackStarted() else onPlaybackStopped()
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        onPlaybackChanged()
    }

    override fun onMediaItemTransition(
        mediaItem: MediaItem?,
        reason: Int,
    ) {
        onPlaybackChanged()
    }

    override fun onPlayerErrorChanged(error: PlaybackException?) {
        onPlaybackChanged()
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
        onPlaybackChanged()
    }

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
        onShuffleModeChanged()
    }
}
