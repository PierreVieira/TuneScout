package com.pierre.tunescout.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pierre.tunescout.core.playback.PlaybackController
import com.pierre.tunescout.presentation.model.MainUiAction
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainViewModel(
    private val playbackController: PlaybackController,
) : ViewModel() {
    val uiAction: SharedFlow<MainUiAction>
        field = MutableSharedFlow<MainUiAction>()

    init {
        requestNotificationPermissionOnPlayback()
    }

    private fun requestNotificationPermissionOnPlayback() {
        viewModelScope.launch {
            playbackController.state.first { state -> state.isPlaying }
            uiAction.emit(MainUiAction.RequestNotificationPermission)
        }
    }
}
