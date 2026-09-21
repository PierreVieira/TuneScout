package com.pierre.tunescout.feature.songoptions.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.route.AddToPlaylistRoute
import com.pierre.tunescout.core.navigation.route.AlbumRoute
import com.pierre.tunescout.core.navigation.route.SongOptionsRoute
import com.pierre.tunescout.core.playback.Enqueuer
import com.pierre.tunescout.core.playback.PlayableSongs
import com.pierre.tunescout.feature.songoptions.domain.usecase.SongOptionsUseCases
import com.pierre.tunescout.feature.songoptions.presentation.model.SongOptionsUiAction
import com.pierre.tunescout.feature.songoptions.presentation.model.SongOptionsUiEvent
import com.pierre.tunescout.feature.songoptions.presentation.model.SongOptionsUiState
import com.pierre.tunescout.ui.component.R
import com.pierre.tunescout.ui.utils.ActionViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SongOptionsViewModel(
    private val useCases: SongOptionsUseCases,
    private val enqueuer: Enqueuer,
    private val playableSongs: PlayableSongs,
    private val navigator: Navigator,
    route: SongOptionsRoute,
) : ActionViewModel<SongOptionsUiAction>() {
    private val emptyUiState = SongOptionsUiState(
        song = null,
        isFavorite = false,
    )

    val uiState: StateFlow<SongOptionsUiState> = combine(
        useCases.observeSong(route.songId),
        useCases.isFavorite(route.songId),
        ::SongOptionsUiState,
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyUiState)

    fun onEvent(event: SongOptionsUiEvent) = when (event) {
        SongOptionsUiEvent.OnFavoriteClicked -> toggleFavorite()
        SongOptionsUiEvent.OnAddToPlaylistClicked -> openAddToPlaylist()
        SongOptionsUiEvent.OnPlayNowClicked -> queue(enqueuer::playNow)
        SongOptionsUiEvent.OnPlayNextClicked -> queue(enqueuer::queueNext)
        SongOptionsUiEvent.OnAddToQueueClicked -> queue(enqueuer::addToQueue)
        SongOptionsUiEvent.OnViewAlbumClicked -> openAlbum()
        SongOptionsUiEvent.OnDismissed -> navigator.navigateBack()
    }

    /**
     * A song the player cannot reach never enters the queue, so it does not stall on it. The sheet
     * stays open instead, with the message saying why.
     */
    private fun queue(enqueue: (List<Song>) -> Unit) {
        val song = uiState.value.song ?: return
        if (!playableSongs.isPlayable(song)) return showSongUnavailableOffline()
        enqueue(listOf(song))
        navigator.navigateBack()
    }

    private fun showSongUnavailableOffline() {
        emitAction(SongOptionsUiAction.ShowSnackBar(R.string.ui_song_unavailable_offline))
    }

    private fun toggleFavorite() {
        val state = uiState.value
        val song = state.song ?: return
        viewModelScope.launch {
            useCases.toggleFavorite(song = song, isFavorite = state.isFavorite)
            navigator.navigateBack()
        }
    }

    private fun openAddToPlaylist() {
        val songId = uiState.value.song?.id ?: return
        navigator.navigateReplacingTop(AddToPlaylistRoute(songId = songId))
    }

    private fun openAlbum() {
        val albumId = uiState.value.song?.albumId ?: return
        navigator.navigateReplacingTop(AlbumRoute(albumId = albumId))
    }
}
