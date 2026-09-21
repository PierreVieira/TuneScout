package com.pierre.tunescout.feature.audiosearch.data.datasource

import android.content.Intent

/** The intent that tells the recognizer how to listen: the language model, partial results. */
fun interface ListeningIntentFactory {
    fun create(): Intent
}
