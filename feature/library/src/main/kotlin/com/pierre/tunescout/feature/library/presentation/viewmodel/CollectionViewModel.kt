package com.pierre.tunescout.feature.library.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.pierre.tunescout.core.model.CollectionDownloadState
import com.pierre.tunescout.core.model.PlaybackContext
import com.pierre.tunescout.core.model.PlaybackState
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.model.SongDownloadStatus
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.reorder.ReorderRequests
import com.pierre.tunescout.core.navigation.reorder.ReorderTarget
import com.pierre.tunescout.core.navigation.route.PlayerRoute
import com.pierre.tunescout.core.navigation.route.SongOptionsRoute
import com.pierre.tunescout.core.playback.ContextStarter
import com.pierre.tunescout.core.playback.Enqueuer
import com.pierre.tunescout.core.playback.ObservableDownloads
import com.pierre.tunescout.core.playback.ObservablePlayableSongs
import com.pierre.tunescout.core.playback.ObservablePlayback
import com.pierre.tunescout.core.playback.PlayableSongs
import com.pierre.tunescout.core.playback.SongPlayOutcome
import com.pierre.tunescout.core.playback.SongPlayback
import com.pierre.tunescout.core.playback.TransportControls
import com.pierre.tunescout.feature.library.domain.model.CollectionKey
import com.pierre.tunescout.feature.library.domain.usecase.CollectionUseCases
import com.pierre.tunescout.feature.library.presentation.mapper.CollectionStreams
import com.pierre.tunescout.feature.library.presentation.mapper.toLibraryItemKey
import com.pierre.tunescout.feature.library.presentation.mapper.toOptionsRoute
import com.pierre.tunescout.feature.library.presentation.model.CollectionTitle
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
    private val contextStarter: ContextStarter,
    private val playableSongs: PlayableSongs,
    private val enqueuer: Enqueuer,
    private val transportControls: TransportControls,
    private val navigator: Navigator,
    private val observablePlayback: ObservablePlayback,
    private val reorderRequests: ReorderRequests,
    collectionStreams: CollectionStreams,
    observablePlayableSongs: ObservablePlayableSongs,
    observableDownloads: ObservableDownloads,
) : ActionViewModel<CollectionUiAction>() {
    private val downloadKey = key.toLibraryItemKey()

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

    /**
     * The songs, in the order the user is dragging them into, whether they are being dragged, and
     * where their downloads stand.
     */
    private val arrangedSongs: Flow<ArrangedSongs> = combine(
        reorder.observeArranged(collectionStreams.observeSongs(key)),
        reorder.isReordering,
        observableDownloads.observeDownloadStatuses(),
        useCases.observeCollectionDownloads(),
    ) { songs, isReordering, statuses, collections ->
        ArrangedSongs(
            songs = songs,
            isReordering = isReordering,
            downloadStatuses = statuses,
            isDownloadRequested = downloadKey in collections,
        )
    }

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
                isDownloadable = key != CollectionKey.DownloadedSongs,
                favoriteSongIds = favoriteSongIds,
                unplayableSongIds = playable.findUnplayableIds(songs),
                isPlaying = playback.isPlaying && playback.isOnCollection(),
                isShuffleEnabled = playback.isShuffleEnabled,
                isReorderable = reorderTarget != null,
                isReordering = arranged.isReordering,
                download = CollectionDownloadState.of(
                    isRequested = arranged.isDownloadRequested,
                    songIds = songs.map(Song::id),
                    statuses = arranged.downloadStatuses,
                ),
                downloadStatuses = arranged.downloadStatuses,
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
        CollectionUiEvent.OnDownloadClicked -> toggleDownload()
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

    private fun toggleDownload() {
        val loaded = uiState.value as? CollectionUiState.Loaded ?: return
        if (!loaded.isDownloadable) return
        viewModelScope.launch {
            useCases.toggleCollectionDownload(
                key = downloadKey,
                isDownloaded = loaded.download != CollectionDownloadState.NotDownloaded,
            )
        }
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
        CollectionKey.DownloadedSongs -> context == PlaybackContext.DownloadedSongs
    }

    private fun CollectionUiState.Loaded.toPlaybackContext(): PlaybackContext = when (key) {
        CollectionKey.Favorites -> PlaybackContext.LikedSongs

        is CollectionKey.Playlist -> PlaybackContext.Playlist(
            id = key.playlistId,
            title = (title as? CollectionTitle.Custom)?.name.orEmpty(),
        )

        CollectionKey.DownloadedSongs -> PlaybackContext.DownloadedSongs
    }

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
     * @property downloadStatuses how far each song the user asked to keep has got.
     * @property isDownloadRequested whether the user asked for the collection as a whole.
     */
    private data class ArrangedSongs(
        val songs: List<Song>,
        val isReordering: Boolean,
        val downloadStatuses: Map<Long, SongDownloadStatus>,
        val isDownloadRequested: Boolean,
    )
}
