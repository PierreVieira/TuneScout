package com.pierre.tunescout.feature.songoptions.di

import com.pierre.tunescout.feature.songoptions.domain.usecase.IsFavorite
import com.pierre.tunescout.feature.songoptions.domain.usecase.IsRecentlyPlayed
import com.pierre.tunescout.feature.songoptions.domain.usecase.ObserveSong
import com.pierre.tunescout.feature.songoptions.domain.usecase.RemoveFromRecentlyPlayed
import com.pierre.tunescout.feature.songoptions.domain.usecase.SongOptionsUseCases
import com.pierre.tunescout.feature.songoptions.domain.usecase.ToggleFavorite
import com.pierre.tunescout.feature.songoptions.domain.usecase.impl.IsFavoriteUseCase
import com.pierre.tunescout.feature.songoptions.domain.usecase.impl.IsRecentlyPlayedUseCase
import com.pierre.tunescout.feature.songoptions.domain.usecase.impl.ObserveSongUseCase
import com.pierre.tunescout.feature.songoptions.domain.usecase.impl.RemoveFromRecentlyPlayedUseCase
import com.pierre.tunescout.feature.songoptions.domain.usecase.impl.ToggleFavoriteUseCase
import com.pierre.tunescout.feature.songoptions.presentation.viewmodel.SongOptionsViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val songOptionsModule: Module = module {
    factoryOf(::ObserveSongUseCase).bind<ObserveSong>()
    factoryOf(::IsRecentlyPlayedUseCase).bind<IsRecentlyPlayed>()
    factoryOf(::IsFavoriteUseCase).bind<IsFavorite>()
    factoryOf(::ToggleFavoriteUseCase).bind<ToggleFavorite>()
    factoryOf(::RemoveFromRecentlyPlayedUseCase).bind<RemoveFromRecentlyPlayed>()
    factoryOf(::SongOptionsUseCases)
    viewModelOf(::SongOptionsViewModel)
}
