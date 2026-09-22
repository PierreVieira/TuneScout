package com.pierre.tunescout.feature.library.presentation.content

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.Artwork
import com.pierre.tunescout.core.model.LibraryItemKey
import com.pierre.tunescout.feature.library.domain.model.LibraryFilter
import com.pierre.tunescout.feature.library.domain.model.LibraryViewMode
import com.pierre.tunescout.feature.library.presentation.model.LibraryItemUiModel
import com.pierre.tunescout.feature.library.presentation.model.LibraryUiEvent
import com.pierre.tunescout.feature.library.presentation.model.LibraryUiState
import com.pierre.tunescout.ui.theme.TuneScoutTheme
import de.mannodermaus.junit5.compose.createComposeExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalTestApi::class)
class LibraryContentTest {
    @JvmField
    @RegisterExtension
    val compose = createComposeExtension()

    private val events = mutableListOf<LibraryUiEvent>()
    private val favorites = LibraryItemUiModel.Favorites(songCount = 2, artworks = emptyList())
    private val toxicity = LibraryItemUiModel.Album(
        id = 10,
        title = "Toxicity",
        artistName = "System Of A Down",
        artwork = Artwork("https://example.com/art/10/100x100bb.jpg"),
    )
    private val downloadedSongs = LibraryItemUiModel.DownloadedSongs(songCount = 4, artworks = emptyList())
    private val roadTrip = LibraryItemUiModel.Playlist(
        id = 7,
        name = "Road trip",
        songCount = 3,
        artworks = emptyList(),
    )

    @Test
    fun theListShowsLikedSongsAndEveryPlaylist() = compose.use {
        setContent {
            TuneScoutTheme {
                LibraryContent(uiState = state(), isTwoPane = false, onEvent = events::add)
            }
        }

        onNodeWithText("Your Library").assertIsDisplayed()
        onNodeWithText("Liked songs").assertIsDisplayed()
        onNodeWithText("Road trip").assertIsDisplayed()
        onNodeWithText("Playlist • 3 songs").assertIsDisplayed()
        onNodeWithText("2 songs").assertIsDisplayed()
        onNodeWithText("Toxicity").assertIsDisplayed()
        onNodeWithText("Album • System Of A Down").assertIsDisplayed()
    }

    @Test
    fun theGridShowsTheSameItems() = compose.use {
        setContent {
            TuneScoutTheme {
                LibraryContent(
                    uiState = state(viewMode = LibraryViewMode.GRID),
                    isTwoPane = false,
                    onEvent = events::add,
                )
            }
        }

        onNodeWithText("Liked songs").assertIsDisplayed()
        onNodeWithText("Road trip").assertIsDisplayed()
    }

    @Test
    fun clickingAPlaylistEmitsItsItem() = compose.use {
        setContent {
            TuneScoutTheme {
                LibraryContent(uiState = state(), isTwoPane = false, onEvent = events::add)
            }
        }

        onNodeWithText("Road trip").performClick()

        assertThat(events).containsExactly(LibraryUiEvent.OnItemClicked(roadTrip))
    }

    @Test
    fun theToggleShowsBothModesAndPicksTheOneTapped() = compose.use {
        setContent {
            TuneScoutTheme {
                LibraryContent(uiState = state(), isTwoPane = false, onEvent = events::add)
            }
        }

        onNodeWithContentDescription("Show as list").assertIsDisplayed()
        onNodeWithContentDescription("Show as grid").performClick()

        assertThat(events).containsExactly(LibraryUiEvent.OnViewModeSelected(LibraryViewMode.GRID))
    }

    @Test
    fun theHeaderOpensSearch() = compose.use {
        setContent {
            TuneScoutTheme {
                LibraryContent(uiState = state(), isTwoPane = false, onEvent = events::add)
            }
        }

        onNodeWithContentDescription("Search your library").performClick()

        assertThat(events).containsExactly(LibraryUiEvent.OnSearchClicked)
    }

    @Test
    fun theFloatingButtonCreatesAPlaylistInBothModes() = compose.use {
        var viewMode by mutableStateOf(LibraryViewMode.LIST)
        setContent {
            TuneScoutTheme {
                LibraryContent(uiState = state(viewMode = viewMode), isTwoPane = false, onEvent = events::add)
            }
        }

        onNodeWithContentDescription("Create playlist").performClick()
        viewMode = LibraryViewMode.GRID
        onNodeWithContentDescription("Create playlist").performClick()

        assertThat(events)
            .containsExactly(
                LibraryUiEvent.OnCreatePlaylistClicked,
                LibraryUiEvent.OnCreatePlaylistClicked,
            )
    }

