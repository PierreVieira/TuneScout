package com.pierre.tunescout.feature.songoptions.presentation.viewmodel

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.route.AddToPlaylistRoute
import com.pierre.tunescout.core.navigation.route.AlbumRoute
import com.pierre.tunescout.core.navigation.route.SongOptionsRoute
import com.pierre.tunescout.core.playback.Enqueuer
import com.pierre.tunescout.core.playback.PlayableSongs
import com.pierre.tunescout.core.testing.extension.MainDispatcherExtension
import com.pierre.tunescout.core.testing.fixture.song
import com.pierre.tunescout.feature.songoptions.domain.usecase.SongOptionsUseCases
import com.pierre.tunescout.feature.songoptions.presentation.model.SongOptionsUiAction
import com.pierre.tunescout.feature.songoptions.presentation.model.SongOptionsUiEvent
import com.pierre.tunescout.ui.component.R
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
    private lateinit var favoriteToggles: MutableList<Pair<Long, Boolean>>
    private lateinit var actions: MutableList<SongOptionsUiAction>

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
    fun `GIVEN a cached song WHEN clicking play now THEN it takes over the current song and closes the sheet`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(song = song(id = 1))

            // When
            viewModel.onEvent(SongOptionsUiEvent.OnPlayNowClicked)

            // Then
            verifyOrder {
                enqueuer.playNow(listOf(song(id = 1)))
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
    fun `WHEN dismissing THEN navigates back`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(song = song(id = 1))

        // When
        viewModel.onEvent(SongOptionsUiEvent.OnDismissed)

        // Then
        verify { navigator.navigateBack() }
    }

    @Test
    fun `GIVEN a song that is not liked WHEN clicking like THEN stores it and dismisses`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(song = song(id = 1), isFavorite = false)

            // When
            viewModel.onEvent(SongOptionsUiEvent.OnFavoriteClicked)
            runCurrent()

            // Then
            assertThat(favoriteToggles).containsExactly(1L to false)
            verify { navigator.navigateBack() }
        }

    @Test
    fun `GIVEN a liked song WHEN clicking unlike THEN passes the current state through`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(song = song(id = 1), isFavorite = true)

            // When
            viewModel.onEvent(SongOptionsUiEvent.OnFavoriteClicked)
            runCurrent()

            // Then
            assertThat(favoriteToggles).containsExactly(1L to true)
        }

    @Test
    fun `GIVEN a cached song WHEN clicking add to playlist THEN replaces the sheet with the picker`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(song = song(id = 7))

            // When
            viewModel.onEvent(SongOptionsUiEvent.OnAddToPlaylistClicked)

            // Then
            verify { navigator.navigateReplacingTop(AddToPlaylistRoute(songId = 7)) }
        }

    @Test
    fun `GIVEN a song the player cannot reach WHEN queueing it THEN says so and keeps the sheet open`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            prepareScenario(song = song(id = 1), isPlayable = false)

            // When
            viewModel.onEvent(SongOptionsUiEvent.OnPlayNowClicked)

            // Then
            assertThat(actions).containsExactly(SongOptionsUiAction.ShowSnackBar(R.string.ui_song_unavailable_offline))
            verify(exactly = 0) { enqueuer.playNow(any()) }
            verify(exactly = 0) { navigator.navigateBack() }
        }

    private fun TestScope.prepareScenario(
        song: Song?,
        isFavorite: Boolean = false,
        isPlayable: Boolean = true,
    ) {
        favoriteToggles = mutableListOf()
        actions = mutableListOf()
        enqueuer = mockk(relaxUnitFun = true)
        navigator = mockk(relaxUnitFun = true)
        viewModel = SongOptionsViewModel(
            route = SongOptionsRoute(songId = 1),
            useCases = SongOptionsUseCases(
                observeSong = { flowOf(song) },
                isFavorite = { flowOf(isFavorite) },
                toggleFavorite = { toggled, wasFavorite -> favoriteToggles += toggled.id to wasFavorite },
            ),
            enqueuer = enqueuer,
            playableSongs = PlayableSongs { isPlayable },
            navigator = navigator,
        )
        backgroundScope.launch { viewModel.uiState.collect {} }
        backgroundScope.launch { viewModel.uiAction.collect { action -> actions += action } }
        runCurrent()
    }

    companion object {
        @JvmField
        @RegisterExtension
        val mainDispatcher = MainDispatcherExtension()
    }
}
