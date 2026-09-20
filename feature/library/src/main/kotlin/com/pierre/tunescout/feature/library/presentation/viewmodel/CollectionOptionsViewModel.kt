package com.pierre.tunescout.feature.library.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.playback.Enqueuer
import com.pierre.tunescout.feature.library.domain.model.CollectionKey
import com.pierre.tunescout.feature.library.domain.usecase.CollectionUseCases
import com.pierre.tunescout.feature.library.presentation.mapper.observeCollectionSongs
import com.pierre.tunescout.feature.library.presentation.mapper.observeCollectionTitle
import com.pierre.tunescout.feature.library.presentation.model.CollectionOptionsUiEvent
import com.pierre.tunescout.feature.library.presentation.model.CollectionOptionsUiState
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
    private val navigator: Navigator,
) : ViewModel() {
    private val isDeletable = key is CollectionKey.Playlist
    private val emptyUiState = CollectionOptionsUiState(
        title = null,
        songs = emptyList(),
        isDeletable = isDeletable,
        isConfirmingDelete = false,
    )
    private val isConfirmingDelete = MutableStateFlow(false)

    val uiState: StateFlow<CollectionOptionsUiState> = combine(
        observeCollectionTitle(key = key, useCases = useCases),
        observeCollectionSongs(key = key, useCases = useCases),
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

    private fun queue(enqueue: (List<Song>) -> Unit) {
        val songs = uiState.value.songs
        if (songs.isEmpty()) return
        enqueue(songs)
        navigator.navigateBack()
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
