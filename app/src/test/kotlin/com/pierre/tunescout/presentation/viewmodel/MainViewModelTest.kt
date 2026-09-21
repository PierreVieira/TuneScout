package com.pierre.tunescout.presentation.viewmodel

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.PlaybackState
import com.pierre.tunescout.core.model.PlaybackStatus
import com.pierre.tunescout.core.testing.extension.MainDispatcherExtension
import com.pierre.tunescout.core.testing.fixture.playbackState
import com.pierre.tunescout.core.testing.fixture.song
import com.pierre.tunescout.presentation.model.MainUiState
import com.pierre.tunescout.ui.theme.SystemBars
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
    private lateinit var isOnlineFlow: MutableStateFlow<Boolean>

    @BeforeEach
    fun setUp() {
        playbackStateFlow = MutableStateFlow(PlaybackState.Idle)
        themeFlow = MutableStateFlow(Theme.SYSTEM)
        isOnlineFlow = MutableStateFlow(true)
        viewModel = MainViewModel(
            observablePlayback = { playbackStateFlow },
            observeTheme = { themeFlow },
            observeDynamicColorEnabled = { flowOf(false) },
            networkMonitor = { isOnlineFlow },
        )
    }

    @Test
    fun `WHEN the device's dark mode is not known yet THEN stays in the loading state the splash waits on`() =
        runTest(mainDispatcher.dispatcher) {
            // When
            themeFlow.value = Theme.LIGHT

            // Then
            assertThat(viewModel.uiState.value).isEqualTo(MainUiState.Loading)
        }

    @Test
    fun `WHEN the stored theme and the device's dark mode arrive THEN leaves the loading state`() =
        runTest(mainDispatcher.dispatcher) {
            // When
            themeFlow.value = Theme.LIGHT
            viewModel.onSystemDarkThemeChanged(isSystemInDarkTheme = true)

            // Then
            assertThat(viewModel.uiState.value).isEqualTo(
                MainUiState.Ready(
                    theme = Theme.LIGHT,
                    isDynamicColorEnabled = false,
                    systemBars = SystemBars.of(isDark = false),
                    isOffline = false,
                ),
            )
        }

    @Test
    fun `WHEN the connection goes away THEN says so, so the artwork that cannot load explains itself`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            viewModel.onSystemDarkThemeChanged(isSystemInDarkTheme = false)

            // When
            isOnlineFlow.value = false

            // Then
            assertThat((viewModel.uiState.value as MainUiState.Ready).isOffline).isTrue()
        }

    @Test
    fun `GIVEN the system theme WHEN the device turns dark THEN draws the dark system bars`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            viewModel.onSystemDarkThemeChanged(isSystemInDarkTheme = false)

            // When
            viewModel.onSystemDarkThemeChanged(isSystemInDarkTheme = true)

            // Then
            assertThat((viewModel.uiState.value as MainUiState.Ready).systemBars)
                .isEqualTo(SystemBars.of(isDark = true))
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
