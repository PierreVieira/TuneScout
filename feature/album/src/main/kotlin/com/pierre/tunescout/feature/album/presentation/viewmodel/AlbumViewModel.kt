package com.pierre.tunescout.feature.album.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.pierre.tunescout.core.model.Album
import com.pierre.tunescout.core.model.CollectionDownloadState
import com.pierre.tunescout.core.model.LibraryItemKey
import com.pierre.tunescout.core.model.PlaybackContext
import com.pierre.tunescout.core.model.PlaybackState
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.model.SongDownloadStatus
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.reorder.ReorderRequests
import com.pierre.tunescout.core.navigation.reorder.ReorderTarget
import com.pierre.tunescout.core.navigation.route.AlbumOptionsRoute
import com.pierre.tunescout.core.navigation.route.AlbumRoute
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
import com.pierre.tunescout.feature.album.domain.usecase.AlbumUseCases
import com.pierre.tunescout.feature.album.presentation.model.AlbumUiAction
import com.pierre.tunescout.feature.album.presentation.model.AlbumUiEvent
import com.pierre.tunescout.feature.album.presentation.model.AlbumUiState
import com.pierre.tunescout.ui.component.R
import com.pierre.tunescout.ui.utils.ActionViewModel
import com.pierre.tunescout.ui.utils.reorder.ListReorder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AlbumViewModel(
    private val route: AlbumRoute,
    private val useCases: AlbumUseCases,
    private val songPlayback: SongPlayback,
    private val contextStarter: ContextStarter,
    private val enqueuer: Enqueuer,
    private val transportControls: TransportControls,
    private val playableSongs: PlayableSongs,
    private val navigator: Navigator,
    private val observablePlayback: ObservablePlayback,
    private val reorderRequests: ReorderRequests,
    observablePlayableSongs: ObservablePlayableSongs,
    observableDownloads: ObservableDownloads,
) : ActionViewModel<AlbumUiAction>() {
    private val reorderTarget = ReorderTarget.Album(albumId = route.albumId)

    private val downloadKey = LibraryItemKey.Album(albumId = route.albumId)

    /** How far each song has got, and whether the user asked for the album as a whole. */
    private val downloads: Flow<AlbumDownloads> = combine(
        observableDownloads.observeDownloadStatuses(),
        useCases.observeCollectionDownloads(),
    ) { statuses, collections -> AlbumDownloads(statuses = statuses, isRequested = downloadKey in collections) }

    private val reorder = ListReorder<Song, Long>(
        keyOf = { song -> song.id },
        scope = viewModelScope,
        persist = { songIds -> useCases.saveTrackOrder(albumId = route.albumId, songIds = songIds) },
    )

    private val refreshFailed = MutableStateFlow(false)

    private val songAlreadyQueued = MutableStateFlow<Song?>(null)

    /** The dialog asking about a song already queued is read with the player, which answers it. */
    private val queueing: Flow<Queueing> = combine(
        observablePlayback.observePlaybackState(),
        songAlreadyQueued,
        ::Queueing,
    )

    /**
     * Whether an album put together from the saved tracks may be drawn. Not before the first refresh
     * fails: online, the whole album is a moment away, and one track flashing before it would be
     * noise. Never reset afterwards, so asking again once the connection is back keeps those tracks
     * on screen instead of dropping to the skeleton.
     */
    private val showsPartialAlbum = MutableStateFlow(false)

    /**
     * The album, the reach the player has over its tracks, which of them are liked and where their
     * downloads stand arrive as one, so the rows are never drawn against the reach of a connection
     * the monitor has already replaced. Its tracks come in the order the user is dragging them into,
     * so the play button follows it too.
     */
    private val albumReach: Flow<AlbumReach> = combine(
        reorder.observeArranged(useCases.observeAlbum(route.albumId)) { album, arrange ->
            album?.copy(songs = arrange(album.songs))
        },
        observablePlayableSongs.observePlayableSongs(),
        useCases.observeFavoriteSongIds(),
        reorder.isReordering,
        downloads,
        ::AlbumReach,
    )

    val uiState: StateFlow<AlbumUiState> = combine(
        albumReach,
        queueing,
        refreshFailed,
        showsPartialAlbum,
        useCases.isAlbumFavorite(route.albumId),
        ::toUiState,
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(), AlbumUiState.Loading)

    /**
     * Started optimistically, like the search screen: the monitor reports the real state as soon as
     * it is collected, and a tap in the meantime is better sent to the player than refused.
     */
    private val isOnline: StateFlow<Boolean> = useCases
        .observeIsOnline()
        .distinctUntilChanged()
        .stateIn(scope = viewModelScope, started = SharingStarted.Eagerly, initialValue = true)

    /** Emits every time the connection comes back, and never for the state the screen opened on. */
    private val reconnections: Flow<Boolean>
        get() = isOnline.drop(1).filter { isOnline -> isOnline }

    init {
        refresh()
        retryOnReconnection()
        startReorderingOnRequest()
    }

    fun onEvent(event: AlbumUiEvent) = when (event) {
        is AlbumUiEvent.OnSongClicked -> play(event.song)
        is AlbumUiEvent.OnSongOptionsClicked -> openSongOptions(event.song)
        is AlbumUiEvent.OnSongSwipedToQueue -> addToQueue(event.song)
        AlbumUiEvent.OnDuplicateInQueueConfirmed -> addToQueueAgain()
        AlbumUiEvent.OnDuplicateInQueueDismissed -> songAlreadyQueued.value = null
        is AlbumUiEvent.OnSongSwipedToFavorite -> toggleSongFavorite(event.song)
        AlbumUiEvent.OnPlayPauseClicked -> togglePlayback()
        AlbumUiEvent.OnShuffleClicked -> transportControls.toggleShuffle()
        AlbumUiEvent.OnFavoriteClicked -> toggleFavorite()
        AlbumUiEvent.OnDownloadClicked -> toggleDownload()
        AlbumUiEvent.OnMoreClicked -> navigator.navigate(AlbumOptionsRoute(albumId = route.albumId))
        AlbumUiEvent.OnRetryClicked -> refresh()
        AlbumUiEvent.OnReorderStarted -> reorder.start()
        AlbumUiEvent.OnReorderFinished -> reorder.finish()
        is AlbumUiEvent.OnSongMoved -> moveSong(fromSongId = event.fromSongId, toSongId = event.toSongId)
        AlbumUiEvent.OnBackClicked -> goBack()
    }

    /** Back leaves the reordering first, and the album only once the rows are back to normal. */
    private fun goBack() {
        if (reorder.isReordering.value) return reorder.finish()
        navigator.navigateBack()
    }

    private fun openSongOptions(song: Song) {
        navigator.navigate(SongOptionsRoute(songId = song.id, reorderTarget = reorderTarget))
    }

    private fun moveSong(
        fromSongId: Long,
        toSongId: Long,
    ) {
        val songs = (uiState.value as? AlbumUiState.Loaded)?.album?.songs ?: return
        reorder.move(items = songs, from = fromSongId, to = toSongId)
    }

    /** The song options sheet and the album's own ask for it, and close as they do. */
    private fun startReorderingOnRequest() {
        viewModelScope.launch {
            reorderRequests.observe(reorderTarget).collect { reorder.start() }
        }
    }

    private fun refresh() {
        refreshFailed.value = false
        viewModelScope.launch {
            useCases.refreshAlbum(route.albumId).onFailure {
                refreshFailed.value = true
                showsPartialAlbum.value = true
            }
        }
    }

    /**
     * A refresh that failed — the tracks saved on the device, the cached album marked stale, or the
     * error — is asked again as soon as the connection is back, so the whole album replaces what
     * the device had without the user having to tap anything.
     */
    private fun retryOnReconnection() {
        viewModelScope.launch {
            reconnections.collect {
                if (refreshFailed.value) refresh()
            }
        }
    }

    /**
     * The album the player is already on is paused and resumed, like the player's own button; any
     * other album — or this one once it has played to its end — starts over from its first song, or
     * from any of them while shuffle is on. Songs queued by hand stay queued either way.
     */
    private fun togglePlayback() {
        val album = (uiState.value as? AlbumUiState.Loaded)?.album ?: return
        val playback = observablePlayback.observePlaybackState().value
        if (playback.isOnAlbum(album.id) && !playback.hasEnded) return resume(playback)
        val songs = playableSongs.findPlayableOrNull(album.songs) ?: return showSongUnavailableOffline()
        contextStarter.playFromStart(
            songs = songs,
            context = PlaybackContext.Album(id = album.id, title = album.title),
        )
    }

    /** Pausing is always honoured; resuming a song the player cannot reach is refused. */
    private fun resume(playback: PlaybackState) {
        val song = playback.currentSong
        if (!playback.isPlaying && song != null && !playableSongs.isPlayable(song)) {
            return showSongUnavailableOffline()
        }
        transportControls.togglePlayPause()
    }

    private fun toggleFavorite() {
        val state = uiState.value as? AlbumUiState.Loaded ?: return
        if (!state.album.isComplete) return
        viewModelScope.launch {
            useCases.toggleAlbumFavorite(album = state.album, isFavorite = state.isFavorite)
        }
    }

    private fun toggleDownload() {
        val state = uiState.value as? AlbumUiState.Loaded ?: return
        viewModelScope.launch {
            useCases.toggleAlbumDownload(
                album = state.album,
                isDownloaded = state.download != CollectionDownloadState.NotDownloaded,
                isFavorite = state.isFavorite,
            )
        }
    }

    /**
     * A song the player cannot reach never enters the queue, so it does not stall on it. One the
     * user already queued only goes in again once they confirm it.
     */
    private fun addToQueue(song: Song) {
        if (!playableSongs.isPlayable(song)) return showSongUnavailableOffline()
        if (observablePlayback.observePlaybackState().value.isQueuedByUser(song.id)) {
            songAlreadyQueued.value = song
        } else {
            enqueue(song)
        }
    }

    private fun addToQueueAgain() {
        val song = songAlreadyQueued.value ?: return
        songAlreadyQueued.value = null
        enqueue(song)
    }

    private fun enqueue(song: Song) {
        enqueuer.addToQueue(listOf(song))
        emitAction(AlbumUiAction.ShowSnackBar(R.string.ui_added_to_queue))
    }

    private fun toggleSongFavorite(song: Song) {
        val isFavorite = (uiState.value as? AlbumUiState.Loaded)?.favoriteSongIds?.contains(song.id) ?: return
        viewModelScope.launch {
            useCases.toggleSongFavorite(song = song, isFavorite = isFavorite)
            val message = if (isFavorite) R.string.ui_removed_from_favorites else R.string.ui_added_to_favorites
            emitAction(AlbumUiAction.ShowSnackBar(message))
        }
    }

    private fun play(song: Song) {
        val loaded = uiState.value as? AlbumUiState.Loaded ?: return
        val album = loaded.album
        val outcome = songPlayback.request(
            song = song,
            nowPlaying = loaded.nowPlaying,
            queue = album.songs,
            context = PlaybackContext.Album(id = album.id, title = album.title),
        )
        when (outcome) {
            SongPlayOutcome.AlreadyPlaying -> navigator.navigate(PlayerRoute(songId = song.id))
            SongPlayOutcome.Unavailable -> showSongUnavailableOffline()
            SongPlayOutcome.Started -> Unit
        }
    }

    private fun showSongUnavailableOffline() {
        emitAction(AlbumUiAction.ShowSnackBar(R.string.ui_song_unavailable_offline))
    }

    private fun toUiState(
        reach: AlbumReach,
        queueing: Queueing,
        refreshFailed: Boolean,
        showsPartialAlbum: Boolean,
        isFavorite: Boolean,
    ): AlbumUiState {
        val album = reach.album
        val playback = queueing.playback
        return when {
            album != null && (album.isComplete || showsPartialAlbum) -> AlbumUiState.Loaded(
                album = album,
                nowPlaying = playback.nowPlaying,
                isFavorite = isFavorite,
                isStale = refreshFailed,
                unplayableSongIds = reach.playableSongs.findUnplayableIds(album.songs),
                favoriteSongIds = reach.favoriteSongIds,
                isPlaying = playback.isPlaying && playback.isOnAlbum(album.id),
                isShuffleEnabled = playback.isShuffleEnabled,
                isReordering = reach.isReordering,
                download = CollectionDownloadState.of(
                    isRequested = reach.downloads.isRequested,
                    songIds = album.songs.map(Song::id),
                    statuses = reach.downloads.statuses,
                ),
                downloadStatuses = reach.downloads.statuses,
                songAlreadyQueued = queueing.songAlreadyQueued,
            )

            refreshFailed -> AlbumUiState.Error

            else -> AlbumUiState.Loading
        }
    }

    private fun PlaybackState.isOnAlbum(albumId: Long): Boolean = (context as? PlaybackContext.Album)?.id == albumId

    /**
     * @property playback what the player holds.
     * @property songAlreadyQueued the song swiped into the queue while the user already had it
     * queued, while the screen asks whether to add it again.
     */
    private data class Queueing(
        val playback: PlaybackState,
        val songAlreadyQueued: Song?,
    )

    /**
     * @property album the album as the device has it, or null before it has one.
     * @property playableSongs which of its tracks the player can reach with the connection it has.
     * @property favoriteSongIds the songs the user liked, from this album or any other.
     * @property isReordering whether the rows are there to be dragged into a new order.
     * @property downloads where the downloads of the album and of its tracks stand.
     */
    private data class AlbumReach(
        val album: Album?,
        val playableSongs: PlayableSongs,
        val favoriteSongIds: Set<Long>,
        val isReordering: Boolean,
        val downloads: AlbumDownloads,
    )

    /**
     * @property statuses how far each song the user asked to keep has got, from this album or any other.
     * @property isRequested whether the user asked for the album as a whole.
     */
    private data class AlbumDownloads(
        val statuses: Map<Long, SongDownloadStatus>,
        val isRequested: Boolean,
    )
}
