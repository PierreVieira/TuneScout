package com.pierre.tunescout.feature.addtoplaylist.di

import com.pierre.tunescout.feature.addtoplaylist.data.repository.AddToPlaylistRepositoryImpl
import com.pierre.tunescout.feature.addtoplaylist.domain.repository.AddToPlaylistRepository
import com.pierre.tunescout.feature.addtoplaylist.domain.usecase.AddSongToPlaylist
import com.pierre.tunescout.feature.addtoplaylist.domain.usecase.AddToPlaylistUseCases
import com.pierre.tunescout.feature.addtoplaylist.domain.usecase.CreatePlaylistWithSong
import com.pierre.tunescout.feature.addtoplaylist.domain.usecase.ObservePlaylists
import com.pierre.tunescout.feature.addtoplaylist.domain.usecase.ObserveSong
import com.pierre.tunescout.feature.addtoplaylist.domain.usecase.impl.AddSongToPlaylistUseCase
import com.pierre.tunescout.feature.addtoplaylist.domain.usecase.impl.CreatePlaylistWithSongUseCase
import com.pierre.tunescout.feature.addtoplaylist.domain.usecase.impl.ObservePlaylistsUseCase
import com.pierre.tunescout.feature.addtoplaylist.domain.usecase.impl.ObserveSongUseCase
import com.pierre.tunescout.feature.addtoplaylist.presentation.viewmodel.AddToPlaylistViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val addToPlaylistModule: Module = module {
    factoryOf(::AddToPlaylistRepositoryImpl).bind<AddToPlaylistRepository>()
    factoryOf(::ObservePlaylistsUseCase).bind<ObservePlaylists>()
    factoryOf(::ObserveSongUseCase).bind<ObserveSong>()
    factoryOf(::AddSongToPlaylistUseCase).bind<AddSongToPlaylist>()
    factoryOf(::CreatePlaylistWithSongUseCase).bind<CreatePlaylistWithSong>()
    factoryOf(::AddToPlaylistUseCases)
    viewModelOf(::AddToPlaylistViewModel)
}
