package com.pierre.tunescout.feature.songs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.route.AlbumRoute
import com.pierre.tunescout.core.navigation.route.SongOptionsRoute
import com.pierre.tunescout.core.playback.PlaybackController
import com.pierre.tunescout.feature.songs.domain.usecase.ObserveSong
import com.pierre.tunescout.feature.songs.presentation.model.SongOptionsUiEvent
import com.pierre.tunescout.feature.songs.presentation.model.SongOptionsUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class SongOptionsViewModel(
    route: SongOptionsRoute,
    observeSong: ObserveSong,
    private val playbackController: PlaybackController,
    private val navigator: Navigator,
) : ViewModel() {
    val uiState: StateFlow<SongOptionsUiState> = observeSong(route.songId)
        .map(::SongOptionsUiState)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), SongOptionsUiState(song = null))

    fun onEvent(event: SongOptionsUiEvent) = when (event) {
        SongOptionsUiEvent.OnPlayNextClicked -> queue(playbackController::queueNext)
        SongOptionsUiEvent.OnAddToQueueClicked -> queue(playbackController::addToQueue)
        SongOptionsUiEvent.OnViewAlbumClicked -> openAlbum()
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
}
