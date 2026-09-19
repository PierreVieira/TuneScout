package com.pierre.tunescout.feature.songs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.route.AlbumRoute
import com.pierre.tunescout.core.navigation.route.SongOptionsRoute
import com.pierre.tunescout.core.playback.PlaybackController
import com.pierre.tunescout.feature.songs.domain.usecase.SongOptionsUseCases
import com.pierre.tunescout.feature.songs.presentation.model.SongOptionsUiEvent
import com.pierre.tunescout.feature.songs.presentation.model.SongOptionsUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SongOptionsViewModel(
    route: SongOptionsRoute,
    private val useCases: SongOptionsUseCases,
    private val playbackController: PlaybackController,
    private val navigator: Navigator,
) : ViewModel() {
    private val emptyUiState = SongOptionsUiState(song = null, isRecentlyPlayed = false)

    val uiState: StateFlow<SongOptionsUiState> = combine(
        useCases.observeSong(route.songId),
        useCases.observeRecentlyPlayed(),
        ::toUiState,
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyUiState)

    fun onEvent(event: SongOptionsUiEvent) = when (event) {
        SongOptionsUiEvent.OnPlayNextClicked -> queue(playbackController::queueNext)
        SongOptionsUiEvent.OnAddToQueueClicked -> queue(playbackController::addToQueue)
        SongOptionsUiEvent.OnViewAlbumClicked -> openAlbum()
        SongOptionsUiEvent.OnRemoveFromRecentlyPlayedClicked -> removeFromRecentlyPlayed()
        SongOptionsUiEvent.OnDismissed -> navigator.navigateBack()
    }

    private fun toUiState(
        song: Song?,
        recentlyPlayed: List<Song>,
    ): SongOptionsUiState = SongOptionsUiState(
        song = song,
        isRecentlyPlayed = song != null && recentlyPlayed.any { recent -> recent.id == song.id },
    )

    private fun queue(enqueue: (List<Song>) -> Unit) {
        val song = uiState.value.song ?: return
        enqueue(listOf(song))
        navigator.navigateBack()
    }

    private fun openAlbum() {
        val albumId = uiState.value.song?.albumId ?: return
        navigator.navigateReplacingTop(AlbumRoute(albumId = albumId))
    }

    private fun removeFromRecentlyPlayed() {
        val songId = uiState.value.song?.id ?: return
        viewModelScope.launch {
            useCases.removeFromRecentlyPlayed(songId)
            navigator.navigateBack()
        }
    }
}
