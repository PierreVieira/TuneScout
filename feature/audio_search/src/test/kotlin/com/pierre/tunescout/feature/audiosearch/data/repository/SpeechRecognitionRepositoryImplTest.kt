package com.pierre.tunescout.feature.audiosearch.data.repository

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.feature.audiosearch.domain.model.SpeechFailure
import com.pierre.tunescout.feature.audiosearch.domain.model.SpeechRecognitionEvent
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SpeechRecognitionRepositoryImplTest {
    private lateinit var repository: SpeechRecognitionRepositoryImpl
    private lateinit var recognizer: SpeechRecognizer
    private lateinit var listeningIntent: Intent
    private lateinit var listener: CapturingSlot<RecognitionListener>

    @BeforeEach
    fun setUp() {
        listener = slot()
        listeningIntent = mockk()
        recognizer = mockk {
            every { setRecognitionListener(capture(listener)) } just runs
            every { startListening(any()) } just runs
            every { destroy() } just runs
        }
        repository = SpeechRecognitionRepositoryImpl(
            recognizerFactory = { recognizer },
            listeningIntentFactory = { listeningIntent },
        )
    }

    @Test
    fun `WHEN collection starts THEN starts listening only after the listener is set`() = runTest {
        // When
        repository.observeSpeech().test {
            // Then
            verifyOrder {
                recognizer.setRecognitionListener(any())
                recognizer.startListening(listeningIntent)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `WHEN the voice level changes THEN emits it and keeps listening`() = runTest {
        // When
        repository.observeSpeech().test {
            listener.captured.onRmsChanged(10f)

            // Then
            assertThat(awaitItem()).isEqualTo(SpeechRecognitionEvent.LevelChanged(level = 1f))
            expectNoEvents()
            verify(exactly = 0) { recognizer.destroy() }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `WHEN the recognizer fails THEN emits the failure, completes and closes the microphone`() = runTest {
        // When
        repository.observeSpeech().test {
            listener.captured.onError(SpeechRecognizer.ERROR_NETWORK)

            // Then
            assertThat(awaitItem()).isEqualTo(SpeechRecognitionEvent.Failed(SpeechFailure.NoConnection))
            awaitComplete()
        }
        verify(exactly = 1) { recognizer.destroy() }
    }

    @Test
    fun `WHEN the collection is cancelled THEN closes the microphone`() = runTest {
        // When
        repository.observeSpeech().test {
            cancelAndIgnoreRemainingEvents()
        }

        // Then
        verify(exactly = 1) { recognizer.destroy() }
    }

    @Test
    fun `GIVEN a collector that has not caught up WHEN the session ends THEN the final result is not dropped`() =
        runTest {
            // Given
            val events = mutableListOf<SpeechRecognitionEvent>()
            val collection = launch { repository.observeSpeech().toList(events) }
            runCurrent()

            // When
            repeat(LEVELS_BEYOND_THE_DEFAULT_BUFFER) { listener.captured.onRmsChanged(10f) }
            listener.captured.onResults(results("daft punk"))
            collection.join()

            // Then
            assertThat(events).hasSize(LEVELS_BEYOND_THE_DEFAULT_BUFFER + 1)
            assertThat(events.last()).isEqualTo(SpeechRecognitionEvent.Recognized("daft punk"))
        }

    private fun results(vararg candidates: String): Bundle = mockk {
        every { getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) } returns ArrayList(candidates.toList())
    }

    private companion object {
        const val LEVELS_BEYOND_THE_DEFAULT_BUFFER = 200
    }
}
