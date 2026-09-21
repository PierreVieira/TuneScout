package com.pierre.tunescout.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.deeplink.DeepLinkMatcher
import com.pierre.tunescout.core.network.NetworkMonitor
import com.pierre.tunescout.core.playback.ObservablePlayback
import com.pierre.tunescout.feature.themeselection.domain.usecase.ObserveDynamicColorEnabled
import com.pierre.tunescout.feature.themeselection.domain.usecase.ObserveTheme
import com.pierre.tunescout.presentation.model.MainUiState
import com.pierre.tunescout.ui.theme.SystemBars
import com.pierre.tunescout.ui.theme.isDark
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val observablePlayback: ObservablePlayback,
    private val deepLinkMatcher: DeepLinkMatcher,
    private val navigator: Navigator,
    observeTheme: ObserveTheme,
    observeDynamicColorEnabled: ObserveDynamicColorEnabled,
    networkMonitor: NetworkMonitor,
) : ViewModel() {
    private val isSystemInDarkTheme = MutableStateFlow<Boolean?>(null)

    /**
     * Started optimistically, and never part of what the splash waits on: the monitor reports the
     * real state as soon as it is collected, and one frame drawn as if online is better than a
     * launch held back for it.
     */
    private val isOnline: StateFlow<Boolean> = networkMonitor
        .observeIsOnline()
        .stateIn(scope = viewModelScope, started = SharingStarted.Eagerly, initialValue = true)

    val uiState: StateFlow<MainUiState> = combine(
        observeTheme(),
        observeDynamicColorEnabled(),
        isSystemInDarkTheme.filterNotNull(),
        isOnline,
    ) { theme, isDynamicColorEnabled, isSystemInDarkTheme, isOnline ->
        MainUiState.Ready(
            theme = theme,
            isDynamicColorEnabled = isDynamicColorEnabled,
            systemBars = SystemBars.of(isDark = theme.isDark(isSystemInDarkTheme)),
            isOffline = !isOnline,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, MainUiState.Loading)

    val requestNotificationPermissionsUiAction: SharedFlow<Unit>
        field = MutableSharedFlow<Unit>()

    init {
        requestNotificationPermissionOnPlayback()
    }

    fun onSystemDarkThemeChanged(isSystemInDarkTheme: Boolean) {
        this.isSystemInDarkTheme.value = isSystemInDarkTheme
    }

    fun onDeepLinkReceived(url: String?) {
        val route = deepLinkMatcher.findRouteOrNull(url) ?: return
        navigator.navigateToDeepLink(route)
    }

    private fun requestNotificationPermissionOnPlayback() {
        viewModelScope.launch {
            observablePlayback.observePlaybackState().first { state -> state.isPlaying }
            requestNotificationPermissionsUiAction.emit(Unit)
        }
    }
}
