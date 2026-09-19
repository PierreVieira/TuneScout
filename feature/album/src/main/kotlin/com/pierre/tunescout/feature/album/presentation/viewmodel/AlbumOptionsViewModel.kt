package com.pierre.tunescout.feature.album.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pierre.tunescout.core.model.Album
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.route.AlbumOptionsRoute
import com.pierre.tunescout.core.playback.Enqueuer
import com.pierre.tunescout.feature.album.domain.usecase.ObserveAlbum
import com.pierre.tunescout.feature.album.presentation.model.AlbumOptionsUiEvent
import com.pierre.tunescout.feature.album.presentation.model.AlbumOptionsUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class AlbumOptionsViewModel(
    route: AlbumOptionsRoute,
    observeAlbum: ObserveAlbum,
    private val enqueuer: Enqueuer,
    private val navigator: Navigator,
) : ViewModel() {
    val uiState: StateFlow<AlbumOptionsUiState> = observeAlbum(route.albumId)
        .map(::AlbumOptionsUiState)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), AlbumOptionsUiState(album = null))

    fun onEvent(event: AlbumOptionsUiEvent) = when (event) {
        AlbumOptionsUiEvent.OnPlayNextClicked -> queue(enqueuer::queueNext)
        AlbumOptionsUiEvent.OnAddToQueueClicked -> queue(enqueuer::addToQueue)
    }

    private fun queue(enqueue: (List<Song>) -> Unit) {
        val album: Album = uiState.value.album ?: return
        enqueue(album.songs)
        navigator.navigateBack()
    }
}
