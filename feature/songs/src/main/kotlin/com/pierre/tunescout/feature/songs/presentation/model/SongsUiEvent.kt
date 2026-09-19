package com.pierre.tunescout.feature.songs.presentation.model

import com.pierre.tunescout.core.model.Song

sealed interface SongsUiEvent {
    data class OnQueryChanged(
        val query: String,
    ) : SongsUiEvent

    data object OnClearQueryClicked : SongsUiEvent

    data class OnSongClicked(
        val song: Song,
    ) : SongsUiEvent

    data class OnSongOptionsClicked(
        val song: Song,
    ) : SongsUiEvent

    data class OnRecentSongSwipedAway(
        val song: Song,
    ) : SongsUiEvent
}
