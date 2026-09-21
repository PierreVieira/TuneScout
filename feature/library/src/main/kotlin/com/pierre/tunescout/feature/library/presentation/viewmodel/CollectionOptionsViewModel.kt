package com.pierre.tunescout.feature.library.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.playback.Enqueuer
import com.pierre.tunescout.core.playback.PlayableSongs
import com.pierre.tunescout.feature.library.domain.model.CollectionKey
import com.pierre.tunescout.feature.library.domain.usecase.CollectionUseCases
import com.pierre.tunescout.feature.library.presentation.mapper.CollectionStreams
import com.pierre.tunescout.feature.library.presentation.model.CollectionOptionsUiAction
import com.pierre.tunescout.feature.library.presentation.model.CollectionOptionsUiEvent
import com.pierre.tunescout.feature.library.presentation.model.CollectionOptionsUiState
import com.pierre.tunescout.ui.component.R
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CollectionOptionsViewModel(
    private val key: CollectionKey,
    private val useCases: CollectionUseCases,
    private val enqueuer: Enqueuer,
    private val playableSongs: PlayableSongs,
    private val navigator: Navigator,
    collectionStreams: CollectionStreams,
) : ViewModel() {
    private val isDeletable = key is CollectionKey.Playlist
    private val emptyUiState = CollectionOptionsUiState(
        title = null,
        songs = emptyList(),
        isDeletable = isDeletable,
        isConfirmingDelete = false,
    )
    private val isConfirmingDelete = MutableStateFlow(false)

    val uiAction: SharedFlow<CollectionOptionsUiAction>
        field = MutableSharedFlow<CollectionOptionsUiAction>()

    val uiState: StateFlow<CollectionOptionsUiState> = combine(
        collectionStreams.observeTitle(key),
        collectionStreams.observeSongs(key),
        isConfirmingDelete,
    ) { title, songs, isConfirming ->
        CollectionOptionsUiState(
            title = title,
            songs = songs,
            isDeletable = isDeletable,
            isConfirmingDelete = isConfirming,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyUiState)

    fun onEvent(event: CollectionOptionsUiEvent) = when (event) {
        CollectionOptionsUiEvent.OnPlayNextClicked -> queue(enqueuer::queueNext)
        CollectionOptionsUiEvent.OnAddToQueueClicked -> queue(enqueuer::addToQueue)
        CollectionOptionsUiEvent.OnDeleteClicked -> askForDeleteConfirmation()
        CollectionOptionsUiEvent.OnDeleteConfirmed -> deleteCollection()
        CollectionOptionsUiEvent.OnDeleteDismissed -> isConfirmingDelete.value = false
    }

    /**
     * Only the songs the player can reach are queued, so the queue does not stall on one it cannot.
     * With none of them left the sheet stays open, with the message saying why.
     */
    private fun queue(enqueue: (List<Song>) -> Unit) {
        val songs = uiState.value.songs
        if (songs.isEmpty()) return
        val playable = playableSongs.filterPlayable(songs)
        if (playable.isEmpty()) return showSongUnavailableOffline()
        enqueue(playable)
        navigator.navigateBack()
    }

    private fun showSongUnavailableOffline() {
        emitAction(CollectionOptionsUiAction.ShowSnackBar(R.string.ui_song_unavailable_offline))
    }

    private fun emitAction(action: CollectionOptionsUiAction) {
        viewModelScope.launch { uiAction.emit(action) }
    }

    private fun askForDeleteConfirmation() {
        if (key !is CollectionKey.Playlist) return
        isConfirmingDelete.value = true
    }

    private fun deleteCollection() {
        if (key !is CollectionKey.Playlist) return
        isConfirmingDelete.value = false
        viewModelScope.launch { useCases.deletePlaylist(key.playlistId) }
        closeSheetAndCollection()
    }

    /** The playlist behind the sheet is gone too, so leaving it on screen would strand the user. */
    private fun closeSheetAndCollection() {
        navigator.navigateBack()
        navigator.navigateBack()
    }
}
