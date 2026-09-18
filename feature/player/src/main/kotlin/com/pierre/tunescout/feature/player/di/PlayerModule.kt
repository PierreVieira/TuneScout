package com.pierre.tunescout.feature.player.di

import com.pierre.tunescout.feature.player.domain.usecase.ObserveSong
import com.pierre.tunescout.feature.player.domain.usecase.impl.ObserveSongUseCase
import com.pierre.tunescout.feature.player.presentation.viewmodel.PlayerViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val playerModule: Module = module {
    factoryOf(::ObserveSongUseCase).bind<ObserveSong>()
    viewModelOf(::PlayerViewModel)
}
