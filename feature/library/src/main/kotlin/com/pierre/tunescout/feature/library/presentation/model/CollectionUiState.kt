package com.pierre.tunescout.feature.library.presentation.model

import com.pierre.tunescout.core.model.CollectionDownloadState
import com.pierre.tunescout.core.model.NowPlaying
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.model.SongDownloadStatus

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
     * @property isReorderable whether the user can put [songs] in an order of their own — a playlist
     * can, the liked songs are kept in the order they were liked.
     * @property isReordering whether [songs] are there to be dragged into a new order: each row shows
     * a handle instead of its options, and a tap no longer plays it.
     * @property isDownloadable whether the collection can be downloaded as a whole. The songs
     * downloaded one by one cannot: each of them is already kept by its own request, and taking
     * them back is done song by song.
     * @property download whether the user asked for the whole collection, and how far it has got.
     * @property downloadStatuses how far each of [songs] the user asked to keep has got, whether on
     * its own or with a collection; a song absent from it has no download.
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
        val isReorderable: Boolean,
        val isReordering: Boolean,
        val isDownloadable: Boolean,
        val download: CollectionDownloadState,
        val downloadStatuses: Map<Long, SongDownloadStatus>,
    ) : CollectionUiState
}
