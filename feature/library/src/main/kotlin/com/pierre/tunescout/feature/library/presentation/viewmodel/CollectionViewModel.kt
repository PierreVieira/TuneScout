package com.pierre.tunescout.feature.library.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.pierre.tunescout.core.model.PlaybackContext
import com.pierre.tunescout.core.model.PlaybackState
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.route.PlayerRoute
import com.pierre.tunescout.core.navigation.route.SongOptionsRoute
import com.pierre.tunescout.core.playback.ContextStarter
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
import com.pierre.tunescout.feature.library.presentation.model.CollectionTitle
import com.pierre.tunescout.feature.library.presentation.model.CollectionUiAction
import com.pierre.tunescout.feature.library.presentation.model.CollectionUiEvent
import com.pierre.tunescout.feature.library.presentation.model.CollectionUiState
import com.pierre.tunescout.ui.component.R
import com.pierre.tunescout.ui.utils.ActionViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CollectionViewModel(
    private val key: CollectionKey,
    private val useCases: CollectionUseCases,
    private val songPlayback: SongPlayback,
    private val contextStarter: ContextStarter,
    private val playableSongs: PlayableSongs,
    private val enqueuer: Enqueuer,
    private val transportControls: TransportControls,
    private val navigator: Navigator,
    private val observablePlayback: ObservablePlayback,
    collectionStreams: CollectionStreams,
    observablePlayableSongs: ObservablePlayableSongs,
) : ActionViewModel<CollectionUiAction>() {
    val uiState: StateFlow<CollectionUiState> = combine(
        collectionStreams.observeTitle(key),
        collectionStreams.observeSongs(key),
        observablePlayback.observePlaybackState(),
        collectionStreams.observeFavoriteSongIds(),
        observablePlayableSongs.observePlayableSongs(),
    ) { title, songs, playback, favoriteSongIds, playable ->
        if (title == null) {
            CollectionUiState.Loading
        } else {
            CollectionUiState.Loaded(
                title = title,
                songs = songs,
                nowPlaying = playback.nowPlaying,
                isDeletable = key is CollectionKey.Playlist,
                favoriteSongIds = favoriteSongIds,
                unplayableSongIds = playable.findUnplayableIds(songs),
                isPlaying = playback.isPlaying && playback.isOnCollection(),
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
        is CollectionUiEvent.OnSongOptionsClicked -> openSongOptions(event.song)
        is CollectionUiEvent.OnSongSwipedToQueue -> addToQueue(event.song)
        is CollectionUiEvent.OnSongSwipedToFavorite -> toggleFavorite(event.song)
        CollectionUiEvent.OnPlayPauseClicked -> togglePlayback()
        CollectionUiEvent.OnShuffleClicked -> transportControls.toggleShuffle()
        CollectionUiEvent.OnMoreClicked -> navigator.navigate(key.toOptionsRoute())
        CollectionUiEvent.OnBackClicked -> navigator.navigateBack()
    }

    /** The rest of the collection plays on after [song], in the collection's order. */
    private fun play(song: Song) {
        val loaded = uiState.value as? CollectionUiState.Loaded ?: return
        val outcome = songPlayback.request(
            song = song,
            nowPlaying = loaded.nowPlaying,
            queue = loaded.songs,
            context = loaded.toPlaybackContext(),
        )
        when (outcome) {
            SongPlayOutcome.AlreadyPlaying -> navigator.navigate(PlayerRoute(songId = song.id))
            SongPlayOutcome.Unavailable -> showSongUnavailableOffline()
            SongPlayOutcome.Started -> Unit
        }
    }

    /**
     * The collection the player is already on is paused and resumed, like the player's own button;
     * otherwise — or once it has played to its end — it starts over from its first song, or from any
     * of them while shuffle is on. Songs queued by hand stay queued either way.
     */
    private fun togglePlayback() {
        val loaded = uiState.value as? CollectionUiState.Loaded ?: return
        if (loaded.songs.isEmpty()) return
        val playback = observablePlayback.observePlaybackState().value
        if (playback.isOnCollection() && !playback.hasEnded) return resume(playback)
        val songs = playableSongs.findPlayableOrNull(loaded.songs) ?: return showSongUnavailableOffline()
        contextStarter.playFromStart(songs = songs, context = loaded.toPlaybackContext())
    }

    /** Pausing is always honoured; resuming a song the player cannot reach is refused. */
    private fun resume(playback: PlaybackState) {
        val song = playback.currentSong
        if (!playback.isPlaying && song != null && !playableSongs.isPlayable(song)) {
            return showSongUnavailableOffline()
        }
        transportControls.togglePlayPause()
    }

    /**
     * @return whether the queue was built on this collection. A playlist is told apart by its id
     * alone, so one renamed since it started is still the one playing.
     */
    private fun PlaybackState.isOnCollection(): Boolean = when (key) {
        CollectionKey.Favorites -> context == PlaybackContext.LikedSongs
        is CollectionKey.Playlist -> (context as? PlaybackContext.Playlist)?.id == key.playlistId
    }

    private fun CollectionUiState.Loaded.toPlaybackContext(): PlaybackContext = when (key) {
        CollectionKey.Favorites -> PlaybackContext.LikedSongs

        is CollectionKey.Playlist -> PlaybackContext.Playlist(
            id = key.playlistId,
            title = (title as? CollectionTitle.Custom)?.name.orEmpty(),
        )
    }

    private fun showSongUnavailableOffline() {
        emitAction(CollectionUiAction.ShowSnackBar(R.string.ui_song_unavailable_offline))
    }

    /** A song opened from a playlist is offered its way out of it; the liked songs have the like for that. */
    private fun openSongOptions(song: Song) {
        val playlistId = (key as? CollectionKey.Playlist)?.playlistId
        navigator.navigate(SongOptionsRoute(songId = song.id, playlistId = playlistId))
    }

    /** A song the player cannot reach never enters the queue, so it does not stall on it. */
    private fun addToQueue(song: Song) {
        if (!playableSongs.isPlayable(song)) return showSongUnavailableOffline()
        enqueuer.addToQueue(listOf(song))
        emitAction(CollectionUiAction.ShowSnackBar(R.string.ui_added_to_queue))
    }

    /** Taking the like back from the liked songs is what takes the song out of them. */
    private fun toggleFavorite(song: Song) {
        val isFavorite = (uiState.value as? CollectionUiState.Loaded)?.favoriteSongIds?.contains(song.id) ?: return
        viewModelScope.launch {
            useCases.toggleSongFavorite(song = song, isFavorite = isFavorite)
            val message = if (isFavorite) R.string.ui_removed_from_favorites else R.string.ui_added_to_favorites
            emitAction(CollectionUiAction.ShowSnackBar(message))
        }
    }
}
