package com.pierre.tunescout.feature.songoptions.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.route.AlbumRoute
import com.pierre.tunescout.core.navigation.route.SongOptionsRoute
import com.pierre.tunescout.core.playback.Enqueuer
import com.pierre.tunescout.feature.songoptions.domain.usecase.SongOptionsUseCases
import com.pierre.tunescout.feature.songoptions.presentation.model.SongOptionsUiEvent
import com.pierre.tunescout.feature.songoptions.presentation.model.SongOptionsUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SongOptionsViewModel(
    route: SongOptionsRoute,
    private val useCases: SongOptionsUseCases,
    private val enqueuer: Enqueuer,
    private val navigator: Navigator,
) : ViewModel() {
    private val emptyUiState = SongOptionsUiState(song = null, isRecentlyPlayed = false)

    val uiState: StateFlow<SongOptionsUiState> = combine(
        useCases.observeSong(route.songId),
        useCases.isRecentlyPlayed(route.songId),
        ::SongOptionsUiState,
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyUiState)

    fun onEvent(event: SongOptionsUiEvent) = when (event) {
        SongOptionsUiEvent.OnPlayNextClicked -> queue(enqueuer::queueNext)
        SongOptionsUiEvent.OnAddToQueueClicked -> queue(enqueuer::addToQueue)
        SongOptionsUiEvent.OnViewAlbumClicked -> openAlbum()
        SongOptionsUiEvent.OnRemoveFromRecentlyPlayedClicked -> removeFromRecentlyPlayed()
        SongOptionsUiEvent.OnDismissed -> navigator.navigateBack()
    }

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
