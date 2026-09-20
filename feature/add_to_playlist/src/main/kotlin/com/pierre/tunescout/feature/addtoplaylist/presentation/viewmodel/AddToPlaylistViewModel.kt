package com.pierre.tunescout.feature.addtoplaylist.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.route.AddToPlaylistRoute
import com.pierre.tunescout.feature.addtoplaylist.domain.usecase.AddToPlaylistUseCases
import com.pierre.tunescout.feature.addtoplaylist.presentation.model.AddToPlaylistUiEvent
import com.pierre.tunescout.feature.addtoplaylist.presentation.model.AddToPlaylistUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddToPlaylistViewModel(
    private val useCases: AddToPlaylistUseCases,
    private val navigator: Navigator,
    route: AddToPlaylistRoute,
) : ViewModel() {
    private val emptyUiState = AddToPlaylistUiState(song = null, playlists = emptyList(), newPlaylistName = null)
    private val newPlaylistName = MutableStateFlow<String?>(null)

    val uiState: StateFlow<AddToPlaylistUiState> = combine(
        useCases.observeSong(route.songId),
        useCases.observePlaylists(),
        newPlaylistName,
        ::AddToPlaylistUiState,
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyUiState)

    fun onEvent(event: AddToPlaylistUiEvent) = when (event) {
        is AddToPlaylistUiEvent.OnPlaylistClicked -> addToPlaylist(event.playlistId)
        AddToPlaylistUiEvent.OnNewPlaylistClicked -> newPlaylistName.value = ""
        is AddToPlaylistUiEvent.OnNewPlaylistNameChanged -> newPlaylistName.value = event.name
        AddToPlaylistUiEvent.OnNewPlaylistConfirmed -> createPlaylist()
        AddToPlaylistUiEvent.OnNewPlaylistDismissed -> newPlaylistName.update { null }
        AddToPlaylistUiEvent.OnDismissed -> navigator.navigateBack()
    }

    private fun addToPlaylist(playlistId: Long) {
        val song = uiState.value.song ?: return
        viewModelScope.launch {
            useCases.addSongToPlaylist(playlistId = playlistId, song = song)
            navigator.navigateBack()
        }
    }

    private fun createPlaylist() {
        val state = uiState.value
        val song = state.song ?: return
        val name = state.newPlaylistName?.trim().orEmpty()
        if (name.isEmpty()) return
        viewModelScope.launch {
            useCases.createPlaylistWithSong(name = name, song = song)
            navigator.navigateBack()
        }
    }
}
