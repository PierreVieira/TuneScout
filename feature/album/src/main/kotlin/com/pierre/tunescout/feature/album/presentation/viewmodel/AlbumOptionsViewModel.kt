package com.pierre.tunescout.feature.album.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.pierre.tunescout.core.model.Album
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.route.AlbumOptionsRoute
import com.pierre.tunescout.core.playback.Enqueuer
import com.pierre.tunescout.core.playback.PlayableSongs
import com.pierre.tunescout.feature.album.domain.usecase.ObserveAlbum
import com.pierre.tunescout.feature.album.presentation.model.AlbumOptionsUiAction
import com.pierre.tunescout.feature.album.presentation.model.AlbumOptionsUiEvent
import com.pierre.tunescout.feature.album.presentation.model.AlbumOptionsUiState
import com.pierre.tunescout.ui.component.R
import com.pierre.tunescout.ui.utils.ActionViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class AlbumOptionsViewModel(
    private val enqueuer: Enqueuer,
    private val playableSongs: PlayableSongs,
    private val navigator: Navigator,
    route: AlbumOptionsRoute,
    observeAlbum: ObserveAlbum,
) : ActionViewModel<AlbumOptionsUiAction>() {
    val uiState: StateFlow<AlbumOptionsUiState> = observeAlbum(route.albumId)
        .map(::AlbumOptionsUiState)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), AlbumOptionsUiState(album = null))

    fun onEvent(event: AlbumOptionsUiEvent) = when (event) {
        AlbumOptionsUiEvent.OnPlayNextClicked -> queue(enqueuer::queueNext)
        AlbumOptionsUiEvent.OnAddToQueueClicked -> queue(enqueuer::addToQueue)
    }

    /**
     * Only the tracks the player can reach are queued, so the queue does not stall on one it cannot.
     * With none of them left the sheet stays open, with the message saying why.
     */
    private fun queue(enqueue: (List<Song>) -> Unit) {
        val album: Album = uiState.value.album ?: return
        val playable = playableSongs.filterPlayable(album.songs)
        if (playable.isEmpty()) return showSongUnavailableOffline()
        enqueue(playable)
        navigator.navigateBack()
    }

    private fun showSongUnavailableOffline() {
        emitAction(AlbumOptionsUiAction.ShowSnackBar(R.string.ui_song_unavailable_offline))
    }
}
