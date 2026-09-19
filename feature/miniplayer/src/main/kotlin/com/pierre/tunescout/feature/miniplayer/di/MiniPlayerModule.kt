package com.pierre.tunescout.feature.miniplayer.di

import com.pierre.tunescout.feature.miniplayer.presentation.viewmodel.MiniPlayerViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val miniPlayerModule: Module = module {
    viewModelOf(::MiniPlayerViewModel)
}
