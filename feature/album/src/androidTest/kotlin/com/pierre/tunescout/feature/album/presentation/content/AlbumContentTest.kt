package com.pierre.tunescout.feature.album.presentation.content

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performCustomAccessibilityActionWithLabel
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.CollectionDownloadState
import com.pierre.tunescout.core.model.SongDownloadStatus
import com.pierre.tunescout.core.testing.fixture.album
import com.pierre.tunescout.core.testing.fixture.song
import com.pierre.tunescout.feature.album.presentation.model.AlbumUiEvent
import com.pierre.tunescout.feature.album.presentation.model.AlbumUiState
import com.pierre.tunescout.ui.theme.TuneScoutTheme
import de.mannodermaus.junit5.compose.createComposeExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalTestApi::class)
class AlbumContentTest {
    @JvmField
    @RegisterExtension
    val compose = createComposeExtension()

    private val events = mutableListOf<AlbumUiEvent>()

    @Test
    fun givenAnAlbumThatIsNotLikedTheTopBarOffersToLikeItAndOpensTheRest() = compose.use {
        setContent {
            TuneScoutTheme {
                AlbumContent(
                    isHeaderInline = false,
                    uiState = loaded(isFavorite = false),
                    onEvent = events::add,
                )
            }
        }

        onNodeWithContentDescription("Like this album").performClick()
        onNodeWithContentDescription("More options for this album").performClick()

        assertThat(events)
            .containsExactly(AlbumUiEvent.OnFavoriteClicked, AlbumUiEvent.OnMoreClicked)
            .inOrder()
    }

    @Test
    fun givenAnAlbumNotDownloadedItsSwitchIsOffAndATapAsksForIt() = compose.use {
        setContent {
            TuneScoutTheme {
                AlbumContent(isHeaderInline = false, uiState = loaded(), onEvent = events::add)
            }
        }

        onNodeWithContentDescription("Download")
            .assertIsOff()
            .assert(hasStateDescription("Not downloaded"))
            .performClick()

        assertThat(events).containsExactly(AlbumUiEvent.OnDownloadClicked)
    }

    @Test
    fun givenAnAlbumHalfwayDownloadedItsSwitchSaysHowManySongsArrivedAndTheirRowsSayWhereTheyStand() = compose.use {
        setContent {
            TuneScoutTheme {
                AlbumContent(
                    isHeaderInline = false,
                    uiState = loaded(
                        download = CollectionDownloadState.Downloading(downloadedCount = 1, totalCount = 2),
                        downloadStatuses = mapOf(
                            1L to SongDownloadStatus.Downloaded,
                            2L to SongDownloadStatus.Downloading,
                        ),
                    ),
                    onEvent = events::add,
                )
            }
        }

        onNodeWithContentDescription("Download")
            .assertIsOn()
            .assert(hasStateDescription("1 of 2 downloaded"))
        onNode(hasText("Give Life Back to Music", substring = true) and hasStateDescription("Downloaded"))
            .assertIsDisplayed()
        onNode(hasText("The Game of Love", substring = true) and hasStateDescription("Downloading"))
            .assertIsDisplayed()
    }

    @Test
    fun givenALikedAlbumTheTopBarOffersToRemoveIt() = compose.use {
        setContent {
            TuneScoutTheme {
                AlbumContent(
                    isHeaderInline = false,
                    uiState = loaded(isFavorite = true),
                    onEvent = events::add,
                )
            }
        }

        onNodeWithContentDescription("Remove this album from your library").assertIsDisplayed()
    }

    @Test
    fun givenLoadedAlbumEachTrackOpensTheSongOptions() = compose.use {
        val album = album(songs = listOf(song(id = 1, title = "Give Life Back to Music")))
        setContent {
            TuneScoutTheme {
                AlbumContent(
                    isHeaderInline = false,
                    uiState = AlbumUiState.Loaded(
                        album = album,
                        nowPlaying = null,
                        isFavorite = false,
                        isStale = false,
                        favoriteSongIds = emptySet(),
                        unplayableSongIds = emptySet(),
                        isPlaying = false,
                        isShuffleEnabled = false,
                        isReordering = false,
                        download = CollectionDownloadState.NotDownloaded,
                        downloadStatuses = emptyMap(),
                    ),
                    onEvent = events::add,
                )
            }
        }

        onNodeWithContentDescription("More options").performClick()

        assertThat(events).containsExactly(AlbumUiEvent.OnSongOptionsClicked(album.songs.first()))
    }

