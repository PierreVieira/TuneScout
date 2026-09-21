package com.pierre.tunescout.feature.songs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.pierre.tunescout.core.model.PlaybackContext
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.route.SongOptionsRoute
import com.pierre.tunescout.core.navigation.route.ThemeSelectionRoute
import com.pierre.tunescout.core.playback.ObservablePlayback
import com.pierre.tunescout.core.playback.PlayableSongs
import com.pierre.tunescout.core.playback.PlaybackStarter
import com.pierre.tunescout.feature.songs.domain.usecase.SongsUseCases
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

    val uiState: StateFlow<SongsUiState> = combine(
        query,
        useCases.observeRecentlyPlayed(),
        observablePlayback.observePlaybackState(),
        songPendingRemoval,
        isOnline,
    ) { query, recentlyPlayed, playback, pendingRemoval, isOnline ->
        SongsUiState(
            query = query,
            recentlyPlayed = recentlyPlayed,
            nowPlaying = playback.nowPlaying,
            songPendingRemoval = pendingRemoval,
            isOffline = !isOnline,
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
    val searchResults: Flow<PagingData<Song>> = combine(
        query.debounce(searchDebounce).map { query -> query.trim() }.distinctUntilChanged(),
        reconnections.onStart { emit(Unit) },
    ) { term, _ -> term }
        .flatMapLatest { term ->
            if (term.isBlank()) flowOf(PagingData.empty(idleLoadStates)) else useCases.searchSongs(term)
        }.cachedIn(viewModelScope)

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

    private fun play(song: Song) {
        if (!playableSongs.isPlayable(song)) return showSongUnavailableOffline()
        playbackStarter.play(song = song, songs = listOf(song), context = PlaybackContext.SingleSong)
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
}
