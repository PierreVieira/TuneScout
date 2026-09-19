package com.pierre.tunescout.feature.album.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pierre.tunescout.core.model.Album
import com.pierre.tunescout.core.model.PlaybackContext
import com.pierre.tunescout.core.model.PlaybackState
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.route.AlbumRoute
import com.pierre.tunescout.core.navigation.route.PlayerRoute
import com.pierre.tunescout.core.playback.Enqueuer
import com.pierre.tunescout.core.playback.ObservablePlayback
import com.pierre.tunescout.core.playback.PlaybackStarter
import com.pierre.tunescout.feature.album.domain.usecase.ObserveAlbum
import com.pierre.tunescout.feature.album.domain.usecase.RefreshAlbum
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
    observeAlbum: ObserveAlbum,
    private val refreshAlbum: RefreshAlbum,
    private val observablePlayback: ObservablePlayback,
    private val playbackStarter: PlaybackStarter,
    private val enqueuer: Enqueuer,
    private val navigator: Navigator,
) : ViewModel() {
    private val refreshFailed = MutableStateFlow(false)

    val uiState: StateFlow<AlbumUiState> = combine(
        observeAlbum(route.albumId),
        observablePlayback.observePlaybackState(),
        refreshFailed,
        ::toUiState,
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(), AlbumUiState.Loading)

    init {
        refresh()
    }

    fun onEvent(event: AlbumUiEvent) = when (event) {
        is AlbumUiEvent.OnSongClicked -> playAndOpen(event.song)
        AlbumUiEvent.OnPlayNextClicked -> queue(enqueuer::queueNext)
        AlbumUiEvent.OnAddToQueueClicked -> queue(enqueuer::addToQueue)
        AlbumUiEvent.OnRetryClicked -> refresh()
        AlbumUiEvent.OnBackClicked -> navigator.navigateBack()
    }

    private fun refresh() {
        refreshFailed.value = false
        viewModelScope.launch {
            refreshAlbum(route.albumId).onFailure { refreshFailed.value = true }
        }
    }

    private fun queue(enqueue: (List<Song>) -> Unit) {
        val album = (uiState.value as? AlbumUiState.Loaded)?.album ?: return
        enqueue(album.songs)
    }

    private fun playAndOpen(song: Song) {
        val album = (uiState.value as? AlbumUiState.Loaded)?.album ?: return
        playbackStarter.play(
            song = song,
            songs = album.songs,
            context = PlaybackContext.Album(id = album.id, title = album.title),
        )
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
