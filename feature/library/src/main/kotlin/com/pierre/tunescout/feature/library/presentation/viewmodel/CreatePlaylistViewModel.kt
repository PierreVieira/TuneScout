package com.pierre.tunescout.feature.library.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.route.PlaylistRoute
import com.pierre.tunescout.feature.library.domain.usecase.CreatePlaylist
import com.pierre.tunescout.feature.library.presentation.model.CreatePlaylistUiEvent
import com.pierre.tunescout.feature.library.presentation.model.CreatePlaylistUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CreatePlaylistViewModel(
    private val createPlaylist: CreatePlaylist,
    private val navigator: Navigator,
) : ViewModel() {
    val uiState: StateFlow<CreatePlaylistUiState>
        field = MutableStateFlow(CreatePlaylistUiState(name = ""))

    fun onEvent(event: CreatePlaylistUiEvent) = when (event) {
        is CreatePlaylistUiEvent.OnNameChanged -> uiState.value = CreatePlaylistUiState(name = event.name)
        CreatePlaylistUiEvent.OnConfirmClicked -> confirm()
        CreatePlaylistUiEvent.OnDismissed -> navigator.navigateBack()
    }

    private fun confirm() {
        val state = uiState.value
        if (!state.canConfirm) return
        viewModelScope.launch {
            val playlistId = createPlaylist(state.name.trim())
            navigator.navigateReplacingTop(PlaylistRoute(playlistId = playlistId))
        }
    }
}
