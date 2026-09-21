package com.pierre.tunescout.feature.library.presentation.content

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performCustomAccessibilityActionWithLabel
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.CollectionDownloadState
import com.pierre.tunescout.core.testing.fixture.song
import com.pierre.tunescout.feature.library.presentation.model.CollectionTitle
import com.pierre.tunescout.feature.library.presentation.model.CollectionUiEvent
import com.pierre.tunescout.feature.library.presentation.model.CollectionUiState
import com.pierre.tunescout.ui.theme.TuneScoutTheme
import de.mannodermaus.junit5.compose.createComposeExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalTestApi::class)
class CollectionContentTest {
    @JvmField
    @RegisterExtension
    val compose = createComposeExtension()

    private val events = mutableListOf<CollectionUiEvent>()

    @Test
    fun givenAPlaylistBeingReorderedItsSongsOfferNoOptionsAndDoneFinishes() = compose.use {
        setContent {
            TuneScoutTheme {
                CollectionContent(uiState = loaded(isReordering = true), onEvent = events::add)
            }
        }

        onNodeWithContentDescription("More options").assertDoesNotExist()
        onNodeWithText("Veridis Quo").performClick()
        onNodeWithContentDescription("Done reordering").performClick()

        assertThat(events).containsExactly(CollectionUiEvent.OnReorderFinished)
    }

    @Test
    fun givenAPlaylistBeingReorderedASongMovesThroughItsAccessibilityActions() = compose.use {
        setContent {
            TuneScoutTheme {
                CollectionContent(uiState = loaded(isReordering = true), onEvent = events::add)
            }
        }

        onNodeWithText("Veridis Quo").performCustomAccessibilityActionWithLabel("Move down")
        onNodeWithText("Digital Love").performCustomAccessibilityActionWithLabel("Move up")

        assertThat(events)
            .containsExactly(
                CollectionUiEvent.OnSongMoved(fromSongId = 1, toSongId = 2),
                CollectionUiEvent.OnSongMoved(fromSongId = 2, toSongId = 1),
            ).inOrder()
    }

    @Test
    fun givenAPlaylistNotBeingReorderedASongPlaysAndOpensItsOptions() = compose.use {
        setContent {
            TuneScoutTheme {
                CollectionContent(uiState = loaded(isReordering = false), onEvent = events::add)
            }
        }

        onNodeWithText("Veridis Quo").performClick()
        onNodeWithContentDescription("Done reordering").assertDoesNotExist()

        assertThat(events).containsExactly(CollectionUiEvent.OnSongClicked(songs.first()))
    }

    private val songs = listOf(song(id = 1, title = "Veridis Quo"), song(id = 2, title = "Digital Love"))

    private fun loaded(isReordering: Boolean): CollectionUiState.Loaded = CollectionUiState.Loaded(
        title = CollectionTitle.Custom("Road trip"),
        songs = songs,
        nowPlaying = null,
        isDeletable = true,
        favoriteSongIds = emptySet(),
        unplayableSongIds = emptySet(),
        isPlaying = false,
        isShuffleEnabled = false,
        isReorderable = true,
        isReordering = isReordering,
        download = CollectionDownloadState.NotDownloaded,
        downloadStatuses = emptyMap(),
    )
}
