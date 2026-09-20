package com.pierre.tunescout.feature.songs.presentation.model

import com.pierre.tunescout.core.model.NowPlaying
import com.pierre.tunescout.core.model.Song

data class SongsUiState(
    val query: String,
    val recentlyPlayed: List<Song>,
    val nowPlaying: NowPlaying?,
    val songPendingRemoval: Song?,
) {
    val isSearching: Boolean
        get() = query.isNotBlank()
}
