package com.pierre.tunescout.feature.library.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pierre.tunescout.core.model.PlaybackContext
import com.pierre.tunescout.core.model.Playlist
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.route.PlayerRoute
import com.pierre.tunescout.core.navigation.route.SongOptionsRoute
import com.pierre.tunescout.core.playback.ObservablePlayback
import com.pierre.tunescout.core.playback.PlaybackStarter
import com.pierre.tunescout.feature.library.domain.model.CollectionKey
import com.pierre.tunescout.feature.library.domain.usecase.CollectionUseCases
import com.pierre.tunescout.feature.library.presentation.model.CollectionTitle
import com.pierre.tunescout.feature.library.presentation.model.CollectionUiEvent
import com.pierre.tunescout.feature.library.presentation.model.CollectionUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CollectionViewModel(
    private val key: CollectionKey,
    private val useCases: CollectionUseCases,
    private val observablePlayback: ObservablePlayback,
    private val playbackStarter: PlaybackStarter,
    private val navigator: Navigator,
) : ViewModel() {
    private val songs: Flow<List<Song>> = when (key) {
        CollectionKey.Favorites -> useCases.observeFavorites()
        is CollectionKey.Playlist -> useCases.observePlaylistSongs(key.playlistId)
    }
    private val title: Flow<CollectionTitle?> = when (key) {
        CollectionKey.Favorites -> flowOf(CollectionTitle.Favorites)

        is CollectionKey.Playlist ->
            useCases
                .observePlaylist(key.playlistId)
                .map { playlist -> playlist?.let(::toCustomTitle) }
    }

    val uiState: StateFlow<CollectionUiState> = combine(
        title,
        songs,
        observablePlayback.observePlaybackState(),
    ) { title, songs, playback ->
        if (title == null) {
            CollectionUiState.Loading
        } else {
            CollectionUiState.Loaded(
                title = title,
                songs = songs,
                nowPlayingId = playback.currentSong?.id,
                isDeletable = key is CollectionKey.Playlist,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = CollectionUiState.Loading,
    )

    fun onEvent(event: CollectionUiEvent) = when (event) {
        is CollectionUiEvent.OnSongClicked -> playAndOpen(event.song)
        is CollectionUiEvent.OnSongOptionsClicked -> navigator.navigate(SongOptionsRoute(songId = event.song.id))
        is CollectionUiEvent.OnSongRemoved -> removeSong(event.song)
        CollectionUiEvent.OnDeleteClicked -> deleteCollection()
        CollectionUiEvent.OnBackClicked -> navigator.navigateBack()
    }

    private fun playAndOpen(song: Song) {
        playbackStarter.play(song = song, songs = listOf(song), context = PlaybackContext.SingleSong)
        navigator.navigate(PlayerRoute(songId = song.id))
    }

    private fun removeSong(song: Song) {
        viewModelScope.launch {
            when (key) {
                CollectionKey.Favorites -> useCases.removeFavorite(song.id)

                is CollectionKey.Playlist ->
                    useCases.removeSongFromPlaylist(playlistId = key.playlistId, songId = song.id)
            }
        }
    }

    private fun deleteCollection() {
        if (key !is CollectionKey.Playlist) return
        viewModelScope.launch { useCases.deletePlaylist(key.playlistId) }
        navigator.navigateBack()
    }
}

private fun toCustomTitle(playlist: Playlist): CollectionTitle = CollectionTitle.Custom(playlist.name)
