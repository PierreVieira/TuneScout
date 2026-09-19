package com.pierre.tunescout.presentation.viewmodel

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.PlaybackState
import com.pierre.tunescout.core.model.PlaybackStatus
import com.pierre.tunescout.core.playback.PlaybackController
import com.pierre.tunescout.core.testing.extension.MainDispatcherExtension
import com.pierre.tunescout.core.testing.fixture.playbackState
import com.pierre.tunescout.core.testing.fixture.song
import com.pierre.tunescout.presentation.model.MainUiAction
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class MainViewModelTest {
    private lateinit var viewModel: MainViewModel
    private lateinit var playbackStateFlow: MutableStateFlow<PlaybackState>
    private lateinit var playbackController: PlaybackController

    @BeforeEach
    fun setUp() {
        playbackStateFlow = MutableStateFlow(PlaybackState.Idle)
        playbackController = mockk(relaxUnitFun = true) {
            every { state } returns playbackStateFlow
        }
        viewModel = MainViewModel(playbackController = playbackController)
    }

    @Test
    fun `WHEN nothing has played yet THEN does not request the notification permission`() =
        runTest(mainDispatcher.dispatcher) {
            // When
            viewModel.uiAction.test {
                // Then
                expectNoEvents()
            }
        }

    @Test
    fun `WHEN playback starts THEN requests the notification permission`() = runTest(mainDispatcher.dispatcher) {
        // When
        viewModel.uiAction.test {
            playbackStateFlow.value = playbackState(songs = listOf(song()))

            // Then
            assertThat(awaitItem()).isEqualTo(MainUiAction.RequestNotificationPermission)
        }
    }

    @Test
    fun `WHEN playback starts again THEN does not request the notification permission a second time`() =
        runTest(mainDispatcher.dispatcher) {
            // When
            viewModel.uiAction.test {
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
