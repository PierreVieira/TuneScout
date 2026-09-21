package com.pierre.tunescout.feature.queue.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pierre.tunescout.core.model.PlaybackContext
import com.pierre.tunescout.core.model.PlaybackState
import com.pierre.tunescout.core.model.PlaybackStatus
import com.pierre.tunescout.core.model.QueueEntry
import com.pierre.tunescout.core.model.QueueSource
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.route.PlayerRoute
import com.pierre.tunescout.core.playback.ObservablePlayableSongs
import com.pierre.tunescout.core.playback.ObservablePlayback
import com.pierre.tunescout.core.playback.PlayableSongs
import com.pierre.tunescout.core.playback.QueueControls
import com.pierre.tunescout.feature.queue.presentation.model.QueueUiAction
import com.pierre.tunescout.feature.queue.presentation.model.QueueUiEvent
import com.pierre.tunescout.feature.queue.presentation.model.QueueUiState
import com.pierre.tunescout.ui.component.R
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class QueueViewModel(
    private val observablePlayback: ObservablePlayback,
    private val queueControls: QueueControls,
    private val playableSongs: PlayableSongs,
    private val navigator: Navigator,
    observablePlayableSongs: ObservablePlayableSongs,
) : ViewModel() {
    private val emptyUiState = QueueUiState(
        contextTitle = null,
        nowPlaying = null,
        status = PlaybackStatus.Idle,
        queuedByUser = emptyList(),
        upNext = emptyList(),
        unplayableSongIds = emptySet(),
    )

    val uiAction: SharedFlow<QueueUiAction>
        field = MutableSharedFlow<QueueUiAction>()

    val uiState: StateFlow<QueueUiState> = combine(
        observablePlayback.observePlaybackState(),
        observablePlayableSongs.observePlayableSongs(),
        ::toUiState,
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyUiState)

    fun onEvent(event: QueueUiEvent) = when (event) {
        QueueUiEvent.OnNowPlayingClicked -> openPlayer()
        is QueueUiEvent.OnEntryClicked -> skipTo(event.entryId)
        is QueueUiEvent.OnRemoveClicked -> queueControls.removeFromQueue(event.entryId)
        is QueueUiEvent.OnEntryMoved -> move(from = event.fromEntryId, to = event.toEntryId)
    }

    /**
     * An entry was queued while the player could reach its song, which it may no longer be able to:
     * jumping to it then would leave the player stuck on it instead of playing.
     */
    private fun skipTo(entryId: String) {
        val song = observablePlayback
            .observePlaybackState()
            .value.entries
            .firstOrNull { entry -> entry.id == entryId }
            ?.song ?: return
        if (!playableSongs.isPlayable(song)) return showSongUnavailableOffline()
        queueControls.skipTo(entryId)
    }

    private fun showSongUnavailableOffline() {
        emitAction(QueueUiAction.ShowSnackBar(R.string.ui_song_unavailable_offline))
    }

    private fun emitAction(action: QueueUiAction) {
        viewModelScope.launch { uiAction.emit(action) }
    }

    private fun openPlayer() {
        val songId = uiState.value.nowPlaying
            ?.song
            ?.id ?: return
        navigator.navigate(PlayerRoute(songId = songId))
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

    private fun toUiState(
        playback: PlaybackState,
        playable: PlayableSongs,
    ): QueueUiState {
        val upcoming = playback.upcomingEntries
        val queuedByUser = upcoming.takeWhile { entry -> entry.source == QueueSource.UserQueue }
        return QueueUiState(
            contextTitle = (playback.context as? PlaybackContext.Album)?.title,
            nowPlaying = playback.currentEntry,
            status = playback.status,
            queuedByUser = queuedByUser,
            upNext = upcoming.drop(queuedByUser.size),
            unplayableSongIds = playable.findUnplayableIds(playback.entries.map(QueueEntry::song)),
        )
    }
}
