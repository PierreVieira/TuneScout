package com.pierre.tunescout.feature.library.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.pierre.tunescout.core.model.PlaybackContext
import com.pierre.tunescout.core.model.PlaybackState
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.reorder.ReorderRequests
import com.pierre.tunescout.core.navigation.reorder.ReorderTarget
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
import com.pierre.tunescout.ui.utils.reorder.ListReorder
import kotlinx.coroutines.flow.Flow
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
    private val reorderRequests: ReorderRequests,
    collectionStreams: CollectionStreams,
    observablePlayableSongs: ObservablePlayableSongs,
) : ActionViewModel<CollectionUiAction>() {
    /** Only a playlist has an order of the user's own; the liked songs keep the order they were liked in. */
    private val reorderTarget: ReorderTarget.Playlist? =
        (key as? CollectionKey.Playlist)?.let { playlist -> ReorderTarget.Playlist(playlistId = playlist.playlistId) }

    private val reorder = ListReorder<Song, Long>(
        keyOf = { song -> song.id },
        scope = viewModelScope,
        persist = { songIds ->
            val playlistId = reorderTarget?.playlistId
            if (playlistId != null) useCases.reorderPlaylistSongs(playlistId = playlistId, songIds = songIds)
        },
    )

    /** The songs, in the order the user is dragging them into, and whether they are being dragged. */
    private val arrangedSongs: Flow<ArrangedSongs> = combine(
        reorder.observeArranged(collectionStreams.observeSongs(key)),
        reorder.isReordering,
        ::ArrangedSongs,
    )

    val uiState: StateFlow<CollectionUiState> = combine(
        collectionStreams.observeTitle(key),
        arrangedSongs,
        observablePlayback.observePlaybackState(),
        collectionStreams.observeFavoriteSongIds(),
        observablePlayableSongs.observePlayableSongs(),
    ) { title, arranged, playback, favoriteSongIds, playable ->
        val songs = arranged.songs
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
                isPlaying = playback.isPlaying && playback.isOnOneOf(songs),
                isShuffleEnabled = playback.isShuffleEnabled,
                isReorderable = reorderTarget != null,
                isReordering = arranged.isReordering,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = CollectionUiState.Loading,
    )

    init {
        startReorderingOnRequest()
    }

    fun onEvent(event: CollectionUiEvent) = when (event) {
        is CollectionUiEvent.OnSongClicked -> play(event.song)
        is CollectionUiEvent.OnSongOptionsClicked -> openSongOptions(event.song)
        is CollectionUiEvent.OnSongSwipedToQueue -> addToQueue(event.song)
        is CollectionUiEvent.OnSongSwipedToFavorite -> toggleFavorite(event.song)
        CollectionUiEvent.OnPlayPauseClicked -> togglePlayback()
        CollectionUiEvent.OnShuffleClicked -> transportControls.toggleShuffle()
        CollectionUiEvent.OnMoreClicked -> navigator.navigate(key.toOptionsRoute())
        CollectionUiEvent.OnReorderStarted -> startReordering()
        CollectionUiEvent.OnReorderFinished -> reorder.finish()
        is CollectionUiEvent.OnSongMoved -> moveSong(fromSongId = event.fromSongId, toSongId = event.toSongId)
        CollectionUiEvent.OnBackClicked -> goBack()
    }

    /** Back leaves the reordering first, and the collection only once the rows are back to normal. */
    private fun goBack() {
        if (reorder.isReordering.value) return reorder.finish()
        navigator.navigateBack()
    }

    private fun startReordering() {
        if (reorderTarget != null) reorder.start()
    }

    private fun moveSong(
        fromSongId: Long,
        toSongId: Long,
    ) {
        if (reorderTarget == null) return
        val songs = (uiState.value as? CollectionUiState.Loaded)?.songs ?: return
        reorder.move(items = songs, from = fromSongId, to = toSongId)
    }

    /** The song options sheet and the playlist's own ask for it, and close as they do. */
    private fun startReorderingOnRequest() {
        val target = reorderTarget ?: return
        viewModelScope.launch {
            reorderRequests.observe(target).collect { reorder.start() }
        }
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

    /**
     * A song opened from a playlist is offered its way out of it, and the playlist's reordering; the
     * liked songs have the like for the first and keep the order they were liked in.
     */
    private fun openSongOptions(song: Song) {
        val playlistId = (key as? CollectionKey.Playlist)?.playlistId
        navigator.navigate(
            SongOptionsRoute(songId = song.id, playlistId = playlistId, reorderTarget = reorderTarget),
        )
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

    /**
     * @property songs the collection's songs, in the order the user is dragging them into.
     * @property isReordering whether they are there to be dragged into a new order.
     */
    private data class ArrangedSongs(
        val songs: List<Song>,
        val isReordering: Boolean,
    )
}
