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
     * @property favoriteSongIds which of [songs] are liked, which is what a swipe toward the start
     * of a row would take back.
     * @property unplayableSongIds which of [songs] the player cannot reach right now — offline, the
     * ones whose preview never reached the device. Their rows are drawn dimmer, so a tap that is
     * refused is seen coming.
     * @property isPlaying whether the song playing is one of [songs], which turns the collection's
     * play button into a pause button. A playlist is not a context the player keeps, so the song is
     * all there is to tell it by.
     * @property isShuffleEnabled whether the player shuffles, which is also the order the collection
     * is handed over in.
     */
    data class Loaded(
        val title: CollectionTitle,
        val songs: List<Song>,
        val nowPlaying: NowPlaying?,
        val isDeletable: Boolean,
        val favoriteSongIds: Set<Long>,
        val unplayableSongIds: Set<Long>,
        val isPlaying: Boolean,
        val isShuffleEnabled: Boolean,
    ) : CollectionUiState
}
