package com.pierre.tunescout.feature.library.presentation.viewmodel

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.AlbumSummary
import com.pierre.tunescout.core.model.Artwork
import com.pierre.tunescout.core.model.LibraryItemKey
import com.pierre.tunescout.core.model.Playlist
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.route.AlbumRoute
import com.pierre.tunescout.core.navigation.route.CreatePlaylistRoute
import com.pierre.tunescout.core.navigation.route.FavoritesRoute
import com.pierre.tunescout.core.navigation.route.LibrarySearchRoute
import com.pierre.tunescout.core.navigation.route.PlaylistRoute
import com.pierre.tunescout.core.testing.extension.MainDispatcherExtension
import com.pierre.tunescout.core.testing.fixture.albumSummary
import com.pierre.tunescout.core.testing.fixture.playlist
import com.pierre.tunescout.core.testing.fixture.song
import com.pierre.tunescout.feature.library.domain.model.LibraryFilter
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
    fun `GIVEN the list view WHEN picking the grid THEN stores it`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(viewMode = LibraryViewMode.LIST)

        // When
        viewModel.onEvent(LibraryUiEvent.OnViewModeSelected(LibraryViewMode.GRID))
        runCurrent()

        // Then
        assertThat(storedViewMode.value).isEqualTo(LibraryViewMode.GRID)
        assertThat(viewModel.uiState.value.viewMode).isEqualTo(LibraryViewMode.GRID)
    }

    @Test
    fun `GIVEN liked albums WHEN observing THEN lists them after the playlists`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(
            playlists = listOf(playlist(id = 7)),
            albums = listOf(albumSummary(id = 10, title = "Toxicity", artistName = "System Of A Down")),
        )

        // When
        val items = viewModel.uiState.value.items

        // Then
        assertThat(items.last()).isEqualTo(
            LibraryItemUiModel.Album(
                id = 10,
                title = "Toxicity",
                artistName = "System Of A Down",
                artwork = Artwork("https://example.com/art/10/100x100bb.jpg"),
            ),
        )
    }

    @Test
    fun `GIVEN a liked album WHEN clicking it THEN opens the album screen`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(albums = listOf(albumSummary(id = 10)))

        // When
        viewModel.onEvent(
            LibraryUiEvent.OnItemClicked(
                viewModel.uiState.value.items
                    .last(),
            ),
        )

        // Then
        verify { navigator.navigate(AlbumRoute(albumId = 10)) }
    }

    @Test
    fun `GIVEN the albums chip WHEN picking it THEN only albums are left`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(playlists = listOf(playlist(id = 7)), albums = listOf(albumSummary(id = 10)))

        // When
        viewModel.onEvent(LibraryUiEvent.OnFilterClicked(LibraryFilter.ALBUMS))
        runCurrent()

        // Then
        assertThat(
            viewModel.uiState.value.filteredItems
                .map { item -> item.key },
        ).containsExactly(LibraryItemKey.Album(albumId = 10))
    }

    @Test
    fun `GIVEN the playlists chip WHEN picking it THEN the liked songs stay with the playlists`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(playlists = listOf(playlist(id = 7)), albums = listOf(albumSummary(id = 10)))

            // When
            viewModel.onEvent(LibraryUiEvent.OnFilterClicked(LibraryFilter.PLAYLISTS))
            runCurrent()

            // Then
            assertThat(
                viewModel.uiState.value.filteredItems
                    .map { item -> item.key },
            ).containsExactly(LibraryItemKey.Favorites, LibraryItemKey.Playlist(playlistId = 7))
                .inOrder()
        }

    @Test
    fun `GIVEN a chip already picked WHEN tapping it again THEN everything is listed again`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(albums = listOf(albumSummary(id = 10)))
            viewModel.onEvent(LibraryUiEvent.OnFilterClicked(LibraryFilter.ALBUMS))
            runCurrent()

            // When
            viewModel.onEvent(LibraryUiEvent.OnFilterClicked(LibraryFilter.ALBUMS))
            runCurrent()

            // Then
            assertThat(viewModel.uiState.value.filter).isNull()
            assertThat(viewModel.uiState.value.filteredItems).hasSize(2)
        }

    private fun TestScope.prepareScenario(
        favorites: List<Song> = emptyList(),
        playlists: List<Playlist> = emptyList(),
        albums: List<AlbumSummary> = emptyList(),
        viewMode: LibraryViewMode = LibraryViewMode.LIST,
    ) {
        storedViewMode = MutableStateFlow(viewMode)
        navigator = mockk(relaxUnitFun = true)
        viewModel = LibraryViewModel(
            useCases = LibraryUseCases(
                observePlaylists = { flowOf(playlists) },
                observeFavorites = { flowOf(favorites) },
                observeFavoriteAlbums = { flowOf(albums) },
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
