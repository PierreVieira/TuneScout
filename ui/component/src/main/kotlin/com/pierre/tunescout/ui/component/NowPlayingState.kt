package com.pierre.tunescout.ui.component

enum class NowPlayingState {
    None,
    Playing,
    Paused,
    ;

    companion object {
        fun of(
            isCurrentSong: Boolean,
            isPlaying: Boolean,
        ): NowPlayingState = when {
            !isCurrentSong -> None
            isPlaying -> Playing
            else -> Paused
        }
    }
}
