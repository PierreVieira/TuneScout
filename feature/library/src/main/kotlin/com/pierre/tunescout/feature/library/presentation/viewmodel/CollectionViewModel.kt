package com.pierre.tunescout.feature.library.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pierre.tunescout.core.model.PlaybackContext
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.route.SongOptionsRoute
import com.pierre.tunescout.core.playback.Enqueuer
import com.pierre.tunescout.core.playback.ObservablePlayback
import com.pierre.tunescout.core.playback.PlayableSongs
import com.pierre.tunescout.core.playback.PlaybackStarter
import com.pierre.tunescout.feature.library.domain.model.CollectionKey
import com.pierre.tunescout.feature.library.domain.usecase.CollectionUseCases
import com.pierre.tunescout.feature.library.presentation.mapper.CollectionStreams
import com.pierre.tunescout.feature.library.presentation.mapper.toOptionsRoute
import com.pierre.tunescout.feature.library.presentation.model.CollectionUiAction
import com.pierre.tunescout.feature.library.presentation.model.CollectionUiEvent
import com.pierre.tunescout.feature.library.presentation.model.CollectionUiState
import com.pierre.tunescout.ui.component.R
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CollectionViewModel(
    private val key: CollectionKey,
    private val useCases: CollectionUseCases,
    private val playbackStarter: PlaybackStarter,
    private val playableSongs: PlayableSongs,
    private val enqueuer: Enqueuer,
    private val navigator: Navigator,
    collectionStreams: CollectionStreams,
    observablePlayback: ObservablePlayback,
) : ViewModel() {
    private val songPendingRemoval = MutableStateFlow<Song?>(null)

    val uiAction: SharedFlow<CollectionUiAction>
        field = MutableSharedFlow<CollectionUiAction>()

    val uiState: StateFlow<CollectionUiState> = combine(
        collectionStreams.observeTitle(key),
        collectionStreams.observeSongs(key),
        observablePlayback.observePlaybackState(),
        songPendingRemoval,
    ) { title, songs, playback, pendingRemoval ->
        if (title == null) {
            CollectionUiState.Loading
        } else {
            CollectionUiState.Loaded(
                title = title,
                songs = songs,
                nowPlaying = playback.nowPlaying,
                isDeletable = key is CollectionKey.Playlist,
                songPendingRemoval = pendingRemoval,
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
        is CollectionUiEvent.OnSongSwipedAway -> requestRemoval(event.song)
        CollectionUiEvent.OnRemovalConfirmed -> confirmRemoval()
        CollectionUiEvent.OnRemovalDismissed -> songPendingRemoval.value = null
        CollectionUiEvent.OnPlayNowClicked -> playNow()
        CollectionUiEvent.OnMoreClicked -> navigator.navigate(key.toOptionsRoute())
        CollectionUiEvent.OnBackClicked -> navigator.navigateBack()
    }

    /**
     * A song the player cannot reach is refused with a message before it gets there, and playing the
     * whole collection keeps only the songs it can reach.
     */
    private fun play(song: Song) {
        if (!playableSongs.isPlayable(song)) return showSongUnavailableOffline()
        playbackStarter.play(song = song, songs = listOf(song), context = PlaybackContext.SingleSong)
    }

    private fun playNow() {
        val songs = (uiState.value as? CollectionUiState.Loaded)?.songs.orEmpty()
        if (songs.isEmpty()) return
        val playable = playableSongs.filterPlayable(songs)
        if (playable.isEmpty()) return showSongUnavailableOffline()
        enqueuer.playNow(playable)
    }

    private fun showSongUnavailableOffline() {
        emitAction(CollectionUiAction.ShowSnackBar(R.string.ui_song_unavailable_offline))
    }

    private fun emitAction(action: CollectionUiAction) {
        viewModelScope.launch { uiAction.emit(action) }
    }

    private fun requestRemoval(song: Song) {
        when (key) {
            CollectionKey.Favorites -> removeSong(song)
            is CollectionKey.Playlist -> songPendingRemoval.value = song
        }
    }

    private fun confirmRemoval() {
        val song = songPendingRemoval.value ?: return
        songPendingRemoval.value = null
        removeSong(song)
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
