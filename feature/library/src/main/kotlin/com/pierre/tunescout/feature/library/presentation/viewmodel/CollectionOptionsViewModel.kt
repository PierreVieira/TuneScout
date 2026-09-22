package com.pierre.tunescout.feature.library.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.reorder.ReorderRequests
import com.pierre.tunescout.core.navigation.reorder.ReorderTarget
import com.pierre.tunescout.core.playback.DuplicatesInQueue
import com.pierre.tunescout.core.playback.Enqueuer
import com.pierre.tunescout.core.playback.ObservablePlayback
import com.pierre.tunescout.core.playback.PlayableSongs
import com.pierre.tunescout.core.playback.QueuePlacement
import com.pierre.tunescout.core.playback.enqueue
import com.pierre.tunescout.feature.library.domain.model.CollectionKey
import com.pierre.tunescout.feature.library.domain.usecase.CollectionUseCases
import com.pierre.tunescout.feature.library.presentation.mapper.CollectionStreams
import com.pierre.tunescout.feature.library.presentation.model.CollectionOptionsUiAction
import com.pierre.tunescout.feature.library.presentation.model.CollectionOptionsUiEvent
import com.pierre.tunescout.feature.library.presentation.model.CollectionOptionsUiState
import com.pierre.tunescout.ui.component.R
import com.pierre.tunescout.ui.utils.ActionViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CollectionOptionsViewModel(
    private val key: CollectionKey,
    private val useCases: CollectionUseCases,
    private val enqueuer: Enqueuer,
    private val observablePlayback: ObservablePlayback,
    private val playableSongs: PlayableSongs,
    private val navigator: Navigator,
    private val reorderRequests: ReorderRequests,
    collectionStreams: CollectionStreams,
) : ActionViewModel<CollectionOptionsUiAction>() {
    private val isDeletable = key is CollectionKey.Playlist
    private val emptyUiState = CollectionOptionsUiState(
        title = null,
        songs = emptyList(),
        isDeletable = isDeletable,
        isReorderable = key is CollectionKey.Playlist,
        isConfirmingDelete = false,
        duplicates = null,
    )
    private val isConfirmingDelete = MutableStateFlow(false)
    private val duplicates = MutableStateFlow<DuplicatesInQueue?>(null)

    val uiState: StateFlow<CollectionOptionsUiState> = combine(
        collectionStreams.observeTitle(key),
        collectionStreams.observeSongs(key),
        isConfirmingDelete,
        duplicates,
    ) { title, songs, isConfirming, duplicates ->
        emptyUiState.copy(title = title, songs = songs, isConfirmingDelete = isConfirming, duplicates = duplicates)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyUiState)

    fun onEvent(event: CollectionOptionsUiEvent) = when (event) {
        CollectionOptionsUiEvent.OnPlayNextClicked -> queueUnlessQueued(QueuePlacement.Next)
        CollectionOptionsUiEvent.OnAddToQueueClicked -> queueUnlessQueued(QueuePlacement.End)
        CollectionOptionsUiEvent.OnDuplicatesInQueueConfirmed -> queueAgain()
        CollectionOptionsUiEvent.OnDuplicatesInQueueDismissed -> duplicates.value = null
        CollectionOptionsUiEvent.OnReorderClicked -> startReordering()
        CollectionOptionsUiEvent.OnDeleteClicked -> askForDeleteConfirmation()
        CollectionOptionsUiEvent.OnDeleteConfirmed -> deleteCollection()
        CollectionOptionsUiEvent.OnDeleteDismissed -> isConfirmingDelete.value = false
    }

    /**
     * Only the songs the player can reach are queued, so the queue does not stall on one it cannot.
     * With none of them left the sheet stays open, with the message saying why. When the user
     * already queued some of them, the sheet asks before adding those again.
     */
    private fun queueUnlessQueued(placement: QueuePlacement) {
        val playable = findPlayableSongs() ?: return
        val playback = observablePlayback.observePlaybackState().value
        val queuedCount = playable.count { song -> playback.isQueuedByUser(song.id) }
        if (queuedCount > 0) {
            duplicates.value = DuplicatesInQueue(placement = placement, count = queuedCount)
        } else {
            queue(playable, placement)
        }
    }

    private fun queueAgain() {
        val placement = duplicates.value?.placement ?: return
        duplicates.value = null
        val playable = findPlayableSongs() ?: return
        queue(playable, placement)
    }

    private fun findPlayableSongs(): List<Song>? {
        val songs = uiState.value.songs
        if (songs.isEmpty()) return null
        val playable = playableSongs.filterPlayable(songs)
        if (playable.isEmpty()) {
            showSongUnavailableOffline()
            return null
        }
        return playable
    }

    private fun queue(
        songs: List<Song>,
        placement: QueuePlacement,
    ) {
        enqueuer.enqueue(songs, placement)
        navigator.navigateBack()
    }

    /** The sheet closes as it asks, so the playlist under it is what the user sees reordering start on. */
    private fun startReordering() {
        if (key !is CollectionKey.Playlist) return
        reorderRequests.request(ReorderTarget.Playlist(playlistId = key.playlistId))
        navigator.navigateBack()
    }

    private fun showSongUnavailableOffline() {
        emitAction(CollectionOptionsUiAction.ShowSnackBar(R.string.ui_song_unavailable_offline))
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
