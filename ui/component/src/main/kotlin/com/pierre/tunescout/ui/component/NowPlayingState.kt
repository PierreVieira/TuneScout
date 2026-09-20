package com.pierre.tunescout.ui.component

enum class NowPlayingState {
    None,
    Playing,
    Paused,
}

fun getNowPlayingState(
    isCurrentSong: Boolean,
    isPlaying: Boolean,
): NowPlayingState = when {
    !isCurrentSong -> NowPlayingState.None
    isPlaying -> NowPlayingState.Playing
    else -> NowPlayingState.Paused
}
