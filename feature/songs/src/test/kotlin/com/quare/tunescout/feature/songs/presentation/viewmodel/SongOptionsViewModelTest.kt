package com.quare.tunescout.feature.songs.presentation.viewmodel

import com.google.common.truth.Truth.assertThat
import com.quare.tunescout.core.model.Song
import com.quare.tunescout.core.navigation.Navigator
import com.quare.tunescout.core.navigation.route.AlbumRoute
import com.quare.tunescout.core.navigation.route.SongOptionsRoute
import com.quare.tunescout.core.testing.extension.MainDispatcherExtension
import com.quare.tunescout.core.testing.fixture.song
import com.quare.tunescout.feature.songs.presentation.model.SongOptionsUiEvent
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class SongOptionsViewModelTest {
    private lateinit var viewModel: SongOptionsViewModel
    private lateinit var navigator: Navigator

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
    fun `WHEN dismissing THEN navigates back`() = runTest(mainDispatcher.dispatcher) {
        // Given
        prepareScenario(song = song(id = 1))

        // When
        viewModel.onEvent(SongOptionsUiEvent.OnDismissed)

        // Then
        verify { navigator.navigateBack() }
    }

    private fun TestScope.prepareScenario(song: Song?) {
        navigator = mockk(relaxUnitFun = true)
        viewModel = SongOptionsViewModel(
            route = SongOptionsRoute(songId = 1),
            observeSong = { flowOf(song) },
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
