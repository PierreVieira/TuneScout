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
import com.pierre.tunescout.core.navigation.route.PlayerRoute
import com.pierre.tunescout.core.navigation.route.SongOptionsRoute
import com.pierre.tunescout.core.playback.Enqueuer
import com.pierre.tunescout.core.playback.ObservablePlayableSongs
import com.pierre.tunescout.core.playback.ObservablePlayback
import com.pierre.tunescout.core.playback.PlayableSongs
import com.pierre.tunescout.core.playback.SongPlayOutcome
import com.pierre.tunescout.core.playback.SongPlayback
import com.pierre.tunescout.feature.album.domain.usecase.AlbumUseCases
import com.pierre.tunescout.feature.album.presentation.model.AlbumUiAction
import com.pierre.tunescout.feature.album.presentation.model.AlbumUiEvent
import com.pierre.tunescout.feature.album.presentation.model.AlbumUiState
import com.pierre.tunescout.ui.component.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
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
    private val enqueuer: Enqueuer,
    private val playableSongs: PlayableSongs,
    private val navigator: Navigator,
    observablePlayback: ObservablePlayback,
    observablePlayableSongs: ObservablePlayableSongs,
) : ViewModel() {
    private val refreshFailed = MutableStateFlow(false)

    /**
     * Whether an album put together from the saved tracks may be drawn. Not before the first refresh
     * fails: online, the whole album is a moment away, and one track flashing before it would be
     * noise. Never reset afterwards, so asking again once the connection is back keeps those tracks
     * on screen instead of dropping to the skeleton.
     */
    private val showsPartialAlbum = MutableStateFlow(false)

    /**
     * The album and the reach the player has over its tracks arrive as one, so the rows are never
     * drawn against the reach of a connection the monitor has already replaced.
     */
    private val albumReach: Flow<AlbumReach> = combine(
        useCases.observeAlbum(route.albumId),
        observablePlayableSongs.observePlayableSongs(),
        ::AlbumReach,
    )

    val uiState: StateFlow<AlbumUiState> = combine(
        albumReach,
        observablePlayback.observePlaybackState(),
        refreshFailed,
        showsPartialAlbum,
        useCases.isAlbumFavorite(route.albumId),
        ::toUiState,
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(), AlbumUiState.Loading)

    val uiAction: SharedFlow<AlbumUiAction>
        field = MutableSharedFlow<AlbumUiAction>()

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

    private fun playNow() {
        val album = (uiState.value as? AlbumUiState.Loaded)?.album ?: return
        val songs = playableSongs.findPlayableOrNull(album.songs) ?: return showSongUnavailableOffline()
        enqueuer.playNow(songs)
    }

    private fun toggleFavorite() {
        val state = uiState.value as? AlbumUiState.Loaded ?: return
        if (!state.album.isComplete) return
        viewModelScope.launch {
            useCases.toggleAlbumFavorite(album = state.album, isFavorite = state.isFavorite)
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

    private fun emitAction(action: AlbumUiAction) {
        viewModelScope.launch { uiAction.emit(action) }
    }

    private fun toUiState(
        reach: AlbumReach,
        playback: PlaybackState,
        refreshFailed: Boolean,
        showsPartialAlbum: Boolean,
        isFavorite: Boolean,
    ): AlbumUiState {
        val album = reach.album
        return when {
            album != null && (album.isComplete || showsPartialAlbum) -> AlbumUiState.Loaded(
                album = album,
                nowPlaying = playback.nowPlaying,
                isFavorite = isFavorite,
                isStale = refreshFailed,
                unplayableSongIds = reach.playableSongs.findUnplayableIds(album.songs),
            )

            refreshFailed -> AlbumUiState.Error

            else -> AlbumUiState.Loading
        }
    }

    /**
     * @property album the album as the device has it, or null before it has one.
     * @property playableSongs which of its tracks the player can reach with the connection it has.
     */
    private data class AlbumReach(
        val album: Album?,
        val playableSongs: PlayableSongs,
    )
}