    /**
     * The library sits in a narrower list pane on a wide window, so the FAB moves into the top bar,
     * beside search — see [LibraryContent].
     */
    @Test
    fun theCreatePlaylistActionMovesIntoTheTopBarOnTwoPanes() = compose.use {
        setContent {
            TuneScoutTheme {
                LibraryContent(uiState = state(), isTwoPane = true, onEvent = events::add)
            }
        }

        onNodeWithContentDescription("Search your library").assertIsDisplayed()
        onNodeWithContentDescription("Create playlist").performClick()

        assertThat(events).containsExactly(LibraryUiEvent.OnCreatePlaylistClicked)
    }

    private fun state(
        viewMode: LibraryViewMode = LibraryViewMode.LIST,
        filters: Set<LibraryFilter> = emptySet(),
        items: List<LibraryItemUiModel> = listOf(favorites, downloadedSongs, roadTrip, toxicity),
        downloadedKeys: Set<LibraryItemKey> = emptySet(),
    ): LibraryUiState = LibraryUiState(
        items = items,
        viewMode = viewMode,
        filters = filters,
        downloadedKeys = downloadedKeys,
    )

    @Test
    fun theAlbumsChipLeavesOnlyTheAlbums() = compose.use {
        setContent {
            TuneScoutTheme {
                LibraryContent(
                    uiState = state(filters = setOf(LibraryFilter.ALBUMS)),
                    isTwoPane = false,
                    onEvent = events::add,
                )
            }
        }

        onNodeWithText("Toxicity").assertIsDisplayed()
        onNodeWithText("Road trip").assertDoesNotExist()
        onNodeWithText("Liked songs").assertDoesNotExist()
    }

    @Test
    fun tappingAChipEmitsItsFilter() = compose.use {
        setContent {
            TuneScoutTheme {
                LibraryContent(uiState = state(), isTwoPane = false, onEvent = events::add)
            }
        }

        onNodeWithText("Albums").performClick()

        assertThat(events).containsExactly(LibraryUiEvent.OnFilterClicked(LibraryFilter.ALBUMS))
    }

    @Test
    fun theSongsDownloadedOnTheirOwnOnlyShowUnderTheDownloadedChip() = compose.use {
        var filters by mutableStateOf(emptySet<LibraryFilter>())
        setContent {
            TuneScoutTheme {
                LibraryContent(
                    uiState = state(filters = filters, downloadedKeys = setOf(LibraryItemKey.Album(albumId = 10))),
                    isTwoPane = false,
                    onEvent = events::add,
                )
            }
        }

        onNodeWithText("Individual songs").assertDoesNotExist()
        filters = setOf(LibraryFilter.DOWNLOADED)
        onNodeWithText("Individual songs").assertIsDisplayed()
        onNodeWithText("4 songs").assertIsDisplayed()
        onNodeWithText("Toxicity").assertIsDisplayed()
        onNodeWithText("Road trip").assertDoesNotExist()
        onNodeWithText("Liked songs").assertDoesNotExist()
    }

    @Test
    fun theClearButtonOnlyShowsWithAChipOnAndClearsThem() = compose.use {
        var filters by mutableStateOf(emptySet<LibraryFilter>())
        setContent {
            TuneScoutTheme {
                LibraryContent(uiState = state(filters = filters), isTwoPane = false, onEvent = events::add)
            }
        }

        onNodeWithContentDescription("Clear filters").assertDoesNotExist()
        filters = setOf(LibraryFilter.DOWNLOADED)
        onNodeWithContentDescription("Clear filters").performClick()

        assertThat(events).containsExactly(LibraryUiEvent.OnClearFiltersClicked)
    }

    @Test
    fun nothingDownloadedSaysSoUnderTheDownloadedChip() = compose.use {
        setContent {
            TuneScoutTheme {
                LibraryContent(
                    uiState = state(filters = setOf(LibraryFilter.DOWNLOADED), items = listOf(favorites, roadTrip)),
                    isTwoPane = false,
                    onEvent = events::add,
                )
            }
        }

        onNodeWithText("Nothing downloaded yet", substring = true).assertIsDisplayed()
    }
}
