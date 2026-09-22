package com.pierre.tunescout.feature.album.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.reorder.ReorderRequests
import com.pierre.tunescout.core.navigation.reorder.ReorderTarget
import com.pierre.tunescout.core.navigation.route.AlbumOptionsRoute
import com.pierre.tunescout.core.playback.DuplicatesInQueue
import com.pierre.tunescout.core.playback.Enqueuer
import com.pierre.tunescout.core.playback.ObservablePlayback
import com.pierre.tunescout.core.playback.PlayableSongs
import com.pierre.tunescout.core.playback.QueuePlacement
import com.pierre.tunescout.core.playback.enqueue
import com.pierre.tunescout.feature.album.domain.usecase.ObserveAlbum
import com.pierre.tunescout.feature.album.presentation.model.AlbumOptionsUiAction
import com.pierre.tunescout.feature.album.presentation.model.AlbumOptionsUiEvent
import com.pierre.tunescout.feature.album.presentation.model.AlbumOptionsUiState
import com.pierre.tunescout.ui.component.R
import com.pierre.tunescout.ui.utils.ActionViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class AlbumOptionsViewModel(
    private val enqueuer: Enqueuer,
    private val observablePlayback: ObservablePlayback,
    private val playableSongs: PlayableSongs,
    private val navigator: Navigator,
    private val reorderRequests: ReorderRequests,
    private val route: AlbumOptionsRoute,
    observeAlbum: ObserveAlbum,
) : ActionViewModel<AlbumOptionsUiAction>() {
    private val duplicates = MutableStateFlow<DuplicatesInQueue?>(null)

    val uiState: StateFlow<AlbumOptionsUiState> = combine(
        observeAlbum(route.albumId),
        duplicates,
        ::AlbumOptionsUiState,
    ).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        AlbumOptionsUiState(album = null, duplicates = null),
    )

    fun onEvent(event: AlbumOptionsUiEvent) = when (event) {
        AlbumOptionsUiEvent.OnPlayNextClicked -> queueUnlessQueued(QueuePlacement.Next)
        AlbumOptionsUiEvent.OnAddToQueueClicked -> queueUnlessQueued(QueuePlacement.End)
        AlbumOptionsUiEvent.OnDuplicatesInQueueConfirmed -> queueAgain()
        AlbumOptionsUiEvent.OnDuplicatesInQueueDismissed -> duplicates.value = null
        AlbumOptionsUiEvent.OnReorderClicked -> startReordering()
    }

    /** The sheet closes as it asks, so the album under it is what the user sees reordering start on. */
    private fun startReordering() {
        if (uiState.value.album == null) return
        reorderRequests.request(ReorderTarget.Album(albumId = route.albumId))
        navigator.navigateBack()
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
        val songs = uiState.value.album
            ?.songs
            .orEmpty()
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

    private fun showSongUnavailableOffline() {
        emitAction(AlbumOptionsUiAction.ShowSnackBar(R.string.ui_song_unavailable_offline))
    }
}
