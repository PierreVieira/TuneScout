package com.pierre.tunescout.feature.album.presentation.model

import com.pierre.tunescout.core.model.Song

sealed interface AlbumUiEvent {
    data class OnSongClicked(
        val song: Song,
    ) : AlbumUiEvent

    data class OnSongOptionsClicked(
        val song: Song,
    ) : AlbumUiEvent

    data class OnSongSwipedToQueue(
        val song: Song,
    ) : AlbumUiEvent

    data object OnDuplicateInQueueConfirmed : AlbumUiEvent

    data object OnDuplicateInQueueDismissed : AlbumUiEvent

    data class OnSongSwipedToFavorite(
        val song: Song,
    ) : AlbumUiEvent

    data object OnPlayPauseClicked : AlbumUiEvent

    data object OnShuffleClicked : AlbumUiEvent

    data object OnFavoriteClicked : AlbumUiEvent

    data object OnDownloadClicked : AlbumUiEvent

    data object OnMoreClicked : AlbumUiEvent

    data object OnRetryClicked : AlbumUiEvent

    /** A long press on a row started dragging it, which is also how the reordering starts. */
    data object OnReorderStarted : AlbumUiEvent

    data object OnReorderFinished : AlbumUiEvent

    /**
     * A song takes the place of another, by drag or by the moves a screen reader offers.
     *
     * @property fromSongId the song that moves.
     * @property toSongId the song whose place it takes.
     */
    data class OnSongMoved(
        val fromSongId: Long,
        val toSongId: Long,
    ) : AlbumUiEvent

    data object OnBackClicked : AlbumUiEvent
}
