package com.pierre.tunescout.feature.songs.presentation.model

import com.pierre.tunescout.core.model.Song

data class SongsUiState(
    val query: String,
    val recentlyPlayed: List<Song>,
    val nowPlayingId: Long?,
    val isPlaying: Boolean,
) {
    val isSearching: Boolean
        get() = query.isNotBlank()
}
