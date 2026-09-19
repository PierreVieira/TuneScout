package com.pierre.tunescout.feature.library.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pierre.tunescout.core.model.LibraryItemKey
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.feature.library.domain.usecase.LibrarySearchUseCases
import com.pierre.tunescout.feature.library.presentation.mapper.buildLibraryItems
import com.pierre.tunescout.feature.library.presentation.mapper.toRoute
import com.pierre.tunescout.feature.library.presentation.model.LibraryItemUiModel
import com.pierre.tunescout.feature.library.presentation.model.LibrarySearchUiEvent
import com.pierre.tunescout.feature.library.presentation.model.LibrarySearchUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibrarySearchViewModel(
    private val useCases: LibrarySearchUseCases,
    private val navigator: Navigator,
) : ViewModel() {
    private val emptyUiState = LibrarySearchUiState(query = "", items = emptyList(), recentSearches = emptyList())
    private val query = MutableStateFlow("")
    private val items: Flow<List<LibraryItemUiModel>> = combine(
        useCases.observeFavorites(),
        useCases.observePlaylists(),
        useCases.observeFavoriteAlbums(),
        ::buildLibraryItems,
    )

    val uiState: StateFlow<LibrarySearchUiState> = combine(
        query,
        items,
        useCases.observeRecentSearches(),
    ) { query, items, recentKeys ->
        LibrarySearchUiState(
            query = query,
            items = items,
            recentSearches = recentKeys.mapNotNull { key -> items.findByKey(key) },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptyUiState,
    )

    fun onEvent(event: LibrarySearchUiEvent) = when (event) {
        is LibrarySearchUiEvent.OnQueryChanged -> query.value = event.query
        LibrarySearchUiEvent.OnClearQueryClicked -> query.value = ""
        is LibrarySearchUiEvent.OnItemClicked -> openItem(event.item)
        is LibrarySearchUiEvent.OnRecentSearchRemoved -> removeRecentSearch(event.item)
        LibrarySearchUiEvent.OnBackClicked -> navigator.navigateBack()
    }

    private fun openItem(item: LibraryItemUiModel) {
        viewModelScope.launch { useCases.recordSearch(item.key) }
        navigator.navigate(item.key.toRoute())
    }

    private fun removeRecentSearch(item: LibraryItemUiModel) {
        viewModelScope.launch { useCases.removeSearch(item.key) }
    }
}

private fun List<LibraryItemUiModel>.findByKey(key: LibraryItemKey): LibraryItemUiModel? =
    firstOrNull { item -> item.key == key }