    @Test
    fun givenLoadedAlbumShowsHeaderAndTracks() = compose.use {
        val album = album(
            songs = listOf(
                song(id = 1, title = "Give Life Back to Music"),
                song(id = 2, title = "The Game of Love"),
            ),
        )
        setContent {
            TuneScoutTheme {
                AlbumContent(
                    isHeaderInline = false,
                    uiState = AlbumUiState.Loaded(
                        album = album,
                        nowPlaying = null,
                        isFavorite = false,
                        isStale = false,
                        favoriteSongIds = emptySet(),
                        unplayableSongIds = emptySet(),
                        isPlaying = false,
                        isShuffleEnabled = false,
                        isReordering = false,
                        download = CollectionDownloadState.NotDownloaded,
                        downloadStatuses = emptyMap(),
                    ),
                    onEvent = events::add,
                )
            }
        }

        onAllNodesWithText("Random Access Memories").assertCountEquals(TITLE_IN_TOP_BAR_AND_HEADER)
        onNodeWithText("Give Life Back to Music").assertIsDisplayed()
        onNodeWithText("The Game of Love").performClick()

        assertThat(events).containsExactly(AlbumUiEvent.OnSongClicked(album.songs[1]))
    }

    @Test
    fun givenTheAlbumIsNotPlayingItsPlayButtonAndShuffleSitUnderTheHeaderAndEmitTheirEvents() = compose.use {
        setContent {
            TuneScoutTheme {
                AlbumContent(isHeaderInline = false, uiState = loaded(), onEvent = events::add)
            }
        }

        onNodeWithContentDescription("Shuffle").assertIsOff().performClick()
        onNodeWithContentDescription("Play the album").performClick()

        assertThat(events)
            .containsExactly(AlbumUiEvent.OnShuffleClicked, AlbumUiEvent.OnPlayPauseClicked)
            .inOrder()
    }

    @Test
    fun givenTheAlbumIsPlayingItsButtonPausesAndShuffleShowsItIsOn() = compose.use {
        setContent {
            TuneScoutTheme {
                AlbumContent(
                    isHeaderInline = false,
                    uiState = loaded(isPlaying = true, isShuffleEnabled = true),
                    onEvent = events::add,
                )
            }
        }

        onNodeWithContentDescription("Shuffle").assertIsOn()
        onNodeWithContentDescription("Play the album").assertDoesNotExist()
        onNodeWithContentDescription("Pause").performClick()

        assertThat(events).containsExactly(AlbumUiEvent.OnPlayPauseClicked)
    }

    @Test
    fun givenErrorShowsRetryThatEmitsEvent() = compose.use {
        setContent {
            TuneScoutTheme {
                AlbumContent(isHeaderInline = false, uiState = AlbumUiState.Error, onEvent = events::add)
            }
        }

        onNodeWithText("Couldn't load this album").assertIsDisplayed()
        onNodeWithText("Try again").performClick()

        assertThat(events).containsExactly(AlbumUiEvent.OnRetryClicked)
    }

    @Test
    fun givenLoadingShowsIndicatorAndBackStillWorks() = compose.use {
        setContent {
            TuneScoutTheme {
                AlbumContent(isHeaderInline = false, uiState = AlbumUiState.Loading, onEvent = events::add)
            }
        }

        onNodeWithContentDescription("Loading").assertIsDisplayed()
        onNodeWithContentDescription("Back").performClick()

        assertThat(events).containsExactly(AlbumUiEvent.OnBackClicked)
    }

