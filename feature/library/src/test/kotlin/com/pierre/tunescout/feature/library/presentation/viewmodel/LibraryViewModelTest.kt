package com.pierre.tunescout.feature.library.presentation.viewmodel

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.Playlist
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.route.CreatePlaylistRoute
import com.pierre.tunescout.core.navigation.route.FavoritesRoute
import com.pierre.tunescout.core.navigation.route.LibrarySearchRoute
import com.pierre.tunescout.core.navigation.route.PlaylistRoute
import com.pierre.tunescout.core.testing.extension.MainDispatcherExtension
import com.pierre.tunescout.core.testing.fixture.playlist
import com.pierre.tunescout.core.testing.fixture.song
import com.pierre.tunescout.feature.library.domain.model.LibraryViewMode
import com.pierre.tunescout.feature.library.domain.usecase.LibraryUseCases
import com.pierre.tunescout.feature.library.presentation.model.LibraryItemUiModel
import com.pierre.tunescout.feature.library.presentation.model.LibraryUiEvent
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

class LibraryViewModelTest {
    private lateinit var viewModel: LibraryViewModel
    private lateinit var navigator: Navigator
    private lateinit var storedViewMode: MutableStateFlow<LibraryViewMode>

    @Test
    fun `GIVEN liked songs and playlists WHEN observing THEN puts liked songs first`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                favorites = listOf(song(id = 1), song(id = 2)),
                playlists = listOf(playlist(id = 7, name = "Road trip", songCount = 3)),
            )

            // When
            val items = viewModel.uiState.value.items

            // Then
            assertThat(items).hasSize(2)
            assertThat(items.first()).isEqualTo(
                LibraryItemUiModel.Favorites(
                    songCount = 2,
                    artworks = listOf(song(id = 1).artwork, song(id = 2).artwork),
                ),
            )
            assertThat(items.last()).isEqualTo(
                LibraryItemUiModel.Playlist(id = 7, name = "Road trip", songCount = 3, artworks = emptyList()),
            )
        }

    @Test
    fun `GIVEN the liked songs row WHEN clicking it THEN opens the favourites collection`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.onEvent(
                LibraryUiEvent.OnItemClicked(
                    viewModel.uiState.value.items
                        .first(),
                ),
            )

            // Then
            verify { navigator.navigate(FavoritesRoute) }
        }

    @Test
    fun `GIVEN a playlist row WHEN clicking it THEN opens that playlist`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(playlists = listOf(playlist(id = 7)))

        // When
        viewModel.onEvent(
            LibraryUiEvent.OnItemClicked(
                viewModel.uiState.value.items
                    .last(),
            ),
        )

        // Then
        verify { navigator.navigate(PlaylistRoute(playlistId = 7)) }
    }

    @Test
    fun `WHEN clicking search THEN opens the library search`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(LibraryUiEvent.OnSearchClicked)

        // Then
        verify { navigator.navigate(LibrarySearchRoute) }
    }

    @Test
    fun `WHEN clicking create playlist THEN opens the create dialog`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(LibraryUiEvent.OnCreatePlaylistClicked)

        // Then
        verify { navigator.navigate(CreatePlaylistRoute) }
    }

    @Test
    fun `GIVEN the list view WHEN toggling THEN stores the grid view`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(viewMode = LibraryViewMode.LIST)

        // When
        viewModel.onEvent(LibraryUiEvent.OnViewModeToggled)
        runCurrent()

        // Then
        assertThat(storedViewMode.value).isEqualTo(LibraryViewMode.GRID)
        assertThat(viewModel.uiState.value.viewMode).isEqualTo(LibraryViewMode.GRID)
    }

    private fun TestScope.prepareScenario(
        favorites: List<Song> = emptyList(),
        playlists: List<Playlist> = emptyList(),
        viewMode: LibraryViewMode = LibraryViewMode.LIST,
    ) {
        storedViewMode = MutableStateFlow(viewMode)
        navigator = mockk(relaxUnitFun = true)
        viewModel = LibraryViewModel(
            useCases = LibraryUseCases(
                observePlaylists = { flowOf(playlists) },
                observeFavorites = { flowOf(favorites) },
                observeViewMode = { storedViewMode },
                setViewMode = { mode -> storedViewMode.value = mode },
            ),
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
