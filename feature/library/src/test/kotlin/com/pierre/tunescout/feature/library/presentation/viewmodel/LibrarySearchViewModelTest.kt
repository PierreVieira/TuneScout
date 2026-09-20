package com.pierre.tunescout.feature.library.presentation.viewmodel

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.LibraryItemKey
import com.pierre.tunescout.core.model.Playlist
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.route.PlaylistRoute
import com.pierre.tunescout.core.testing.extension.MainDispatcherExtension
import com.pierre.tunescout.core.testing.fixture.playlist
import com.pierre.tunescout.feature.library.domain.usecase.LibrarySearchUseCases
import com.pierre.tunescout.feature.library.presentation.mapper.LibraryItemUiModelMapper
import com.pierre.tunescout.feature.library.presentation.model.LibraryItemUiModel
import com.pierre.tunescout.feature.library.presentation.model.LibrarySearchUiEvent
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class LibrarySearchViewModelTest {
    private lateinit var viewModel: LibrarySearchViewModel
    private lateinit var navigator: Navigator
    private lateinit var recentSearches: MutableStateFlow<List<LibraryItemKey>>

    @Test
    fun `GIVEN a recorded search WHEN observing THEN resolves it back to its library item`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                playlists = listOf(playlist(id = 7, name = "Road trip")),
                recent = listOf(LibraryItemKey.Playlist(playlistId = 7)),
            )

            // When
            val state = viewModel.uiState.value

            // Then
            assertThat(state.recentSearches).containsExactly(
                LibraryItemUiModel.Playlist(id = 7, name = "Road trip", songCount = 0, artworks = emptyList()),
            )
        }

    @Test
    fun `GIVEN a search for a playlist that is gone WHEN observing THEN drops it`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(playlists = emptyList(), recent = listOf(LibraryItemKey.Playlist(playlistId = 7)))

            // When
            val state = viewModel.uiState.value

            // Then
            assertThat(state.recentSearches).isEmpty()
        }

    @Test
    fun `GIVEN a result WHEN opening it THEN records the search and navigates`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(playlists = listOf(playlist(id = 7)))
        val item = viewModel.uiState.value.items
            .last()

        // When
        viewModel.onEvent(LibrarySearchUiEvent.OnItemClicked(item))
        runCurrent()

        // Then
        assertThat(recentSearches.value).containsExactly(LibraryItemKey.Playlist(playlistId = 7))
        verify { navigator.navigate(PlaylistRoute(playlistId = 7)) }
    }

    @Test
    fun `GIVEN a recent search WHEN removing it THEN forgets it`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(
            playlists = listOf(playlist(id = 7)),
            recent = listOf(LibraryItemKey.Playlist(playlistId = 7)),
        )
        val item = viewModel.uiState.value.recentSearches
            .first()

        // When
        viewModel.onEvent(LibrarySearchUiEvent.OnRecentSearchRemoved(item))
        runCurrent()

        // Then
        assertThat(recentSearches.value).isEmpty()
    }

    @Test
    fun `GIVEN a typed query WHEN changing it THEN the state reports it is searching`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.onEvent(LibrarySearchUiEvent.OnQueryChanged("road"))
            runCurrent()

            // Then
            assertThat(viewModel.uiState.value.query).isEqualTo("road")
            assertThat(viewModel.uiState.value.isSearching).isTrue()
        }

    @Test
    fun `WHEN clearing the query THEN stops searching`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario()
        viewModel.onEvent(LibrarySearchUiEvent.OnQueryChanged("road"))
        runCurrent()

        // When
        viewModel.onEvent(LibrarySearchUiEvent.OnClearQueryClicked)
        runCurrent()

        // Then
        assertThat(viewModel.uiState.value.isSearching).isFalse()
    }

    private fun TestScope.prepareScenario(
        playlists: List<Playlist> = emptyList(),
        recent: List<LibraryItemKey> = emptyList(),
    ) {
        recentSearches = MutableStateFlow(recent)
        navigator = mockk(relaxUnitFun = true)
        viewModel = LibrarySearchViewModel(
            useCases = LibrarySearchUseCases(
                observePlaylists = { flowOf(playlists) },
                observeFavorites = { flowOf(emptyList()) },
                observeFavoriteAlbums = { flowOf(emptyList()) },
                observeRecentSearches = { recentSearches },
                recordSearch = { key ->
                    recentSearches.value =
                        listOf(key) + recentSearches.value.filterNot { it == key }
                },
                removeSearch = { key -> recentSearches.value = recentSearches.value.filterNot { it == key } },
            ),
            itemMapper = LibraryItemUiModelMapper(),
            navigator = navigator,
        )
        backgroundScope.launch { viewModel.uiState.collect {} }
        runCurrent()
    }

    companion object {
        @JvmField
        @RegisterExtension
        val mainDispatcher = MainDispatcherExtension()
    }
}
