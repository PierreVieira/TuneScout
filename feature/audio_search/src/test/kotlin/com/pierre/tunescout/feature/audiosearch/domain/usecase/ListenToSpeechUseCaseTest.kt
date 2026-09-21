package com.pierre.tunescout.feature.audiosearch.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.feature.audiosearch.domain.model.SpeechRecognitionEvent
import com.pierre.tunescout.feature.audiosearch.domain.usecase.impl.ListenToSpeechUseCase
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ListenToSpeechUseCaseTest {
    @Test
    fun `WHEN listening THEN emits the session the repository opens`() = runTest {
        // Given
        val event = SpeechRecognitionEvent.Recognized("daft punk")
        val useCase = ListenToSpeechUseCase(repository = { flowOf(event) })

        // When
        useCase().test {
            // Then
            assertThat(awaitItem()).isEqualTo(event)
            awaitComplete()
        }
    }
}
