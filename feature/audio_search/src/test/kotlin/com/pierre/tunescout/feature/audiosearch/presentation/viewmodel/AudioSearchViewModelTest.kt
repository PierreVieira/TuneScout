package com.pierre.tunescout.feature.audiosearch.presentation.viewmodel

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.testing.extension.MainDispatcherExtension
import com.pierre.tunescout.feature.audiosearch.domain.model.SpeechFailure
import com.pierre.tunescout.feature.audiosearch.domain.model.SpeechRecognitionEvent
import com.pierre.tunescout.feature.audiosearch.presentation.model.AudioSearchUiEvent
import com.pierre.tunescout.feature.audiosearch.presentation.model.AudioSearchUiState
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class AudioSearchViewModelTest {
    private lateinit var viewModel: AudioSearchViewModel
    private lateinit var navigator: Navigator
    private lateinit var sessions: MutableList<Channel<SpeechRecognitionEvent>>
    private lateinit var publishedQueries: MutableList<String>

    @BeforeEach
    fun setUp() {
        sessions = mutableListOf()
        publishedQueries = mutableListOf()
        navigator = mockk(relaxUnitFun = true)
        viewModel = AudioSearchViewModel(
            listenToSpeech = { Channel<SpeechRecognitionEvent>(Channel.UNLIMITED).also(sessions::add).consumeAsFlow() },
            queryPublisher = publishedQueries::add,
            navigator = navigator,
        )
    }

    @Test
    fun `WHEN the sheet opens THEN listens in silence with nothing heard yet`() = runTest(mainDispatcher.dispatcher) {
        // When
        runCurrent()

        // Then
        assertThat(sessions).hasSize(1)
        assertThat(viewModel.uiState.value).isEqualTo(AudioSearchUiState.Listening(transcript = "", level = 0f))
    }

    @Test
    fun `WHEN the user speaks THEN shows the words and the level as they arrive`() =
        runTest(mainDispatcher.dispatcher) {
            // When
            sessions.last().send(SpeechRecognitionEvent.LevelChanged(level = 0.6f))
            sessions.last().send(SpeechRecognitionEvent.Heard(transcript = "daft"))
            runCurrent()

            // Then
            assertThat(
                viewModel.uiState.value,
            ).isEqualTo(AudioSearchUiState.Listening(transcript = "daft", level = 0.6f))
            assertThat(publishedQueries).isEmpty()
        }

    @Test
    fun `WHEN the user stops speaking THEN publishes the query and closes the sheet`() =
        runTest(mainDispatcher.dispatcher) {
            // When
            sessions.last().send(SpeechRecognitionEvent.Recognized(transcript = "daft punk"))
            runCurrent()

            // Then
            assertThat(publishedQueries).containsExactly("daft punk")
            assertThat(viewModel.uiState.value)
                .isEqualTo(AudioSearchUiState.Listening(transcript = "daft punk", level = 0f))
            verify(exactly = 1) { navigator.navigateBack() }
        }

    @Test
    fun `WHEN listening fails THEN shows why and keeps the sheet open`() = runTest(mainDispatcher.dispatcher) {
        // When
        sessions.last().send(SpeechRecognitionEvent.Failed(SpeechFailure.NothingHeard))
        runCurrent()

        // Then
        assertThat(viewModel.uiState.value).isEqualTo(AudioSearchUiState.Failed(SpeechFailure.NothingHeard))
        assertThat(publishedQueries).isEmpty()
        verify(exactly = 0) { navigator.navigateBack() }
    }

    @Test
    fun `GIVEN a failed session WHEN a late level arrives THEN the failure stays on screen`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            sessions.last().send(SpeechRecognitionEvent.Failed(SpeechFailure.NoConnection))

            // When
            sessions.last().send(SpeechRecognitionEvent.LevelChanged(level = 0.9f))
            runCurrent()

            // Then
            assertThat(viewModel.uiState.value).isEqualTo(AudioSearchUiState.Failed(SpeechFailure.NoConnection))
        }

    @Test
    fun `GIVEN a failed session WHEN retrying THEN listens again on a new session`() =
        runTest(mainDispatcher.dispatcher) {
            // Given
            sessions.last().send(SpeechRecognitionEvent.Failed(SpeechFailure.NothingHeard))
            runCurrent()

            // When
            viewModel.onEvent(AudioSearchUiEvent.OnRetryClicked)
            sessions.last().send(SpeechRecognitionEvent.Heard(transcript = "get lucky"))
            runCurrent()

            // Then
            assertThat(sessions).hasSize(2)
            assertThat(viewModel.uiState.value)
                .isEqualTo(AudioSearchUiState.Listening(transcript = "get lucky", level = 0f))
        }

    companion object {
        @JvmField
        @RegisterExtension
        val mainDispatcher = MainDispatcherExtension()
    }
}
