package com.pierre.tunescout.feature.queue.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pierre.tunescout.core.model.PlaybackContext
import com.pierre.tunescout.core.model.PlaybackState
import com.pierre.tunescout.core.model.PlaybackStatus
import com.pierre.tunescout.core.model.QueueSource
import com.pierre.tunescout.core.playback.ObservablePlayback
import com.pierre.tunescout.core.playback.QueueControls
import com.pierre.tunescout.feature.queue.presentation.model.QueueUiEvent
import com.pierre.tunescout.feature.queue.presentation.model.QueueUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class QueueViewModel(
    private val observablePlayback: ObservablePlayback,
    private val queueControls: QueueControls,
) : ViewModel() {
    private val emptyUiState = QueueUiState(
        contextTitle = null,
        nowPlaying = null,
        status = PlaybackStatus.Idle,
        queuedByUser = emptyList(),
        upNext = emptyList(),
    )

    val uiState: StateFlow<QueueUiState> = observablePlayback
        .observePlaybackState()
        .map(::toUiState)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyUiState)

    fun onEvent(event: QueueUiEvent) = when (event) {
        is QueueUiEvent.OnEntryClicked -> queueControls.skipTo(event.entryId)
        is QueueUiEvent.OnRemoveClicked -> queueControls.removeFromQueue(event.entryId)
        is QueueUiEvent.OnEntryMoved -> move(from = event.fromEntryId, to = event.toEntryId)
    }

    private fun move(
        from: String,
        to: String,
    ) {
        val entries = observablePlayback.observePlaybackState().value.entries
        val fromIndex = entries.indexOfFirst { entry -> entry.id == from }
        val toIndex = entries.indexOfFirst { entry -> entry.id == to }
        if (fromIndex < 0 || toIndex < 0) return
        queueControls.moveInQueue(fromIndex = fromIndex, toIndex = toIndex)
    }

    private fun toUiState(playback: PlaybackState): QueueUiState {
        val upcoming = playback.upcomingEntries
        val queuedByUser = upcoming.takeWhile { entry -> entry.source == QueueSource.UserQueue }
        return QueueUiState(
            contextTitle = (playback.context as? PlaybackContext.Album)?.title,
            nowPlaying = playback.currentEntry,
            status = playback.status,
            queuedByUser = queuedByUser,
            upNext = upcoming.drop(queuedByUser.size),
        )
    }
}
