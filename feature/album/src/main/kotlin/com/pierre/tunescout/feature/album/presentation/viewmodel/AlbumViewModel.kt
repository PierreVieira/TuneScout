package com.pierre.tunescout.feature.album.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pierre.tunescout.core.model.Album
import com.pierre.tunescout.core.model.PlaybackContext
import com.pierre.tunescout.core.model.PlaybackState
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.route.AlbumOptionsRoute
import com.pierre.tunescout.core.navigation.route.AlbumRoute
import com.pierre.tunescout.core.navigation.route.SongOptionsRoute
import com.pierre.tunescout.core.playback.Enqueuer
import com.pierre.tunescout.core.playback.ObservablePlayback
import com.pierre.tunescout.core.playback.PlaybackStarter
import com.pierre.tunescout.feature.album.domain.usecase.AlbumUseCases
import com.pierre.tunescout.feature.album.presentation.model.AlbumUiEvent
import com.pierre.tunescout.feature.album.presentation.model.AlbumUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AlbumViewModel(
    private val route: AlbumRoute,
    private val useCases: AlbumUseCases,
    private val playbackStarter: PlaybackStarter,
    private val enqueuer: Enqueuer,
    private val navigator: Navigator,
    observablePlayback: ObservablePlayback,
) : ViewModel() {
    private val refreshFailed = MutableStateFlow(false)

    val uiState: StateFlow<AlbumUiState> = combine(
        useCases.observeAlbum(route.albumId),
        observablePlayback.observePlaybackState(),
        refreshFailed,
        useCases.isAlbumFavorite(route.albumId),
        ::toUiState,
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(), AlbumUiState.Loading)

    init {
        refresh()
    }

    fun onEvent(event: AlbumUiEvent) = when (event) {
        is AlbumUiEvent.OnSongClicked -> play(event.song)
        is AlbumUiEvent.OnSongOptionsClicked -> navigator.navigate(SongOptionsRoute(songId = event.song.id))
        AlbumUiEvent.OnPlayNowClicked -> playNow()
        AlbumUiEvent.OnFavoriteClicked -> toggleFavorite()
        AlbumUiEvent.OnMoreClicked -> navigator.navigate(AlbumOptionsRoute(albumId = route.albumId))
        AlbumUiEvent.OnRetryClicked -> refresh()
        AlbumUiEvent.OnBackClicked -> navigator.navigateBack()
    }

    private fun refresh() {
        refreshFailed.value = false
        viewModelScope.launch {
            useCases.refreshAlbum(route.albumId).onFailure { refreshFailed.value = true }
        }
    }

    private fun playNow() {
        val album = (uiState.value as? AlbumUiState.Loaded)?.album ?: return
        enqueuer.playNow(album.songs)
    }

    private fun toggleFavorite() {
        val state = uiState.value as? AlbumUiState.Loaded ?: return
        viewModelScope.launch {
            useCases.toggleAlbumFavorite(album = state.album, isFavorite = state.isFavorite)
        }
    }

    private fun play(song: Song) {
        val album = (uiState.value as? AlbumUiState.Loaded)?.album ?: return
        playbackStarter.play(
            song = song,
            songs = album.songs,
            context = PlaybackContext.Album(id = album.id, title = album.title),
        )
    }

    private fun toUiState(
        album: Album?,
        playback: PlaybackState,
        refreshFailed: Boolean,
        isFavorite: Boolean,
    ): AlbumUiState = when {
        album != null -> AlbumUiState.Loaded(
            album = album,
            nowPlaying = playback.nowPlaying,
            isFavorite = isFavorite,
        )

        refreshFailed -> AlbumUiState.Error

        else -> AlbumUiState.Loading
    }
}
