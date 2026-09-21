package com.pierre.tunescout.feature.audiosearch.domain.model

/** What a listening session reports, in the order it happens: [Recognized] or [Failed] ends it. */
sealed interface SpeechRecognitionEvent {
    /**
     * @property level how loud the voice is right now, from 0 (silence) to 1.
     */
    data class LevelChanged(
        val level: Float,
    ) : SpeechRecognitionEvent

    /**
     * @property transcript everything understood so far, while the user is still speaking.
     */
    data class Heard(
        val transcript: String,
    ) : SpeechRecognitionEvent

    /**
     * @property transcript what the user said, once they stopped speaking.
     */
    data class Recognized(
        val transcript: String,
    ) : SpeechRecognitionEvent

    data class Failed(
        val failure: SpeechFailure,
    ) : SpeechRecognitionEvent
}
