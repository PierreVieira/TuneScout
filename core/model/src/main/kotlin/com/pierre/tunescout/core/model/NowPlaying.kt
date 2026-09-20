package com.pierre.tunescout.core.model

/**
 * The song a list marks as the one the player is on. There is no instance when nothing is loaded or
 * the song has ended, so "playing, but no song" cannot be written down.
 */
data class NowPlaying(
    val songId: Long,
    val isPlaying: Boolean,
)
