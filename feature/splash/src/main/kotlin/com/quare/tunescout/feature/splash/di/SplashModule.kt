package com.quare.tunescout.feature.splash.di

import com.quare.tunescout.feature.splash.presentation.viewmodel.SplashViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val splashModule: Module = module {
    viewModel { SplashViewModel(navigator = get()) }
}
