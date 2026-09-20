package com.pierre.tunescout.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pierre.tunescout.core.playback.ObservablePlayback
import com.pierre.tunescout.feature.themeselection.domain.usecase.ObserveDynamicColorEnabled
import com.pierre.tunescout.feature.themeselection.domain.usecase.ObserveTheme
import com.pierre.tunescout.presentation.model.MainUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val observablePlayback: ObservablePlayback,
    observeTheme: ObserveTheme,
    observeDynamicColorEnabled: ObserveDynamicColorEnabled,
) : ViewModel() {
    val uiState: StateFlow<MainUiState> = combine(
        observeTheme(),
        observeDynamicColorEnabled(),
        MainUiState::Ready,
    ).stateIn(viewModelScope, SharingStarted.Eagerly, MainUiState.Loading)

    val requestNotificationPermissionsUiAction: SharedFlow<Unit>
        field = MutableSharedFlow<Unit>()

    init {
        requestNotificationPermissionOnPlayback()
    }

    private fun requestNotificationPermissionOnPlayback() {
        viewModelScope.launch {
            observablePlayback.observePlaybackState().first { state -> state.isPlaying }
            requestNotificationPermissionsUiAction.emit(Unit)
        }
    }
}
