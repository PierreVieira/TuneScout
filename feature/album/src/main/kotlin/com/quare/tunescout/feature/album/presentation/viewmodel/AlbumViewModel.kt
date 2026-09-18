package com.quare.tunescout.feature.album.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quare.tunescout.core.model.Album
import com.quare.tunescout.core.model.PlaybackState
import com.quare.tunescout.core.model.Song
import com.quare.tunescout.core.navigation.Navigator
import com.quare.tunescout.core.navigation.route.AlbumRoute
import com.quare.tunescout.core.navigation.route.PlayerRoute
import com.quare.tunescout.core.playback.PlaybackController
import com.quare.tunescout.feature.album.domain.usecase.ObserveAlbum
import com.quare.tunescout.feature.album.domain.usecase.RefreshAlbum
import com.quare.tunescout.feature.album.presentation.model.AlbumUiEvent
import com.quare.tunescout.feature.album.presentation.model.AlbumUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AlbumViewModel(
    private val route: AlbumRoute,
    observeAlbum: ObserveAlbum,
    private val refreshAlbum: RefreshAlbum,
    private val playbackController: PlaybackController,
    private val navigator: Navigator,
) : ViewModel() {
    private val refreshFailed = MutableStateFlow(false)

    val uiState: StateFlow<AlbumUiState> = combine(
        observeAlbum(route.albumId),
        playbackController.state,
        refreshFailed,
        ::toUiState,
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(), AlbumUiState.Loading)

    init {
        refresh()
    }

    fun onEvent(event: AlbumUiEvent) = when (event) {
        is AlbumUiEvent.OnSongClicked -> playAndOpen(event.song)
        AlbumUiEvent.OnRetryClicked -> refresh()
        AlbumUiEvent.OnBackClicked -> navigator.navigateBack()
    }

    private fun refresh() {
        refreshFailed.value = false
        viewModelScope.launch {
            refreshAlbum(route.albumId).onFailure { refreshFailed.value = true }
        }
    }

    private fun playAndOpen(song: Song) {
        val album = (uiState.value as? AlbumUiState.Loaded)?.album ?: return
        playbackController.play(song = song, queue = album.songs)
        navigator.navigate(PlayerRoute(songId = song.id))
    }

    private fun toUiState(
        album: Album?,
        playback: PlaybackState,
        refreshFailed: Boolean,
    ): AlbumUiState = when {
        album != null -> AlbumUiState.Loaded(
            album = album,
            nowPlayingId = playback.currentSong?.id,
            isPlaying = playback.isPlaying,
        )

        refreshFailed -> AlbumUiState.Error

        else -> AlbumUiState.Loading
    }
}
