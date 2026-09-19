package com.pierre.tunescout.core.model

sealed interface PlaybackContext {
    data object SingleSong : PlaybackContext

    data class Album(
        val id: Long,
        val title: String,
    ) : PlaybackContext
}
