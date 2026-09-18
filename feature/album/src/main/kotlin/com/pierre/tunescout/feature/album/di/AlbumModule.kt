package com.pierre.tunescout.feature.album.di

import com.pierre.tunescout.feature.album.data.repository.AlbumRepositoryImpl
import com.pierre.tunescout.feature.album.domain.repository.AlbumRepository
import com.pierre.tunescout.feature.album.domain.usecase.ObserveAlbum
import com.pierre.tunescout.feature.album.domain.usecase.RefreshAlbum
import com.pierre.tunescout.feature.album.domain.usecase.impl.ObserveAlbumUseCase
import com.pierre.tunescout.feature.album.domain.usecase.impl.RefreshAlbumUseCase
import com.pierre.tunescout.feature.album.presentation.viewmodel.AlbumViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val albumModule: Module = module {
    factoryOf(::AlbumRepositoryImpl).bind<AlbumRepository>()
    factoryOf(::ObserveAlbumUseCase).bind<ObserveAlbum>()
    factoryOf(::RefreshAlbumUseCase).bind<RefreshAlbum>()
    viewModelOf(::AlbumViewModel)
}
