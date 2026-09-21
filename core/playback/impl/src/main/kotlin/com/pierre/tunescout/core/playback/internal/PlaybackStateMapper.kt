package com.pierre.tunescout.core.playback.internal

import androidx.media3.common.Player
import com.pierre.tunescout.core.model.PlaybackContext
import com.pierre.tunescout.core.model.PlaybackState
import com.pierre.tunescout.core.model.PlaybackStatus
import com.pierre.tunescout.core.model.QueueEntry
import com.pierre.tunescout.core.model.RepeatMode
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

internal fun Player.toPlaybackState(
    entries: List<QueueEntry>,
    currentIndex: Int,
    context: PlaybackContext?,
    unshuffledOrder: List<String>,
): PlaybackState = PlaybackState(
    entries = entries,
    currentIndex = currentIndex,
    context = context,
    status = toStatus(),
    position = currentPosition.coerceAtLeast(0L).milliseconds,
    duration = duration.takeIf { millis -> millis > 0L }?.milliseconds ?: Duration.ZERO,
    repeatMode = repeatMode.toRepeatMode(),
    isShuffleEnabled = shuffleModeEnabled,
    unshuffledOrder = unshuffledOrder,
)

internal fun RepeatMode.toPlayerRepeatMode(): Int = when (this) {
    RepeatMode.Off -> Player.REPEAT_MODE_OFF
    RepeatMode.All -> Player.REPEAT_MODE_ALL
    RepeatMode.One -> Player.REPEAT_MODE_ONE
}

internal fun Int.toRepeatMode(): RepeatMode = when (this) {
    Player.REPEAT_MODE_ALL -> RepeatMode.All
    Player.REPEAT_MODE_ONE -> RepeatMode.One
    else -> RepeatMode.Off
}

private fun Player.toStatus(): PlaybackStatus = when {
    playerError != null -> PlaybackStatus.Failed
    playbackState == Player.STATE_ENDED -> PlaybackStatus.Ended
    playbackState == Player.STATE_BUFFERING -> PlaybackStatus.Buffering
    playbackState == Player.STATE_READY && playWhenReady -> PlaybackStatus.Playing
    playbackState == Player.STATE_READY -> PlaybackStatus.Paused
    else -> PlaybackStatus.Idle
}
