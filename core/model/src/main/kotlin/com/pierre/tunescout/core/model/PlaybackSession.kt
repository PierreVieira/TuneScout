package com.pierre.tunescout.core.model

import kotlin.time.Duration

data class PlaybackSession(
    val entries: List<QueueEntry>,
    val currentEntryId: String?,
    val context: PlaybackContext?,
    val position: Duration,
    val repeatMode: RepeatMode,
    val isShuffleEnabled: Boolean,
    val unshuffledOrder: List<String>,
    val hasEnded: Boolean,
)
