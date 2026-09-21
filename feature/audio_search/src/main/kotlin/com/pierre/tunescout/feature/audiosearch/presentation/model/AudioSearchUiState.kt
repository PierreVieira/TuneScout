package com.pierre.tunescout.feature.audiosearch.presentation.model

import com.pierre.tunescout.feature.audiosearch.domain.model.SpeechFailure

sealed interface AudioSearchUiState {
    /**
     * @property transcript what was understood so far, empty until the first word arrives.
     * @property level how loud the voice is right now, from 0 (silence) to 1.
     */
    data class Listening(
        val transcript: String,
        val level: Float,
    ) : AudioSearchUiState

    data class Failed(
        val failure: SpeechFailure,
    ) : AudioSearchUiState
}
