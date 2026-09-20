package com.pierre.tunescout.feature.album.di

import com.pierre.tunescout.feature.album.data.repository.AlbumRepositoryImpl
import com.pierre.tunescout.feature.album.domain.repository.AlbumRepository
import com.pierre.tunescout.feature.album.domain.usecase.AlbumUseCases
import com.pierre.tunescout.feature.album.domain.usecase.IsAlbumFavorite
import com.pierre.tunescout.feature.album.domain.usecase.ObserveAlbum
import com.pierre.tunescout.feature.album.domain.usecase.RefreshAlbum
import com.pierre.tunescout.feature.album.domain.usecase.ToggleAlbumFavorite
import com.pierre.tunescout.feature.album.domain.usecase.impl.IsAlbumFavoriteUseCase
import com.pierre.tunescout.feature.album.domain.usecase.impl.ObserveAlbumUseCase
import com.pierre.tunescout.feature.album.domain.usecase.impl.RefreshAlbumUseCase
import com.pierre.tunescout.feature.album.domain.usecase.impl.ToggleAlbumFavoriteUseCase
import com.pierre.tunescout.feature.album.presentation.viewmodel.AlbumOptionsViewModel
import com.pierre.tunescout.feature.album.presentation.viewmodel.AlbumViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import kotlin.time.Duration.Companion.hours

private val albumCacheMaxAge = 1.hours

val albumModule: Module = module {
    factory<AlbumRepository> {
        AlbumRepositoryImpl(
            remoteDataSource = get(),
            albumLocalDataSource = get(),
            cacheMaxAge = albumCacheMaxAge,
        )
    }
    factoryOf(::ObserveAlbumUseCase).bind<ObserveAlbum>()
    factoryOf(::RefreshAlbumUseCase).bind<RefreshAlbum>()
    factoryOf(::IsAlbumFavoriteUseCase).bind<IsAlbumFavorite>()
    factoryOf(::ToggleAlbumFavoriteUseCase).bind<ToggleAlbumFavorite>()
    factoryOf(::AlbumUseCases)
    viewModelOf(::AlbumViewModel)
    viewModelOf(::AlbumOptionsViewModel)
}