    @Test
    fun givenARefreshThatFailedTheScreenSaysTheAlbumIsTheSavedOne() = compose.use {
        setContent {
            TuneScoutTheme {
                AlbumContent(
                    isHeaderInline = false,
                    uiState = loaded(isFavorite = false, isStale = true),
                    onEvent = events::add,
                )
            }
        }

        onNodeWithContentDescription(STALE_NOTICE).assertIsDisplayed()
    }

    @Test
    fun givenAnAlbumThatRefreshedTheScreenSaysNothingAboutIt() = compose.use {
        setContent {
            TuneScoutTheme {
                AlbumContent(
                    isHeaderInline = false,
                    uiState = loaded(isFavorite = false),
                    onEvent = events::add,
                )
            }
        }

        onNodeWithContentDescription(STALE_NOTICE).assertDoesNotExist()
    }

    @Test
    fun givenAnAlbumBeingReorderedItsTracksOfferNoOptionsAndDoneFinishes() = compose.use {
        setContent {
            TuneScoutTheme {
                AlbumContent(isHeaderInline = false, uiState = loaded(isReordering = true), onEvent = events::add)
            }
        }

        onNodeWithContentDescription("More options").assertDoesNotExist()
        onNodeWithContentDescription("More options for this album").assertDoesNotExist()
        onNodeWithText("Give Life Back to Music").performClick()
        onNodeWithContentDescription("Done reordering").performClick()

        assertThat(events).containsExactly(AlbumUiEvent.OnReorderFinished)
    }

    @Test
    fun givenAnAlbumBeingReorderedATrackMovesThroughItsAccessibilityActions() = compose.use {
        setContent {
            TuneScoutTheme {
                AlbumContent(isHeaderInline = false, uiState = loaded(isReordering = true), onEvent = events::add)
            }
        }

        onNodeWithText("Give Life Back to Music").performCustomAccessibilityActionWithLabel("Move down")
        onNodeWithText("The Game of Love").performCustomAccessibilityActionWithLabel("Move up")

        assertThat(events)
            .containsExactly(
                AlbumUiEvent.OnSongMoved(fromSongId = 1, toSongId = 2),
                AlbumUiEvent.OnSongMoved(fromSongId = 2, toSongId = 1),
            ).inOrder()
    }

    @Test
    fun givenAnAlbumNotBeingReorderedATrackOffersNoMoves() = compose.use {
        setContent {
            TuneScoutTheme {
                AlbumContent(isHeaderInline = false, uiState = loaded(), onEvent = events::add)
            }
        }

        onNodeWithText("Give Life Back to Music").performClick()

        assertThat(events).containsExactly(AlbumUiEvent.OnSongClicked(reorderableAlbum.songs.first()))
        onNodeWithContentDescription("Done reordering").assertDoesNotExist()
    }

    private fun loaded(
        isFavorite: Boolean = false,
        isStale: Boolean = false,
        isPlaying: Boolean = false,
        isShuffleEnabled: Boolean = false,
        isReordering: Boolean = false,
        download: CollectionDownloadState = CollectionDownloadState.NotDownloaded,
        downloadStatuses: Map<Long, SongDownloadStatus> = emptyMap(),
    ): AlbumUiState.Loaded = AlbumUiState.Loaded(
        album = reorderableAlbum,
        nowPlaying = null,
        isFavorite = isFavorite,
        isStale = isStale,
        favoriteSongIds = emptySet(),
        unplayableSongIds = emptySet(),
        isPlaying = isPlaying,
        isShuffleEnabled = isShuffleEnabled,
        isReordering = isReordering,
        download = download,
        downloadStatuses = downloadStatuses,
    )

    private fun hasStateDescription(state: String): SemanticsMatcher =
        SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, state)

    private val reorderableAlbum = album(
        songs = listOf(
            song(id = 1, title = "Give Life Back to Music", trackNumber = 1),
            song(id = 2, title = "The Game of Love", trackNumber = 2),
        ),
    )

    private companion object {
        const val TITLE_IN_TOP_BAR_AND_HEADER = 2
        const val STALE_NOTICE = "Couldn't refresh this album. Showing the version saved on this device."
    }
}
