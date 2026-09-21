package com.pierre.tunescout.feature.audiosearch.data.datasource

import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import com.pierre.tunescout.feature.audiosearch.domain.model.SpeechFailure
import com.pierre.tunescout.feature.audiosearch.domain.model.SpeechRecognitionEvent

/**
 * Translates the recognizer's callbacks into [SpeechRecognitionEvent]s, so nothing above the data
 * layer reads a `Bundle`, an error code or a level in decibels.
 *
 * @property onEvent receives every event, on the thread the recognizer calls back on.
 */
internal class SpeechEventListener(
    private val onEvent: (SpeechRecognitionEvent) -> Unit,
) : RecognitionListener {
    override fun onRmsChanged(rmsdB: Float) {
        onEvent(SpeechRecognitionEvent.LevelChanged(level = rmsdB.toLevel()))
    }

    /** A partial result with no text yet says nothing the sheet is not already showing. */
    override fun onPartialResults(partialResults: Bundle?) {
        val transcript = partialResults.findTranscriptOrNull() ?: return
        onEvent(SpeechRecognitionEvent.Heard(transcript))
    }

    override fun onResults(results: Bundle?) {
        val transcript = results.findTranscriptOrNull()
        onEvent(
            if (transcript == null) {
                SpeechRecognitionEvent.Failed(SpeechFailure.NothingHeard)
            } else {
                SpeechRecognitionEvent.Recognized(transcript)
            },
        )
    }

    override fun onError(error: Int) {
        onEvent(SpeechRecognitionEvent.Failed(error.toFailure()))
    }

    override fun onReadyForSpeech(params: Bundle?) {}

    override fun onBeginningOfSpeech() {}

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {}

    override fun onEvent(
        eventType: Int,
        params: Bundle?,
    ) {}

    /**
     * @return the recognizer's best guess, or `null` when it understood nothing: the candidates
     * come ordered by confidence, and a blank one is as good as none.
     */
    private fun Bundle?.findTranscriptOrNull(): String? = this
        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        ?.firstOrNull()
        ?.trim()
        ?.takeIf { transcript -> transcript.isNotEmpty() }

    private fun Float.toLevel(): Float = ((this - MIN_RMS_DB) / (MAX_RMS_DB - MIN_RMS_DB)).coerceIn(0f, 1f)

    private fun Int.toFailure(): SpeechFailure = when (this) {
        SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> SpeechFailure.NothingHeard
        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> SpeechFailure.NoConnection
        else -> SpeechFailure.Unknown
    }

    /**
     * The platform documents no range for the level it reports. These are the bounds Google's
     * recognizer stays within in practice: about -2 dB in a silent room, about 10 dB for a voice
     * close to the microphone.
     */
    private companion object {
        const val MIN_RMS_DB = -2f
        const val MAX_RMS_DB = 10f
    }
}
