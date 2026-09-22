package com.pierre.tunescout.feature.songs.presentation.model

import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.ui.utils.permission.PermissionResult

sealed interface SongsUiEvent {
    data class OnQueryChanged(
        val query: String,
    ) : SongsUiEvent

    data object OnClearQueryClicked : SongsUiEvent

    data object OnAudioSearchClicked : SongsUiEvent

    data class OnMicrophonePermissionResult(
        val result: PermissionResult,
    ) : SongsUiEvent

    data object OnThemeClicked : SongsUiEvent

    data class OnSongClicked(
        val song: Song,
    ) : SongsUiEvent

    data class OnSongOptionsClicked(
        val song: Song,
    ) : SongsUiEvent

    data class OnSongSwipedToQueue(
        val song: Song,
    ) : SongsUiEvent

    data object OnDuplicateInQueueConfirmed : SongsUiEvent

    data object OnDuplicateInQueueDismissed : SongsUiEvent

    data class OnSongSwipedToFavorite(
        val song: Song,
    ) : SongsUiEvent

    data class OnRemoveRecentClicked(
        val song: Song,
    ) : SongsUiEvent

    data object OnRemoveRecentConfirmed : SongsUiEvent

    data object OnRemoveRecentDismissed : SongsUiEvent
}
