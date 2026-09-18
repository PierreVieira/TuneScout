package com.quare.tunescout.feature.songs.presentation.model

import com.quare.tunescout.core.model.Song

sealed interface SongsUiEvent {
    data class OnQueryChanged(
        val query: String,
    ) : SongsUiEvent

    data object OnClearQueryClicked : SongsUiEvent

    data class OnSongClicked(
        val song: Song,
        val queue: List<Song>,
    ) : SongsUiEvent

    data class OnSongOptionsClicked(
        val song: Song,
    ) : SongsUiEvent
}
