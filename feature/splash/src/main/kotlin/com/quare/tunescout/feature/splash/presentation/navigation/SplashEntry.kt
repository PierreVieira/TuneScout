package com.quare.tunescout.feature.splash.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.quare.tunescout.core.navigation.route.SplashRoute
import com.quare.tunescout.feature.splash.presentation.content.SplashContent
import com.quare.tunescout.feature.splash.presentation.viewmodel.SplashViewModel
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.splashEntry() {
    entry<SplashRoute> {
        koinViewModel<SplashViewModel>()
        SplashContent()
    }
}
