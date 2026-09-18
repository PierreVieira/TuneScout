package com.quare.tunescout.feature.songs.presentation.model

import com.quare.tunescout.core.model.Song

data class SongsUiState(
    val query: String,
    val recentlyPlayed: List<Song>,
    val nowPlayingId: Long?,
) {
    val isSearching: Boolean
        get() = query.isNotBlank()
}
