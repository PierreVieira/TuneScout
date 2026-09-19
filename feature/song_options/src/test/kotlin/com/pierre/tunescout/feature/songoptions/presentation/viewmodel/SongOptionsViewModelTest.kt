package com.pierre.tunescout.feature.songoptions.presentation.viewmodel

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.route.AlbumRoute
import com.pierre.tunescout.core.navigation.route.SongOptionsRoute
import com.pierre.tunescout.core.playback.Enqueuer
import com.pierre.tunescout.core.testing.extension.MainDispatcherExtension
import com.pierre.tunescout.core.testing.fixture.song
import com.pierre.tunescout.feature.songoptions.domain.usecase.SongOptionsUseCases
import com.pierre.tunescout.feature.songoptions.presentation.model.SongOptionsUiEvent
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class SongOptionsViewModelTest {
    private lateinit var viewModel: SongOptionsViewModel
    private lateinit var enqueuer: Enqueuer
    private lateinit var navigator: Navigator
    private lateinit var removedSongIds: MutableList<Long>

    @Test
    fun `GIVEN a cached song WHEN observing THEN exposes it`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(song = song(id = 1))

        // When
        val state = viewModel.uiState.value

        // Then
        assertThat(state.song?.id).isEqualTo(1L)
    }

    @Test
    fun `GIVEN a cached song WHEN clicking view album THEN replaces the sheet with its album`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(song = song(id = 1, albumId = 10))

            // When
            viewModel.onEvent(SongOptionsUiEvent.OnViewAlbumClicked)

            // Then
            verify { navigator.navigateReplacingTop(AlbumRoute(albumId = 10)) }
        }

    @Test
    fun `GIVEN no song yet WHEN clicking view album THEN does nothing`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(song = null)

        // When
        viewModel.onEvent(SongOptionsUiEvent.OnViewAlbumClicked)

        // Then
        verify(exactly = 0) { navigator.navigateReplacingTop(any()) }
    }

    @Test
    fun `GIVEN a cached song WHEN clicking add to queue THEN queues it and closes the sheet`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(song = song(id = 1))

            // When
            viewModel.onEvent(SongOptionsUiEvent.OnAddToQueueClicked)

            // Then
            verifyOrder {
                enqueuer.addToQueue(listOf(song(id = 1)))
                navigator.navigateBack()
            }
        }

    @Test
    fun `GIVEN a cached song WHEN clicking play next THEN queues it next and closes the sheet`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(song = song(id = 1))

            // When
            viewModel.onEvent(SongOptionsUiEvent.OnPlayNextClicked)

            // Then
            verifyOrder {
                enqueuer.queueNext(listOf(song(id = 1)))
                navigator.navigateBack()
            }
        }

    @Test
    fun `GIVEN no song yet WHEN clicking add to queue THEN does nothing`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(song = null)

        // When
        viewModel.onEvent(SongOptionsUiEvent.OnAddToQueueClicked)

        // Then
        verify(exactly = 0) { enqueuer.addToQueue(any()) }
    }

    @Test
    fun `GIVEN a song outside the history WHEN observing THEN does not offer to remove it`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(song = song(id = 1), isRecentlyPlayed = false)

            // When
            val state = viewModel.uiState.value

            // Then
            assertThat(state.isRecentlyPlayed).isFalse()
        }

    @Test
    fun `GIVEN a song in the history WHEN clicking remove THEN drops it and closes the sheet`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(song = song(id = 1), isRecentlyPlayed = true)

            // When
            viewModel.onEvent(SongOptionsUiEvent.OnRemoveFromRecentlyPlayedClicked)
            runCurrent()

            // Then
            assertThat(viewModel.uiState.value.isRecentlyPlayed).isTrue()
            assertThat(removedSongIds).containsExactly(1L)
            verify { navigator.navigateBack() }
        }

    @Test
    fun `GIVEN no song yet WHEN clicking remove THEN does nothing`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(song = null)

        // When
        viewModel.onEvent(SongOptionsUiEvent.OnRemoveFromRecentlyPlayedClicked)
        runCurrent()

        // Then
        assertThat(removedSongIds).isEmpty()
        verify(exactly = 0) { navigator.navigateBack() }
    }

    @Test
    fun `WHEN dismissing THEN navigates back`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(song = song(id = 1))

        // When
        viewModel.onEvent(SongOptionsUiEvent.OnDismissed)

        // Then
        verify { navigator.navigateBack() }
    }

    private fun TestScope.prepareScenario(
        song: Song?,
        isRecentlyPlayed: Boolean = false,
    ) {
        removedSongIds = mutableListOf()
        enqueuer = mockk(relaxUnitFun = true)
        navigator = mockk(relaxUnitFun = true)
        viewModel = SongOptionsViewModel(
            route = SongOptionsRoute(songId = 1),
            useCases = SongOptionsUseCases(
                observeSong = { flowOf(song) },
                isRecentlyPlayed = { flowOf(isRecentlyPlayed) },
                removeFromRecentlyPlayed = { songId -> removedSongIds += songId },
            ),
            enqueuer = enqueuer,
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
