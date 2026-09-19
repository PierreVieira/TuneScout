package com.pierre.tunescout.feature.library.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.route.CreatePlaylistRoute
import com.pierre.tunescout.core.navigation.route.LibrarySearchRoute
import com.pierre.tunescout.feature.library.domain.model.LibraryViewMode
import com.pierre.tunescout.feature.library.domain.usecase.LibraryUseCases
import com.pierre.tunescout.feature.library.presentation.mapper.buildLibraryItems
import com.pierre.tunescout.feature.library.presentation.mapper.toRoute
import com.pierre.tunescout.feature.library.presentation.model.LibraryUiEvent
import com.pierre.tunescout.feature.library.presentation.model.LibraryUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val useCases: LibraryUseCases,
    private val navigator: Navigator,
) : ViewModel() {
    private val emptyUiState = LibraryUiState(items = emptyList(), viewMode = LibraryViewMode.LIST)

    val uiState: StateFlow<LibraryUiState> = combine(
        useCases.observeFavorites(),
        useCases.observePlaylists(),
        useCases.observeViewMode(),
    ) { favorites, playlists, viewMode ->
        LibraryUiState(
            items = buildLibraryItems(favorites = favorites, playlists = playlists),
            viewMode = viewMode,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptyUiState,
    )

    fun onEvent(event: LibraryUiEvent) = when (event) {
        is LibraryUiEvent.OnItemClicked -> navigator.navigate(event.item.key.toRoute())
        LibraryUiEvent.OnSearchClicked -> navigator.navigate(LibrarySearchRoute)
        LibraryUiEvent.OnCreatePlaylistClicked -> navigator.navigate(CreatePlaylistRoute)
        is LibraryUiEvent.OnViewModeSelected -> selectViewMode(event.viewMode)
    }

    private fun selectViewMode(viewMode: LibraryViewMode) {
        viewModelScope.launch { useCases.setViewMode(viewMode) }
    }
}
