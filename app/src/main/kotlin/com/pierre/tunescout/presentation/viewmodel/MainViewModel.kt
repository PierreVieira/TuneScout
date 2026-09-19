package com.pierre.tunescout.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pierre.tunescout.core.playback.PlaybackController
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainViewModel(
    private val playbackController: PlaybackController,
) : ViewModel() {
    val requestNotificationPermissionsUiAction: SharedFlow<Unit>
        field = MutableSharedFlow<Unit>()

    init {
        requestNotificationPermissionOnPlayback()
    }

    private fun requestNotificationPermissionOnPlayback() {
        viewModelScope.launch {
            playbackController.state.first { state -> state.isPlaying }
            requestNotificationPermissionsUiAction.emit(Unit)
        }
    }
}
