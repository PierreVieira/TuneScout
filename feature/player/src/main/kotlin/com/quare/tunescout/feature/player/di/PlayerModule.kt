package com.quare.tunescout.feature.player.di

import com.quare.tunescout.feature.player.domain.usecase.ObserveSong
import com.quare.tunescout.feature.player.domain.usecase.impl.ObserveSongUseCase
import com.quare.tunescout.feature.player.presentation.viewmodel.PlayerViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val playerModule: Module = module {
    factoryOf(::ObserveSongUseCase) { bind<ObserveSong>() }
    viewModelOf(::PlayerViewModel)
}
