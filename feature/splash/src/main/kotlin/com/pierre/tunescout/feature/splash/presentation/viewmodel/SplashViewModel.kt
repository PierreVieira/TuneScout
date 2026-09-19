package com.pierre.tunescout.feature.splash.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pierre.tunescout.core.navigation.Navigator
import com.pierre.tunescout.core.navigation.route.SongsRoute
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private val defaultHoldDuration = 700.milliseconds

class SplashViewModel(
    private val navigator: Navigator,
    private val holdDuration: Duration = defaultHoldDuration,
) : ViewModel() {
    init {
        viewModelScope.launch {
            delay(holdDuration)
            navigator.navigateReplacingTop(SongsRoute)
        }
    }
}
