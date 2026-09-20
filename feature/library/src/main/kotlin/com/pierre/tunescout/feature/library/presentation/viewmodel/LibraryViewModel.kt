package com.pierre.tunescout.feature.library.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.route.CreatePlaylistRoute
import com.pierre.tunescout.core.navigation.route.LibrarySearchRoute
import com.pierre.tunescout.feature.library.domain.model.LibraryFilter
import com.pierre.tunescout.feature.library.domain.model.LibraryViewMode
import com.pierre.tunescout.feature.library.domain.usecase.LibraryUseCases
import com.pierre.tunescout.feature.library.presentation.mapper.LibraryItemUiModelMapper
import com.pierre.tunescout.feature.library.presentation.mapper.toRoute
import com.pierre.tunescout.feature.library.presentation.model.LibraryItemUiModel
import com.pierre.tunescout.feature.library.presentation.model.LibraryUiEvent
import com.pierre.tunescout.feature.library.presentation.model.LibraryUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val useCases: LibraryUseCases,
    private val navigator: Navigator,
    itemMapper: LibraryItemUiModelMapper,
) : ViewModel() {
    private val emptyUiState = LibraryUiState(items = emptyList(), viewMode = LibraryViewMode.LIST, filter = null)
    private val filter = MutableStateFlow<LibraryFilter?>(null)
    private val items: Flow<List<LibraryItemUiModel>> = combine(
        useCases.observeFavorites(),
        useCases.observePlaylists(),
        useCases.observeFavoriteAlbums(),
        itemMapper::buildLibraryItems,
    )

    val uiState: StateFlow<LibraryUiState> = combine(
        items,
        useCases.observeViewMode(),
        filter,
        ::LibraryUiState,
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptyUiState,
    )

    fun onEvent(event: LibraryUiEvent) = when (event) {
        is LibraryUiEvent.OnItemClicked -> navigator.navigate(event.item.key.toRoute())
        LibraryUiEvent.OnSearchClicked -> navigator.navigate(LibrarySearchRoute)
        LibraryUiEvent.OnCreatePlaylistClicked -> navigator.navigate(CreatePlaylistRoute)
        is LibraryUiEvent.OnViewModeSelected -> selectViewMode(event.viewMode)
        is LibraryUiEvent.OnFilterClicked -> toggleFilter(event.filter)
    }

    private fun selectViewMode(viewMode: LibraryViewMode) {
        viewModelScope.launch { useCases.setViewMode(viewMode) }
    }

    /** Tapping the chip that is already on clears it, which is how the bar reads with none picked. */
    private fun toggleFilter(clicked: LibraryFilter) {
        filter.value = clicked.takeIf { it != filter.value }
    }
}
