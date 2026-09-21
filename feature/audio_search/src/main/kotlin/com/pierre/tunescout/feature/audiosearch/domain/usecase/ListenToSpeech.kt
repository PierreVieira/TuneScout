package com.pierre.tunescout.feature.audiosearch.domain.usecase

import com.pierre.tunescout.feature.audiosearch.domain.model.SpeechRecognitionEvent
import kotlinx.coroutines.flow.Flow

fun interface ListenToSpeech {
    operator fun invoke(): Flow<SpeechRecognitionEvent>
}
