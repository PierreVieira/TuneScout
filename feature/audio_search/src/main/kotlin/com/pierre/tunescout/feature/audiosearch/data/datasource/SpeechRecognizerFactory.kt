package com.pierre.tunescout.feature.audiosearch.data.datasource

import android.speech.SpeechRecognizer

/**
 * A recognizer serves one listening session and is destroyed with it, so the repository asks for a
 * new one every time instead of holding on to the one it was built with.
 */
fun interface SpeechRecognizerFactory {
    fun create(): SpeechRecognizer
}
