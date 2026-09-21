package com.pierre.tunescout.feature.audiosearch.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pierre.tunescout.core.audiosearch.AudioSearchQueryPublisher
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.feature.audiosearch.domain.model.SpeechRecognitionEvent
import com.pierre.tunescout.feature.audiosearch.domain.usecase.ListenToSpeech
import com.pierre.tunescout.feature.audiosearch.presentation.model.AudioSearchUiEvent
import com.pierre.tunescout.feature.audiosearch.presentation.model.AudioSearchUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

class AudioSearchViewModel(
    private val listenToSpeech: ListenToSpeech,
    private val queryPublisher: AudioSearchQueryPublisher,
    private val navigator: Navigator,
) : ViewModel() {
    private val silence = AudioSearchUiState.Listening(transcript = "", level = 0f)
    private var session: Job? = null

    val uiState: StateFlow<AudioSearchUiState>
        field = MutableStateFlow<AudioSearchUiState>(silence)

    init {
        listen()
    }

    fun onEvent(event: AudioSearchUiEvent) = when (event) {
        AudioSearchUiEvent.OnRetryClicked -> listen()
    }

    /**
     * The microphone belongs to the session, and the session to `viewModelScope`: dismissing the
     * sheet clears the ViewModel, which cancels the collection and closes the microphone with it.
     */
    private fun listen() {
        session?.cancel()
        uiState.value = silence
        session = listenToSpeech()
            .onEach(::handle)
            .launchIn(viewModelScope)
    }

    private fun handle(event: SpeechRecognitionEvent) {
        when (event) {
            is SpeechRecognitionEvent.LevelChanged -> updateListening { state -> state.copy(level = event.level) }
            is SpeechRecognitionEvent.Heard -> updateListening { state -> state.copy(transcript = event.transcript) }
            is SpeechRecognitionEvent.Recognized -> search(event.transcript)
            is SpeechRecognitionEvent.Failed -> uiState.value = AudioSearchUiState.Failed(event.failure)
        }
    }

    /** A level that arrives after the session failed must not bring the listening state back. */
    private fun updateListening(transform: (AudioSearchUiState.Listening) -> AudioSearchUiState.Listening) {
        uiState.update { state -> if (state is AudioSearchUiState.Listening) transform(state) else state }
    }

    private fun search(query: String) {
        uiState.value = AudioSearchUiState.Listening(transcript = query, level = 0f)
        queryPublisher.publish(query)
        navigator.navigateBack()
    }
}
