package com.pierre.tunescout.feature.songs.presentation.model

import com.pierre.tunescout.core.model.NowPlaying
import com.pierre.tunescout.core.model.Song

/**
 * @property query what the user typed in the search field.
 * @property recentlyPlayed the songs played last, newest first.
 * @property nowPlaying which song is playing, when one is.
 * @property songPendingRemoval the recent song swiped away, while the removal can still be undone.
 * @property isOffline the device has no connection right now.
 * @property unplayableSongIds which of [recentlyPlayed] the player cannot reach right now — offline,
 * the ones whose preview never reached the device. Their rows are drawn dimmer, so a tap that is
 * refused is seen coming.
 */
data class SongsUiState(
    val query: String,
    val recentlyPlayed: List<Song>,
    val nowPlaying: NowPlaying?,
    val songPendingRemoval: Song?,
    val isOffline: Boolean,
    val unplayableSongIds: Set<Long>,
) {
    val isSearching: Boolean
        get() = query.isNotBlank()
}
