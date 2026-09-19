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
import com.pierre.tunescout.core.navigation.route.PlayerRoute
import com.pierre.tunescout.core.navigation.route.SongOptionsRoute
import com.pierre.tunescout.core.playback.PlaybackController
import com.pierre.tunescout.feature.songs.domain.usecase.ObserveRecentlyPlayed
import com.pierre.tunescout.feature.songs.domain.usecase.SearchSongs
import com.pierre.tunescout.feature.songs.presentation.model.SongsUiEvent
import com.pierre.tunescout.feature.songs.presentation.model.SongsUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SongsViewModel(
    searchSongs: SearchSongs,
    observeRecentlyPlayed: ObserveRecentlyPlayed,
    private val playbackController: PlaybackController,
    private val navigator: Navigator,
) : ViewModel() {
    private val searchDebounce = 300.milliseconds
    private val idleLoadStates = LoadStates(
        refresh = LoadState.NotLoading(endOfPaginationReached = true),
        prepend = LoadState.NotLoading(endOfPaginationReached = true),
        append = LoadState.NotLoading(endOfPaginationReached = true),
    )
    private val query = MutableStateFlow("")

    val uiState: StateFlow<SongsUiState> = combine(
        query,
        observeRecentlyPlayed(),
        playbackController.state,
    ) { query, recentlyPlayed, playback ->
        SongsUiState(
            query = query,
            recentlyPlayed = recentlyPlayed,
            nowPlayingId = playback.currentSong?.id,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = SongsUiState(query = "", recentlyPlayed = emptyList(), nowPlayingId = null),
    )

    val searchResults: Flow<PagingData<Song>> = query
        .debounce(searchDebounce)
        .map { query -> query.trim() }
        .distinctUntilChanged()
        .flatMapLatest { term -> if (term.isBlank()) flowOf(PagingData.empty(idleLoadStates)) else searchSongs(term) }
        .cachedIn(viewModelScope)

    fun onEvent(event: SongsUiEvent) = when (event) {
        is SongsUiEvent.OnQueryChanged -> query.value = event.query
        SongsUiEvent.OnClearQueryClicked -> query.value = ""
        is SongsUiEvent.OnSongClicked -> playAndOpen(event.song)
        is SongsUiEvent.OnSongOptionsClicked -> navigator.navigate(SongOptionsRoute(songId = event.song.id))
    }

    private fun playAndOpen(song: Song) {
        playbackController.play(song = song, songs = listOf(song), context = PlaybackContext.SingleSong)
        navigator.navigate(PlayerRoute(songId = song.id))
    }
}
