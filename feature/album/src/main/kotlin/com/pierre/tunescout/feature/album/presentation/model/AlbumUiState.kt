package com.pierre.tunescout.feature.album.presentation.model

import com.pierre.tunescout.core.model.Album
import com.pierre.tunescout.core.model.CollectionDownloadState
import com.pierre.tunescout.core.model.NowPlaying
import com.pierre.tunescout.core.model.SongDownloadStatus

sealed interface AlbumUiState {
    data object Loading : AlbumUiState

    data object Error : AlbumUiState

    /**
     * @property album the album and its tracks, as the device has them.
     * @property nowPlaying which track is playing, when one of them is.
     * @property isFavorite whether the album is in the user's library.
     * @property isStale the album is the one on the device and the call meant to confirm it
     * failed. The rows stay on screen — they are what the user came for — and the screen says so
     * instead of pretending they are fresh.
     * @property unplayableSongIds which of the tracks the player cannot reach right now — offline,
     * the ones whose preview never reached the device. Their rows are drawn dimmer, so a tap that
     * is refused is seen coming.
     * @property favoriteSongIds which of the tracks are liked, which is what a swipe toward the
     * start of a row would take back.
     * @property isPlaying whether the album is what the player is playing, which turns its play
     * button into a pause button.
     * @property isShuffleEnabled whether the player shuffles, which is also how the album starts.
     * @property isReordering whether the tracks are there to be dragged into an order of the user's
     * own: each row shows a handle instead of its options, and a tap no longer plays it.
     * @property download whether the user asked for the whole album, and how far it has got.
     * @property downloadStatuses how far each track the user asked to keep has got, whether on its
     * own or with a collection; a track absent from it has no download.
     */
    data class Loaded(
        val album: Album,
        val nowPlaying: NowPlaying?,
        val isFavorite: Boolean,
        val isStale: Boolean,
        val unplayableSongIds: Set<Long>,
        val favoriteSongIds: Set<Long>,
        val isPlaying: Boolean,
        val isShuffleEnabled: Boolean,
        val isReordering: Boolean,
        val download: CollectionDownloadState,
        val downloadStatuses: Map<Long, SongDownloadStatus>,
    ) : AlbumUiState
}
