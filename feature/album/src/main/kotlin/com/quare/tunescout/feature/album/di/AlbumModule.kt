package com.quare.tunescout.feature.album.di

import com.quare.tunescout.feature.album.data.repository.AlbumRepositoryImpl
import com.quare.tunescout.feature.album.domain.repository.AlbumRepository
import com.quare.tunescout.feature.album.domain.usecase.ObserveAlbum
import com.quare.tunescout.feature.album.domain.usecase.RefreshAlbum
import com.quare.tunescout.feature.album.domain.usecase.impl.ObserveAlbumUseCase
import com.quare.tunescout.feature.album.domain.usecase.impl.RefreshAlbumUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val albumModule: Module = module {
    factoryOf(::AlbumRepositoryImpl) { bind<AlbumRepository>() }
    factoryOf(::ObserveAlbumUseCase) { bind<ObserveAlbum>() }
    factoryOf(::RefreshAlbumUseCase) { bind<RefreshAlbum>() }
}
