package com.pierre.tunescout.feature.songs.di

import com.pierre.tunescout.feature.songs.data.repository.SongsRepositoryImpl
import com.pierre.tunescout.feature.songs.domain.repository.SongsRepository
import com.pierre.tunescout.feature.songs.domain.usecase.ObserveRecentlyPlayed
import com.pierre.tunescout.feature.songs.domain.usecase.ObserveSong
import com.pierre.tunescout.feature.songs.domain.usecase.SearchSongs
import com.pierre.tunescout.feature.songs.domain.usecase.impl.ObserveRecentlyPlayedUseCase
import com.pierre.tunescout.feature.songs.domain.usecase.impl.ObserveSongUseCase
import com.pierre.tunescout.feature.songs.domain.usecase.impl.SearchSongsUseCase
import com.pierre.tunescout.feature.songs.presentation.viewmodel.SongOptionsViewModel
import com.pierre.tunescout.feature.songs.presentation.viewmodel.SongsViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val songsModule: Module = module {
    factoryOf(::SongsRepositoryImpl).bind<SongsRepository>()
    factoryOf(::SearchSongsUseCase).bind<SearchSongs>()
    factoryOf(::ObserveRecentlyPlayedUseCase).bind<ObserveRecentlyPlayed>()
    factoryOf(::ObserveSongUseCase).bind<ObserveSong>()
    viewModelOf(::SongsViewModel)
    viewModelOf(::SongOptionsViewModel)
}
