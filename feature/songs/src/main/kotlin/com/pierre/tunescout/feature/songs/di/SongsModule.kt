package com.pierre.tunescout.feature.songs.di

import com.pierre.tunescout.feature.songs.data.repository.SongsRepositoryImpl
import com.pierre.tunescout.feature.songs.domain.repository.SongsRepository
import com.pierre.tunescout.feature.songs.domain.usecase.ObserveFavoriteSongIds
import com.pierre.tunescout.feature.songs.domain.usecase.ObserveIsOnline
import com.pierre.tunescout.feature.songs.domain.usecase.ObserveRecentlyPlayed
import com.pierre.tunescout.feature.songs.domain.usecase.RemoveFromRecentlyPlayed
import com.pierre.tunescout.feature.songs.domain.usecase.SearchSongs
import com.pierre.tunescout.feature.songs.domain.usecase.SongsUseCases
import com.pierre.tunescout.feature.songs.domain.usecase.ToggleSongFavorite
import com.pierre.tunescout.feature.songs.domain.usecase.impl.ObserveFavoriteSongIdsUseCase
import com.pierre.tunescout.feature.songs.domain.usecase.impl.ObserveIsOnlineUseCase
import com.pierre.tunescout.feature.songs.domain.usecase.impl.ObserveRecentlyPlayedUseCase
import com.pierre.tunescout.feature.songs.domain.usecase.impl.RemoveFromRecentlyPlayedUseCase
import com.pierre.tunescout.feature.songs.domain.usecase.impl.SearchSongsUseCase
import com.pierre.tunescout.feature.songs.domain.usecase.impl.ToggleSongFavoriteUseCase
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
    factoryOf(::RemoveFromRecentlyPlayedUseCase).bind<RemoveFromRecentlyPlayed>()
    factoryOf(::ObserveIsOnlineUseCase).bind<ObserveIsOnline>()
    factoryOf(::ObserveFavoriteSongIdsUseCase).bind<ObserveFavoriteSongIds>()
    factoryOf(::ToggleSongFavoriteUseCase).bind<ToggleSongFavorite>()
    factoryOf(::SongsUseCases)
    viewModelOf(::SongsViewModel)
}
