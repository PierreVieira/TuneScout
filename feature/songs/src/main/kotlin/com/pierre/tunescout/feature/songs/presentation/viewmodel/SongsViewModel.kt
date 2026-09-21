package com.pierre.tunescout.feature.songs.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.pierre.tunescout.core.audiosearch.AudioSearchAvailability
import com.pierre.tunescout.core.audiosearch.ObservableAudioSearchQueries
import com.pierre.tunescout.core.model.PlaybackContext
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.route.AudioSearchRoute
import com.pierre.tunescout.core.navigation.route.PlayerRoute
import com.pierre.tunescout.core.navigation.route.SongOptionsRoute
import com.pierre.tunescout.core.navigation.route.ThemeSelectionRoute
import com.pierre.tunescout.core.playback.Enqueuer
import com.pierre.tunescout.core.playback.ObservablePlayableSongs
import com.pierre.tunescout.core.playback.ObservablePlayback
import com.pierre.tunescout.core.playback.PlayableSongs
import com.pierre.tunescout.core.playback.SongPlayOutcome
import com.pierre.tunescout.core.playback.SongPlayback
import com.pierre.tunescout.feature.songs.R
import com.pierre.tunescout.feature.songs.domain.usecase.SongsUseCases
import com.pierre.tunescout.feature.songs.presentation.model.SearchResultUiModel
import com.pierre.tunescout.feature.songs.presentation.model.SongsUiAction
import com.pierre.tunescout.feature.songs.presentation.model.SongsUiEvent
import com.pierre.tunescout.feature.songs.presentation.model.SongsUiState
import com.pierre.tunescout.ui.utils.ActionViewModel
import com.pierre.tunescout.ui.utils.permission.PermissionResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import com.pierre.tunescout.ui.component.R as ComponentR

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SongsViewModel(
    private val useCases: SongsUseCases,
    private val songPlayback: SongPlayback,
    private val enqueuer: Enqueuer,
    private val playableSongs: PlayableSongs,
    private val navigator: Navigator,
    observablePlayback: ObservablePlayback,
    observablePlayableSongs: ObservablePlayableSongs,
    audioSearchAvailability: AudioSearchAvailability,
    audioSearchQueries: ObservableAudioSearchQueries,
) : ActionViewModel<SongsUiAction>() {
    private val searchDebounce = 300.milliseconds
    private val idleLoadStates = LoadStates(
        refresh = LoadState.NotLoading(endOfPaginationReached = true),
        prepend = LoadState.NotLoading(endOfPaginationReached = true),
        append = LoadState.NotLoading(endOfPaginationReached = true),
    )
    private val isAudioSearchAvailable = audioSearchAvailability.isAvailable()
    private val query = MutableStateFlow("")
    private val songPendingRemoval = MutableStateFlow<Song?>(null)

    /**
     * Started optimistically: the monitor reports the real state as soon as something collects it,
     * and a banner that blinks "offline" on every launch would be worse than one frame of silence.
     */
    private val isOnline: StateFlow<Boolean> = useCases
        .observeIsOnline()
        .distinctUntilChanged()
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(), initialValue = true)

    /**
     * The player's reach, re-answered every time the connection comes or goes, so a list on screen
     * redraws instead of keeping the answer it was built with.
     */
    private val playableSongsNow: Flow<PlayableSongs> = observablePlayableSongs.observePlayableSongs()

    /**
     * The connection and the reach it gives the player change at the same moment, and the state
     * reads one for the banner and the other for the rows, so they arrive as one.
     */
    private val connection: Flow<Connection> = combine(isOnline, playableSongsNow, ::Connection)

    /** What the user kept: the songs played last and the ones liked, which the rows read together. */
    private val savedSongs: Flow<SavedSongs> = combine(
        useCases.observeRecentlyPlayed(),
        useCases.observeFavoriteSongIds(),
        ::SavedSongs,
    )

    val uiState: StateFlow<SongsUiState> = combine(
        query,
        savedSongs,
        observablePlayback.observePlaybackState(),
        songPendingRemoval,
        connection,
    ) { query, saved, playback, pendingRemoval, connection ->
        val recentlyPlayed = saved.recentlyPlayed
        SongsUiState(
            query = query,
            isAudioSearchAvailable = isAudioSearchAvailable,
            recentlyPlayed = recentlyPlayed,
            nowPlaying = playback.nowPlaying,
            songPendingRemoval = pendingRemoval,
            favoriteSongIds = saved.favoriteSongIds,
            isOffline = !connection.isOnline,
            unplayableSongIds = connection.playableSongs.findUnplayableIds(recentlyPlayed),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = SongsUiState(
            query = "",
            isAudioSearchAvailable = isAudioSearchAvailable,
            recentlyPlayed = emptyList(),
            nowPlaying = null,
            songPendingRemoval = null,
            favoriteSongIds = emptySet(),
            isOffline = false,
            unplayableSongIds = emptySet(),
        ),
    )

    /** Emits every time the connection comes back, and never for the state the screen opened on. */
    private val reconnections: Flow<Unit>
        get() = isOnline.drop(1).filter { isOnline -> isOnline }.map { }

    /**
     * The search restarts on the term the user typed and every time the connection comes back, so
     * results the cache answered with while the device was offline are replaced by the catalog's
     * own without anyone having to pull to refresh.
     */
    val searchResults: Flow<PagingData<SearchResultUiModel>> = combine(
        query.debounce(searchDebounce).map { query -> query.trim() }.distinctUntilChanged(),
        reconnections.onStart { emit(Unit) },
    ) { term, _ -> term }
        .flatMapLatest { term ->
            if (term.isBlank()) flowOf(PagingData.empty(idleLoadStates)) else useCases.searchSongs(term)
        }.cachedIn(viewModelScope)
        .combine(playableSongsNow) { results, playable ->
            results.map { song -> SearchResultUiModel(song = song, isUnavailable = !playable.isPlayable(song)) }
        }

    init {
        searchSpokenQueries(audioSearchQueries)
    }

    fun onEvent(event: SongsUiEvent) = when (event) {
        is SongsUiEvent.OnQueryChanged -> query.value = event.query
        SongsUiEvent.OnClearQueryClicked -> query.value = ""
        SongsUiEvent.OnAudioSearchClicked -> emitAction(SongsUiAction.RequestMicrophonePermission)
        is SongsUiEvent.OnMicrophonePermissionResult -> handleMicrophonePermission(event.result)
        SongsUiEvent.OnThemeClicked -> navigator.navigate(ThemeSelectionRoute)
        is SongsUiEvent.OnSongClicked -> play(event.song)
        is SongsUiEvent.OnSongOptionsClicked -> navigator.navigate(SongOptionsRoute(songId = event.song.id))
        is SongsUiEvent.OnSongSwipedToQueue -> addToQueue(event.song)
        is SongsUiEvent.OnSongSwipedToFavorite -> toggleFavorite(event.song)
        is SongsUiEvent.OnRemoveRecentClicked -> songPendingRemoval.value = event.song
        SongsUiEvent.OnRemoveRecentConfirmed -> removeFromRecentlyPlayed()
        SongsUiEvent.OnRemoveRecentDismissed -> songPendingRemoval.value = null
    }

    /**
     * A spoken query lands in the same field a typed one does, so the search it starts, the clear
     * button and the debounce are the ones the keyboard already had.
     */
    private fun searchSpokenQueries(audioSearchQueries: ObservableAudioSearchQueries) {
        audioSearchQueries
            .observeQueries()
            .onEach { spokenQuery -> query.value = spokenQuery }
            .launchIn(viewModelScope)
    }

    /** The sheet opens the microphone as it opens, so it is only reached with the permission in hand. */
    private fun handleMicrophonePermission(result: PermissionResult) {
        when (result) {
            PermissionResult.Granted -> navigator.navigate(AudioSearchRoute)
            PermissionResult.Denied -> emitAction(SongsUiAction.ShowSnackBar(R.string.songs_microphone_denied))
            PermissionResult.PermanentlyDenied -> emitAction(SongsUiAction.ShowMicrophoneSettingsSnackBar)
        }
    }

    private fun play(song: Song) {
        val outcome = songPlayback.request(
            song = song,
            nowPlaying = uiState.value.nowPlaying,
            queue = listOf(song),
            context = PlaybackContext.SingleSong,
        )
        when (outcome) {
            SongPlayOutcome.AlreadyPlaying -> navigator.navigate(PlayerRoute(songId = song.id))
            SongPlayOutcome.Unavailable -> showSongUnavailableOffline()
            SongPlayOutcome.Started -> Unit
        }
    }

    private fun showSongUnavailableOffline() {
        emitAction(SongsUiAction.ShowSnackBar(ComponentR.string.ui_song_unavailable_offline))
    }

    /** A song the player cannot reach never enters the queue, so it does not stall on it. */
    private fun addToQueue(song: Song) {
        if (!playableSongs.isPlayable(song)) return showSongUnavailableOffline()
        enqueuer.addToQueue(listOf(song))
        emitAction(SongsUiAction.ShowSnackBar(ComponentR.string.ui_added_to_queue))
    }

    private fun toggleFavorite(song: Song) {
        val isFavorite = song.id in uiState.value.favoriteSongIds
        viewModelScope.launch {
            useCases.toggleSongFavorite(song = song, isFavorite = isFavorite)
            val message = if (isFavorite) {
                ComponentR.string.ui_removed_from_favorites
            } else {
                ComponentR.string.ui_added_to_favorites
            }
            emitAction(SongsUiAction.ShowSnackBar(message))
        }
    }

    private fun removeFromRecentlyPlayed() {
        val song = songPendingRemoval.value ?: return
        songPendingRemoval.value = null
        viewModelScope.launch { useCases.removeFromRecentlyPlayed(song.id) }
    }

    /**
     * @property recentlyPlayed the songs played last, newest first.
     * @property favoriteSongIds the songs the user liked.
     */
    private data class SavedSongs(
        val recentlyPlayed: List<Song>,
        val favoriteSongIds: Set<Long>,
    )

    /**
     * @property isOnline whether the device has a connection right now.
     * @property playableSongs what the player can reach with that connection.
     */
    private data class Connection(
        val isOnline: Boolean,
        val playableSongs: PlayableSongs,
    )
}
