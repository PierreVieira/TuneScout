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
import com.pierre.tunescout.core.navigation.route.DownloadedSongsRoute
import com.pierre.tunescout.core.navigation.route.FavoritesRoute
import com.pierre.tunescout.core.navigation.route.LibrarySearchRoute
import com.pierre.tunescout.core.navigation.route.PlaylistRoute
import com.pierre.tunescout.core.testing.extension.MainDispatcherExtension
import com.pierre.tunescout.core.testing.fixture.albumSummary
import com.pierre.tunescout.core.testing.fixture.playlist
import com.pierre.tunescout.core.testing.fixture.song
import com.pierre.tunescout.feature.library.domain.model.LibraryFilter
import com.pierre.tunescout.feature.library.domain.model.LibraryGridColumns
import com.pierre.tunescout.feature.library.domain.model.LibraryViewMode
import com.pierre.tunescout.feature.library.domain.usecase.LibraryUseCases
import com.pierre.tunescout.feature.library.presentation.mapper.LibraryItemUiModelMapper
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
    private lateinit var storedGridColumns: MutableStateFlow<LibraryGridColumns>

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
    fun `GIVEN two per row WHEN tapping items per row THEN three are stored`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(gridColumns = LibraryGridColumns.TWO)

        // When
        viewModel.onEvent(LibraryUiEvent.OnGridColumnsClicked)
        runCurrent()

        // Then
        assertThat(storedGridColumns.value).isEqualTo(LibraryGridColumns.THREE)
        assertThat(viewModel.uiState.value.gridColumns).isEqualTo(LibraryGridColumns.THREE)
    }

    @Test
    fun `GIVEN four per row WHEN tapping items per row THEN it is back to two`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(gridColumns = LibraryGridColumns.FOUR)

        // When
        viewModel.onEvent(LibraryUiEvent.OnGridColumnsClicked)
        runCurrent()

        // Then
        assertThat(storedGridColumns.value).isEqualTo(LibraryGridColumns.TWO)
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
            assertThat(viewModel.uiState.value.filters).isEmpty()
            assertThat(viewModel.uiState.value.filteredItems).hasSize(2)
        }

    @Test
    fun `GIVEN the downloaded chip WHEN picking it THEN only what was downloaded is left, songs on their own first`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                playlists = listOf(playlist(id = 7), playlist(id = 8)),
                albums = listOf(albumSummary(id = 10), albumSummary(id = 11)),
                downloadedCollections = setOf(
                    LibraryItemKey.Playlist(playlistId = 8),
                    LibraryItemKey.Album(albumId = 10),
                ),
                downloadedSongs = listOf(song(id = 1)),
            )

            // When
            viewModel.onEvent(LibraryUiEvent.OnFilterClicked(LibraryFilter.DOWNLOADED))
            runCurrent()

            // Then
            assertThat(
                viewModel.uiState.value.filteredItems
                    .map { item -> item.key },
            ).containsExactly(
                LibraryItemKey.DownloadedSongs,
                LibraryItemKey.Playlist(playlistId = 8),
                LibraryItemKey.Album(albumId = 10),
            ).inOrder()
        }

    @Test
    fun `GIVEN songs downloaded on their own WHEN no chip is picked THEN they are not listed as an item`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(downloadedSongs = listOf(song(id = 1)))

            // When
            val keys = viewModel.uiState.value.filteredItems
                .map { item -> item.key }

            // Then
            assertThat(keys).containsExactly(LibraryItemKey.Favorites)
        }

    @Test
    fun `GIVEN no song downloaded on its own WHEN picking the downloaded chip THEN there is no item for them`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                albums = listOf(albumSummary(id = 10)),
                downloadedCollections = setOf(LibraryItemKey.Album(albumId = 10)),
            )

            // When
            viewModel.onEvent(LibraryUiEvent.OnFilterClicked(LibraryFilter.DOWNLOADED))
            runCurrent()

            // Then
            assertThat(
                viewModel.uiState.value.filteredItems
                    .map { item -> item.key },
            ).containsExactly(LibraryItemKey.Album(albumId = 10))
            assertThat(viewModel.uiState.value.isDownloadedEmpty).isFalse()
        }

    @Test
    fun `GIVEN nothing downloaded WHEN picking the downloaded chip THEN the library says so`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(playlists = listOf(playlist(id = 7)))

            // When
            viewModel.onEvent(LibraryUiEvent.OnFilterClicked(LibraryFilter.DOWNLOADED))
            runCurrent()

            // Then
            assertThat(viewModel.uiState.value.filteredItems).isEmpty()
            assertThat(viewModel.uiState.value.isDownloadedEmpty).isTrue()
        }

    @Test
    fun `GIVEN albums WHEN picking the albums chip THEN it is not empty`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(albums = listOf(albumSummary(id = 10)))

        // When
        viewModel.onEvent(LibraryUiEvent.OnFilterClicked(LibraryFilter.ALBUMS))
        runCurrent()

        // Then
        assertThat(viewModel.uiState.value.filteredItems).isNotEmpty()
        assertThat(viewModel.uiState.value.isAlbumsEmpty).isFalse()
    }

    @Test
    fun `GIVEN no albums WHEN picking the albums chip THEN the library says so`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(playlists = listOf(playlist(id = 7)))

        // When
        viewModel.onEvent(LibraryUiEvent.OnFilterClicked(LibraryFilter.ALBUMS))
        runCurrent()

        // Then
        assertThat(viewModel.uiState.value.filteredItems).isEmpty()
        assertThat(viewModel.uiState.value.isAlbumsEmpty).isTrue()
    }

    @Test
    fun `GIVEN the downloaded chip WHEN picking albums too THEN only the downloaded albums are left`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                playlists = listOf(playlist(id = 7)),
                albums = listOf(albumSummary(id = 10), albumSummary(id = 11)),
                downloadedCollections = setOf(
                    LibraryItemKey.Playlist(playlistId = 7),
                    LibraryItemKey.Album(albumId = 11),
                ),
                downloadedSongs = listOf(song(id = 1)),
            )
            viewModel.onEvent(LibraryUiEvent.OnFilterClicked(LibraryFilter.DOWNLOADED))

            // When
            viewModel.onEvent(LibraryUiEvent.OnFilterClicked(LibraryFilter.ALBUMS))
            runCurrent()

            // Then
            assertThat(
                viewModel.uiState.value.filteredItems
                    .map { item -> item.key },
            ).containsExactly(LibraryItemKey.Album(albumId = 11))
        }

    @Test
    fun `GIVEN the downloaded chip WHEN picking playlists too THEN the songs on their own stay with the playlists`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                playlists = listOf(playlist(id = 7)),
                albums = listOf(albumSummary(id = 10)),
                downloadedCollections = setOf(
                    LibraryItemKey.Playlist(playlistId = 7),
                    LibraryItemKey.Album(albumId = 10),
                ),
                downloadedSongs = listOf(song(id = 1)),
            )
            viewModel.onEvent(LibraryUiEvent.OnFilterClicked(LibraryFilter.DOWNLOADED))

            // When
            viewModel.onEvent(LibraryUiEvent.OnFilterClicked(LibraryFilter.PLAYLISTS))
            runCurrent()

            // Then
            assertThat(
                viewModel.uiState.value.filteredItems
                    .map { item -> item.key },
            ).containsExactly(LibraryItemKey.DownloadedSongs, LibraryItemKey.Playlist(playlistId = 7)).inOrder()
        }

    @Test
    fun `GIVEN a kind picked WHEN picking the other kind THEN it takes its place and downloaded stays on`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario()
            viewModel.onEvent(LibraryUiEvent.OnFilterClicked(LibraryFilter.DOWNLOADED))
            viewModel.onEvent(LibraryUiEvent.OnFilterClicked(LibraryFilter.PLAYLISTS))

            // When
            viewModel.onEvent(LibraryUiEvent.OnFilterClicked(LibraryFilter.ALBUMS))
            runCurrent()

            // Then
            assertThat(viewModel.uiState.value.filters).containsExactly(LibraryFilter.DOWNLOADED, LibraryFilter.ALBUMS)
        }

    @Test
    fun `GIVEN chips picked WHEN clearing them THEN none is on and everything is listed`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(albums = listOf(albumSummary(id = 10)))
            viewModel.onEvent(LibraryUiEvent.OnFilterClicked(LibraryFilter.DOWNLOADED))
            viewModel.onEvent(LibraryUiEvent.OnFilterClicked(LibraryFilter.ALBUMS))

            // When
            viewModel.onEvent(LibraryUiEvent.OnClearFiltersClicked)
            runCurrent()

            // Then
            assertThat(viewModel.uiState.value.filters).isEmpty()
            assertThat(viewModel.uiState.value.filteredItems).hasSize(2)
        }

    @Test
    fun `GIVEN no chip picked WHEN observing THEN every chip is drawn in its own order`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario()

            // When
            val visible = viewModel.uiState.value.visibleFilters

            // Then
            assertThat(visible)
                .containsExactly(LibraryFilter.PLAYLISTS, LibraryFilter.ALBUMS, LibraryFilter.DOWNLOADED)
                .inOrder()
        }

    @Test
    fun `GIVEN the downloaded chip picked WHEN observing THEN it leads and both kinds follow`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario()

            // When
            viewModel.onEvent(LibraryUiEvent.OnFilterClicked(LibraryFilter.DOWNLOADED))
            runCurrent()

            // Then
            assertThat(viewModel.uiState.value.visibleFilters)
                .containsExactly(LibraryFilter.DOWNLOADED, LibraryFilter.PLAYLISTS, LibraryFilter.ALBUMS)
                .inOrder()
        }

    @Test
    fun `GIVEN a kind picked WHEN observing THEN the other kind steps aside`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario()

        // When
        viewModel.onEvent(LibraryUiEvent.OnFilterClicked(LibraryFilter.ALBUMS))
        runCurrent()

        // Then
        assertThat(viewModel.uiState.value.visibleFilters)
            .containsExactly(LibraryFilter.ALBUMS, LibraryFilter.DOWNLOADED)
            .inOrder()
    }

    @Test
    fun `GIVEN songs downloaded on their own WHEN clicking their item THEN opens them`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(downloadedSongs = listOf(song(id = 1)))
            viewModel.onEvent(LibraryUiEvent.OnFilterClicked(LibraryFilter.DOWNLOADED))
            runCurrent()

            // When
            viewModel.onEvent(
                LibraryUiEvent.OnItemClicked(
                    viewModel.uiState.value.filteredItems
                        .first(),
                ),
            )

            // Then
            verify { navigator.navigate(DownloadedSongsRoute) }
        }

    @Test
    fun `GIVEN collections the user downloaded WHEN observing the library THEN it knows which ones they are`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(
                playlists = listOf(playlist(id = 7)),
                downloadedCollections = setOf(LibraryItemKey.Favorites, LibraryItemKey.Playlist(playlistId = 7)),
            )

            // When
            val state = viewModel.uiState.value

            // Then
            assertThat(state.downloadedKeys).containsExactly(
                LibraryItemKey.Favorites,
                LibraryItemKey.Playlist(playlistId = 7),
            )
        }

    private fun TestScope.prepareScenario(
        favorites: List<Song> = emptyList(),
        playlists: List<Playlist> = emptyList(),
        albums: List<AlbumSummary> = emptyList(),
        viewMode: LibraryViewMode = LibraryViewMode.LIST,
        gridColumns: LibraryGridColumns = LibraryGridColumns.TWO,
        downloadedCollections: Set<LibraryItemKey> = emptySet(),
        downloadedSongs: List<Song> = emptyList(),
    ) {
        storedViewMode = MutableStateFlow(viewMode)
        storedGridColumns = MutableStateFlow(gridColumns)
        navigator = mockk(relaxUnitFun = true)
        viewModel = LibraryViewModel(
            useCases = LibraryUseCases(
                observePlaylists = { flowOf(playlists) },
                observeFavorites = { flowOf(favorites) },
                observeFavoriteAlbums = { flowOf(albums) },
                observeViewMode = { storedViewMode },
                setViewMode = { mode -> storedViewMode.value = mode },
                observeGridColumns = { storedGridColumns },
                setGridColumns = { columns -> storedGridColumns.value = columns },
                observeCollectionDownloads = { flowOf(downloadedCollections) },
                observeDownloadedSongs = { flowOf(downloadedSongs) },
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
