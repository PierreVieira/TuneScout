package com.pierre.tunescout.feature.audiosearch.data.repository

import com.pierre.tunescout.feature.audiosearch.data.datasource.ListeningIntentFactory
import com.pierre.tunescout.feature.audiosearch.data.datasource.SpeechEventListener
import com.pierre.tunescout.feature.audiosearch.data.datasource.SpeechRecognizerFactory
import com.pierre.tunescout.feature.audiosearch.domain.model.SpeechRecognitionEvent
import com.pierre.tunescout.feature.audiosearch.domain.repository.SpeechRecognitionRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

internal class SpeechRecognitionRepositoryImpl(
    private val recognizerFactory: SpeechRecognizerFactory,
    private val listeningIntentFactory: ListeningIntentFactory,
) : SpeechRecognitionRepository {
    /**
     * The platform only lets a `SpeechRecognizer` be created, started and destroyed on the main
     * thread, and all three happen in the collector's context: collect this from the main
     * dispatcher, which is where `viewModelScope` already runs.
     *
     * @return one listening session, which completes after the event that ends it.
     */
    override fun observeSpeech(): Flow<SpeechRecognitionEvent> = callbackFlow {
        val recognizer = recognizerFactory.create()
        val listener = SpeechEventListener { event ->
            trySend(event)
            if (event.endsSession) close()
        }
        recognizer.setRecognitionListener(listener)
        recognizer.startListening(listeningIntentFactory.create())
        awaitClose { recognizer.destroy() }
    }

    private val SpeechRecognitionEvent.endsSession: Boolean
        get() = this is SpeechRecognitionEvent.Recognized || this is SpeechRecognitionEvent.Failed
}
