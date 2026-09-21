package com.pierre.tunescout.feature.audiosearch.domain.usecase.impl

import com.pierre.tunescout.feature.audiosearch.domain.model.SpeechRecognitionEvent
import com.pierre.tunescout.feature.audiosearch.domain.repository.SpeechRecognitionRepository
import com.pierre.tunescout.feature.audiosearch.domain.usecase.ListenToSpeech
import kotlinx.coroutines.flow.Flow

class ListenToSpeechUseCase(
    private val repository: SpeechRecognitionRepository,
) : ListenToSpeech {
    override fun invoke(): Flow<SpeechRecognitionEvent> = repository.observeSpeech()
}
