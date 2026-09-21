package com.pierre.tunescout.feature.audiosearch.domain.repository

import com.pierre.tunescout.feature.audiosearch.domain.model.SpeechRecognitionEvent
import kotlinx.coroutines.flow.Flow

fun interface SpeechRecognitionRepository {
    /**
     * @return one listening session: the microphone opens when collection starts, and the flow
     * completes after the event that ends the session. Cancelling the collection closes the
     * microphone.
     */
    fun observeSpeech(): Flow<SpeechRecognitionEvent>
}
