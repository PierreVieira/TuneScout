package com.pierre.tunescout.feature.library.presentation.model

import com.pierre.tunescout.core.model.NowPlaying
import com.pierre.tunescout.core.model.Song

sealed interface CollectionUiState {
    data object Loading : CollectionUiState

    /**
     * @property title what the collection is called.
     * @property songs the songs it holds, in the order they were added.
     * @property nowPlaying which song is playing, when one is.
     * @property isDeletable whether the collection itself can be deleted — a playlist can, the
     * liked songs cannot.
     * @property songPendingRemoval the song swiped away, while the removal can still be undone.
     * @property unplayableSongIds which of [songs] the player cannot reach right now — offline, the
     * ones whose preview never reached the device. Their rows are drawn dimmer, so a tap that is
     * refused is seen coming.
     */
    data class Loaded(
        val title: CollectionTitle,
        val songs: List<Song>,
        val nowPlaying: NowPlaying?,
        val isDeletable: Boolean,
        val songPendingRemoval: Song?,
        val unplayableSongIds: Set<Long>,
    ) : CollectionUiState
}
