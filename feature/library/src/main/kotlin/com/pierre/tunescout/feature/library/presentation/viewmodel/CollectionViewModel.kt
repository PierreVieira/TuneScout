package com.pierre.tunescout.feature.library.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.pierre.tunescout.core.model.PlaybackContext
import com.pierre.tunescout.core.model.PlaybackState
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.route.PlayerRoute
import com.pierre.tunescout.core.navigation.route.SongOptionsRoute
import com.pierre.tunescout.core.playback.Enqueuer
import com.pierre.tunescout.core.playback.ObservablePlayableSongs
import com.pierre.tunescout.core.playback.ObservablePlayback
import com.pierre.tunescout.core.playback.PlayableSongs
import com.pierre.tunescout.core.playback.SongPlayOutcome
import com.pierre.tunescout.core.playback.SongPlayback
import com.pierre.tunescout.core.playback.TransportControls
import com.pierre.tunescout.feature.library.domain.model.CollectionKey
import com.pierre.tunescout.feature.library.domain.usecase.CollectionUseCases
import com.pierre.tunescout.feature.library.presentation.mapper.CollectionStreams
import com.pierre.tunescout.feature.library.presentation.mapper.toOptionsRoute
import com.pierre.tunescout.feature.library.presentation.model.CollectionUiAction
import com.pierre.tunescout.feature.library.presentation.model.CollectionUiEvent
import com.pierre.tunescout.feature.library.presentation.model.CollectionUiState
import com.pierre.tunescout.ui.component.R
import com.pierre.tunescout.ui.utils.ActionViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CollectionViewModel(
    private val key: CollectionKey,
    private val useCases: CollectionUseCases,
    private val songPlayback: SongPlayback,
    private val playableSongs: PlayableSongs,
    private val enqueuer: Enqueuer,
    private val transportControls: TransportControls,
    private val navigator: Navigator,
    private val observablePlayback: ObservablePlayback,
    collectionStreams: CollectionStreams,
    observablePlayableSongs: ObservablePlayableSongs,
) : ActionViewModel<CollectionUiAction>() {
    private val songPendingRemoval = MutableStateFlow<Song?>(null)

    val uiState: StateFlow<CollectionUiState> = combine(
        collectionStreams.observeTitle(key),
        collectionStreams.observeSongs(key),
        observablePlayback.observePlaybackState(),
        songPendingRemoval,
        observablePlayableSongs.observePlayableSongs(),
    ) { title, songs, playback, pendingRemoval, playable ->
        if (title == null) {
            CollectionUiState.Loading
        } else {
            CollectionUiState.Loaded(
                title = title,
                songs = songs,
                nowPlaying = playback.nowPlaying,
                isDeletable = key is CollectionKey.Playlist,
                songPendingRemoval = pendingRemoval,
                unplayableSongIds = playable.findUnplayableIds(songs),
                isPlaying = playback.isPlaying && playback.isOnOneOf(songs),
                isShuffleEnabled = playback.isShuffleEnabled,
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
        CollectionUiEvent.OnPlayPauseClicked -> togglePlayback()
        CollectionUiEvent.OnShuffleClicked -> transportControls.toggleShuffle()
        CollectionUiEvent.OnMoreClicked -> navigator.navigate(key.toOptionsRoute())
        CollectionUiEvent.OnBackClicked -> navigator.navigateBack()
    }

    private fun play(song: Song) {
        val outcome = songPlayback.request(
            song = song,
            nowPlaying = (uiState.value as? CollectionUiState.Loaded)?.nowPlaying,
            queue = listOf(song),
            context = PlaybackContext.SingleSong,
        )
        when (outcome) {
            SongPlayOutcome.AlreadyPlaying -> navigator.navigate(PlayerRoute(songId = song.id))
            SongPlayOutcome.Unavailable -> showSongUnavailableOffline()
            SongPlayOutcome.Started -> Unit
        }
    }

    /**
     * The player already on one of the collection's songs is paused and resumed; otherwise the
     * collection is played now, ahead of the rest of the queue. It is not a context the player can
     * shuffle later, so with shuffle on it is handed over already shuffled.
     */
    private fun togglePlayback() {
        val songs = (uiState.value as? CollectionUiState.Loaded)?.songs.orEmpty()
        if (songs.isEmpty()) return
        val playback = observablePlayback.observePlaybackState().value
        if (playback.isOnOneOf(songs) && !playback.hasEnded) return resume(playback)
        val playable = playableSongs.findPlayableOrNull(songs) ?: return showSongUnavailableOffline()
        enqueuer.playNow(if (playback.isShuffleEnabled) playable.shuffled() else playable)
    }

    /** Pausing is always honoured; resuming a song the player cannot reach is refused. */
    private fun resume(playback: PlaybackState) {
        val song = playback.currentSong
        if (!playback.isPlaying && song != null && !playableSongs.isPlayable(song)) {
            return showSongUnavailableOffline()
        }
        transportControls.togglePlayPause()
    }

    private fun PlaybackState.isOnOneOf(songs: List<Song>): Boolean = songs.any { song -> song.id == currentSong?.id }

    private fun showSongUnavailableOffline() {
        emitAction(CollectionUiAction.ShowSnackBar(R.string.ui_song_unavailable_offline))
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
