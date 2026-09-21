package com.pierre.tunescout.feature.songs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.pierre.tunescout.core.model.PlaybackContext
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.model.isOn
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.route.PlayerRoute
import com.pierre.tunescout.core.navigation.route.SongOptionsRoute
import com.pierre.tunescout.core.navigation.route.ThemeSelectionRoute
import com.pierre.tunescout.core.playback.ObservablePlayableSongs
import com.pierre.tunescout.core.playback.ObservablePlayback
import com.pierre.tunescout.core.playback.PlayableSongs
import com.pierre.tunescout.core.playback.PlaybackStarter
import com.pierre.tunescout.feature.songs.domain.usecase.SongsUseCases
import com.pierre.tunescout.feature.songs.presentation.model.SearchResultUiModel
import com.pierre.tunescout.feature.songs.presentation.model.SongsUiAction
import com.pierre.tunescout.feature.songs.presentation.model.SongsUiEvent
import com.pierre.tunescout.feature.songs.presentation.model.SongsUiState
import com.pierre.tunescout.ui.component.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SongsViewModel(
    private val useCases: SongsUseCases,
    private val playbackStarter: PlaybackStarter,
    private val playableSongs: PlayableSongs,
    private val navigator: Navigator,
    observablePlayback: ObservablePlayback,
    observablePlayableSongs: ObservablePlayableSongs,
) : ViewModel() {
    private val searchDebounce = 300.milliseconds
    private val idleLoadStates = LoadStates(
        refresh = LoadState.NotLoading(endOfPaginationReached = true),
        prepend = LoadState.NotLoading(endOfPaginationReached = true),
        append = LoadState.NotLoading(endOfPaginationReached = true),
    )
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

    val uiState: StateFlow<SongsUiState> = combine(
        query,
        useCases.observeRecentlyPlayed(),
        observablePlayback.observePlaybackState(),
        songPendingRemoval,
        connection,
    ) { query, recentlyPlayed, playback, pendingRemoval, connection ->
        SongsUiState(
            query = query,
            recentlyPlayed = recentlyPlayed,
            nowPlaying = playback.nowPlaying,
            songPendingRemoval = pendingRemoval,
            isOffline = !connection.isOnline,
            unplayableSongIds = connection.playableSongs.findUnplayableIds(recentlyPlayed),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = SongsUiState(
            query = "",
            recentlyPlayed = emptyList(),
            nowPlaying = null,
            songPendingRemoval = null,
            isOffline = false,
            unplayableSongIds = emptySet(),
        ),
    )

    val uiAction: SharedFlow<SongsUiAction>
        field = MutableSharedFlow<SongsUiAction>()

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

    fun onEvent(event: SongsUiEvent) = when (event) {
        is SongsUiEvent.OnQueryChanged -> query.value = event.query
        SongsUiEvent.OnClearQueryClicked -> query.value = ""
        SongsUiEvent.OnThemeClicked -> navigator.navigate(ThemeSelectionRoute)
        is SongsUiEvent.OnSongClicked -> play(event.song)
        is SongsUiEvent.OnSongOptionsClicked -> navigator.navigate(SongOptionsRoute(songId = event.song.id))
        is SongsUiEvent.OnRecentSongSwipedAway -> songPendingRemoval.value = event.song
        SongsUiEvent.OnRemoveRecentConfirmed -> removeFromRecentlyPlayed()
        SongsUiEvent.OnRemoveRecentDismissed -> songPendingRemoval.value = null
    }

    /**
     * The song the player is already on opens the player instead of starting over: a tap on the row
     * marked as the one playing means "take me there", not "play it again from the beginning".
     */
    private fun play(song: Song) {
        if (uiState.value.nowPlaying.isOn(song.id)) return openPlayer(song.id)
        if (!playableSongs.isPlayable(song)) return showSongUnavailableOffline()
        playbackStarter.play(song = song, songs = listOf(song), context = PlaybackContext.SingleSong)
    }

    private fun openPlayer(songId: Long) {
        navigator.navigate(PlayerRoute(songId = songId))
    }

    private fun showSongUnavailableOffline() {
        emitAction(SongsUiAction.ShowSnackBar(R.string.ui_song_unavailable_offline))
    }

    private fun emitAction(action: SongsUiAction) {
        viewModelScope.launch { uiAction.emit(action) }
    }

    private fun removeFromRecentlyPlayed() {
        val song = songPendingRemoval.value ?: return
        songPendingRemoval.value = null
        viewModelScope.launch { useCases.removeFromRecentlyPlayed(song.id) }
    }

    /**
     * @property isOnline whether the device has a connection right now.
     * @property playableSongs what the player can reach with that connection.
     */
    private data class Connection(
        val isOnline: Boolean,
        val playableSongs: PlayableSongs,
    )
}
