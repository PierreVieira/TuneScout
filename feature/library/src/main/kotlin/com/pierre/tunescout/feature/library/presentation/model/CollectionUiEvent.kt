package com.pierre.tunescout.feature.library.presentation.model

import com.pierre.tunescout.core.model.Song

sealed interface CollectionUiEvent {
    data class OnSongClicked(
        val song: Song,
    ) : CollectionUiEvent

    data class OnSongOptionsClicked(
        val song: Song,
    ) : CollectionUiEvent

    data class OnSongSwipedToQueue(
        val song: Song,
    ) : CollectionUiEvent

    data object OnDuplicateInQueueConfirmed : CollectionUiEvent

    data object OnDuplicateInQueueDismissed : CollectionUiEvent

    data class OnSongSwipedToFavorite(
        val song: Song,
    ) : CollectionUiEvent

    data object OnPlayPauseClicked : CollectionUiEvent

    data object OnShuffleClicked : CollectionUiEvent

    data object OnDownloadClicked : CollectionUiEvent

    data object OnMoreClicked : CollectionUiEvent

    /** A long press on a row started dragging it, which is also how the reordering starts. */
    data object OnReorderStarted : CollectionUiEvent

    data object OnReorderFinished : CollectionUiEvent

    /**
     * A song takes the place of another, by drag or by the moves a screen reader offers.
     *
     * @property fromSongId the song that moves.
     * @property toSongId the song whose place it takes.
     */
    data class OnSongMoved(
        val fromSongId: Long,
        val toSongId: Long,
    ) : CollectionUiEvent

    data object OnBackClicked : CollectionUiEvent
}
