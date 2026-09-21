package com.pierre.tunescout.feature.audiosearch.data.datasource

import android.os.Bundle
import android.speech.SpeechRecognizer
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.feature.audiosearch.domain.model.SpeechFailure
import com.pierre.tunescout.feature.audiosearch.domain.model.SpeechRecognitionEvent
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class SpeechEventListenerTest {
    private lateinit var listener: SpeechEventListener
    private lateinit var events: MutableList<SpeechRecognitionEvent>

    @BeforeEach
    fun setUp() {
        events = mutableListOf()
        listener = SpeechEventListener(onEvent = events::add)
    }

    @ParameterizedTest
    @CsvSource("-10, 0", "-2, 0", "4, 0.5", "10, 1", "25, 1")
    fun `WHEN the level changes THEN reports it between silence and the loudest voice`(
        rmsDb: Float,
        level: Float,
    ) {
        // When
        listener.onRmsChanged(rmsDb)

        // Then
        assertThat(events).containsExactly(SpeechRecognitionEvent.LevelChanged(level))
    }

    @Test
    fun `WHEN a partial result arrives THEN reports the best guess so far`() {
        // When
        listener.onPartialResults(results("daft", "deft"))

        // Then
        assertThat(events).containsExactly(SpeechRecognitionEvent.Heard("daft"))
    }

    @Test
    fun `WHEN a partial result has no text yet THEN reports nothing`() {
        // When
        listener.onPartialResults(results(""))
        listener.onPartialResults(null)

        // Then
        assertThat(events).isEmpty()
    }

    @Test
    fun `WHEN the final result arrives THEN reports the trimmed transcript`() {
        // When
        listener.onResults(results(" daft punk "))

        // Then
        assertThat(events).containsExactly(SpeechRecognitionEvent.Recognized("daft punk"))
    }

    @Test
    fun `WHEN the final result is blank THEN reports that nothing was heard`() {
        // When
        listener.onResults(results(" "))

        // Then
        assertThat(events).containsExactly(SpeechRecognitionEvent.Failed(SpeechFailure.NothingHeard))
    }

    @Test
    fun `WHEN the final result has no candidates THEN reports that nothing was heard`() {
        // When
        listener.onResults(results())

        // Then
        assertThat(events).containsExactly(SpeechRecognitionEvent.Failed(SpeechFailure.NothingHeard))
    }

    @ParameterizedTest
    @CsvSource(
        "${SpeechRecognizer.ERROR_NO_MATCH}, NothingHeard",
        "${SpeechRecognizer.ERROR_SPEECH_TIMEOUT}, NothingHeard",
        "${SpeechRecognizer.ERROR_NETWORK}, NoConnection",
        "${SpeechRecognizer.ERROR_NETWORK_TIMEOUT}, NoConnection",
        "${SpeechRecognizer.ERROR_AUDIO}, Unknown",
        "${SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS}, Unknown",
    )
    fun `WHEN the recognizer fails THEN reports why in the app's own terms`(
        errorCode: Int,
        failure: SpeechFailure,
    ) {
        // When
        listener.onError(errorCode)

        // Then
        assertThat(events).containsExactly(SpeechRecognitionEvent.Failed(failure))
    }

    @Test
    fun `WHEN the recognizer reports the rest of its lifecycle THEN reports nothing`() {
        // When
        listener.onReadyForSpeech(null)
        listener.onBeginningOfSpeech()
        listener.onBufferReceived(null)
        listener.onEndOfSpeech()
        listener.onEvent(0, null)

        // Then
        assertThat(events).isEmpty()
    }

    private fun results(vararg candidates: String): Bundle = mockk {
        every { getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) } returns ArrayList(candidates.toList())
    }
}
