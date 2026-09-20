package com.pierre.tunescout.feature.library.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pierre.tunescout.core.model.PlaybackContext
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.route.SongOptionsRoute
import com.pierre.tunescout.core.playback.Enqueuer
import com.pierre.tunescout.core.playback.ObservablePlayback
import com.pierre.tunescout.core.playback.PlaybackStarter
import com.pierre.tunescout.feature.library.domain.model.CollectionKey
import com.pierre.tunescout.feature.library.domain.usecase.CollectionUseCases
import com.pierre.tunescout.feature.library.presentation.mapper.observeCollectionSongs
import com.pierre.tunescout.feature.library.presentation.mapper.observeCollectionTitle
import com.pierre.tunescout.feature.library.presentation.mapper.toOptionsRoute
import com.pierre.tunescout.feature.library.presentation.model.CollectionUiEvent
import com.pierre.tunescout.feature.library.presentation.model.CollectionUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CollectionViewModel(
    private val key: CollectionKey,
    private val useCases: CollectionUseCases,
    private val observablePlayback: ObservablePlayback,
    private val playbackStarter: PlaybackStarter,
    private val enqueuer: Enqueuer,
    private val navigator: Navigator,
) : ViewModel() {
    val uiState: StateFlow<CollectionUiState> = combine(
        observeCollectionTitle(key = key, useCases = useCases),
        observeCollectionSongs(key = key, useCases = useCases),
        observablePlayback.observePlaybackState(),
    ) { title, songs, playback ->
        if (title == null) {
            CollectionUiState.Loading
        } else {
            CollectionUiState.Loaded(
                title = title,
                songs = songs,
                nowPlayingId = playback.nowPlayingSong?.id,
                isPlaying = playback.isPlaying,
                isDeletable = key is CollectionKey.Playlist,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = CollectionUiState.Loading,
    )

    fun onEvent(event: CollectionUiEvent) = when (event) {
        is CollectionUiEvent.OnSongClicked -> play(event.song)
        is CollectionUiEvent.OnSongOptionsClicked -> navigator.navigate(SongOptionsRoute(songId = event.song.id))
        is CollectionUiEvent.OnSongRemoved -> removeSong(event.song)
        CollectionUiEvent.OnPlayNowClicked -> playNow()
        CollectionUiEvent.OnMoreClicked -> navigator.navigate(key.toOptionsRoute())
        CollectionUiEvent.OnBackClicked -> navigator.navigateBack()
    }

    private fun play(song: Song) {
        playbackStarter.play(song = song, songs = listOf(song), context = PlaybackContext.SingleSong)
    }

    private fun playNow() {
        val songs = (uiState.value as? CollectionUiState.Loaded)?.songs.orEmpty()
        if (songs.isEmpty()) return
        enqueuer.playNow(songs)
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
}
