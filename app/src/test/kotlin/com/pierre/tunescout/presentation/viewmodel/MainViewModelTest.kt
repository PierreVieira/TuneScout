package com.pierre.tunescout.presentation.viewmodel

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.PlaybackState
import com.pierre.tunescout.core.model.PlaybackStatus
import com.pierre.tunescout.core.testing.extension.MainDispatcherExtension
import com.pierre.tunescout.core.testing.fixture.playbackState
import com.pierre.tunescout.core.testing.fixture.song
import com.pierre.tunescout.presentation.model.MainUiState
import com.pierre.tunescout.ui.theme.Theme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class MainViewModelTest {
    private lateinit var viewModel: MainViewModel
    private lateinit var playbackStateFlow: MutableStateFlow<PlaybackState>
    private lateinit var themeFlow: MutableStateFlow<Theme>

    @BeforeEach
    fun setUp() {
        playbackStateFlow = MutableStateFlow(PlaybackState.Idle)
        themeFlow = MutableStateFlow(Theme.SYSTEM)
        viewModel = MainViewModel(
            observablePlayback = { playbackStateFlow },
            observeTheme = { themeFlow },
            observeDynamicColorEnabled = { flowOf(false) },
        )
    }

    @Test
    fun `WHEN the stored theme arrives THEN leaves the loading state the splash waits on`() =
        runTest(mainDispatcher.dispatcher) {
            // When
            themeFlow.value = Theme.LIGHT

            // Then
            assertThat(viewModel.uiState.value)
                .isEqualTo(MainUiState.Ready(theme = Theme.LIGHT, isDynamicColorEnabled = false))
        }

    @Test
    fun `WHEN nothing has played yet THEN does not request the notification permission`() =
        runTest(mainDispatcher.dispatcher) {
            // When
            viewModel.requestNotificationPermissionsUiAction.test {
                // Then
                expectNoEvents()
            }
        }

    @Test
    fun `WHEN playback starts THEN requests the notification permission`() = runTest(mainDispatcher.dispatcher) {
        // When
        viewModel.requestNotificationPermissionsUiAction.test {
            playbackStateFlow.value = playbackState(songs = listOf(song()))

            // Then
            assertThat(awaitItem()).isEqualTo(Unit)
        }
    }

    @Test
    fun `WHEN playback starts again THEN does not request the notification permission a second time`() =
        runTest(mainDispatcher.dispatcher) {
            // When
            viewModel.requestNotificationPermissionsUiAction.test {
                playbackStateFlow.value = playbackState(songs = listOf(song()))
                awaitItem()
                playbackStateFlow.value = playbackState(songs = listOf(song()), status = PlaybackStatus.Paused)
                playbackStateFlow.value = playbackState(songs = listOf(song()))

                // Then
                expectNoEvents()
            }
        }

    companion object {
        @JvmField
        @RegisterExtension
        val mainDispatcher = MainDispatcherExtension()
    }
}
