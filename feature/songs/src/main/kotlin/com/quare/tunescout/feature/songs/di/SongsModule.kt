package com.quare.tunescout.feature.songs.di

import com.quare.tunescout.feature.songs.data.repository.SongsRepositoryImpl
import com.quare.tunescout.feature.songs.domain.repository.SongsRepository
import com.quare.tunescout.feature.songs.domain.usecase.ObserveRecentlyPlayed
import com.quare.tunescout.feature.songs.domain.usecase.ObserveSong
import com.quare.tunescout.feature.songs.domain.usecase.SearchSongs
import com.quare.tunescout.feature.songs.domain.usecase.impl.ObserveRecentlyPlayedUseCase
import com.quare.tunescout.feature.songs.domain.usecase.impl.ObserveSongUseCase
import com.quare.tunescout.feature.songs.domain.usecase.impl.SearchSongsUseCase
import com.quare.tunescout.feature.songs.presentation.viewmodel.SongOptionsViewModel
import com.quare.tunescout.feature.songs.presentation.viewmodel.SongsViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val songsModule: Module = module {
    factoryOf(::SongsRepositoryImpl) { bind<SongsRepository>() }
    factoryOf(::SearchSongsUseCase) { bind<SearchSongs>() }
    factoryOf(::ObserveRecentlyPlayedUseCase) { bind<ObserveRecentlyPlayed>() }
    factoryOf(::ObserveSongUseCase) { bind<ObserveSong>() }
    viewModelOf(::SongsViewModel)
    viewModelOf(::SongOptionsViewModel)
}
