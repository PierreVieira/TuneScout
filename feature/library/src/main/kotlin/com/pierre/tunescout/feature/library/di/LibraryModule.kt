package com.pierre.tunescout.feature.library.di

import com.pierre.tunescout.feature.library.data.repository.LibraryRepositoryImpl
import com.pierre.tunescout.feature.library.domain.repository.LibraryRepository
import com.pierre.tunescout.feature.library.domain.usecase.CollectionUseCases
import com.pierre.tunescout.feature.library.domain.usecase.CreatePlaylist
import com.pierre.tunescout.feature.library.domain.usecase.DeletePlaylist
import com.pierre.tunescout.feature.library.domain.usecase.LibrarySearchUseCases
import com.pierre.tunescout.feature.library.domain.usecase.LibraryUseCases
import com.pierre.tunescout.feature.library.domain.usecase.ObserveFavorites
import com.pierre.tunescout.feature.library.domain.usecase.ObserveLibraryViewMode
import com.pierre.tunescout.feature.library.domain.usecase.ObservePlaylist
import com.pierre.tunescout.feature.library.domain.usecase.ObservePlaylistSongs
import com.pierre.tunescout.feature.library.domain.usecase.ObservePlaylists
import com.pierre.tunescout.feature.library.domain.usecase.ObserveRecentLibrarySearches
import com.pierre.tunescout.feature.library.domain.usecase.RecordLibrarySearch
import com.pierre.tunescout.feature.library.domain.usecase.RemoveFavorite
import com.pierre.tunescout.feature.library.domain.usecase.RemoveLibrarySearch
import com.pierre.tunescout.feature.library.domain.usecase.RemoveSongFromPlaylist
import com.pierre.tunescout.feature.library.domain.usecase.SetLibraryViewMode
import com.pierre.tunescout.feature.library.domain.usecase.impl.CreatePlaylistUseCase
import com.pierre.tunescout.feature.library.domain.usecase.impl.DeletePlaylistUseCase
import com.pierre.tunescout.feature.library.domain.usecase.impl.ObserveFavoritesUseCase
import com.pierre.tunescout.feature.library.domain.usecase.impl.ObserveLibraryViewModeUseCase
import com.pierre.tunescout.feature.library.domain.usecase.impl.ObservePlaylistSongsUseCase
import com.pierre.tunescout.feature.library.domain.usecase.impl.ObservePlaylistUseCase
import com.pierre.tunescout.feature.library.domain.usecase.impl.ObservePlaylistsUseCase
import com.pierre.tunescout.feature.library.domain.usecase.impl.ObserveRecentLibrarySearchesUseCase
import com.pierre.tunescout.feature.library.domain.usecase.impl.RecordLibrarySearchUseCase
import com.pierre.tunescout.feature.library.domain.usecase.impl.RemoveFavoriteUseCase
import com.pierre.tunescout.feature.library.domain.usecase.impl.RemoveLibrarySearchUseCase
import com.pierre.tunescout.feature.library.domain.usecase.impl.RemoveSongFromPlaylistUseCase
import com.pierre.tunescout.feature.library.domain.usecase.impl.SetLibraryViewModeUseCase
import com.pierre.tunescout.feature.library.presentation.viewmodel.CollectionViewModel
import com.pierre.tunescout.feature.library.presentation.viewmodel.CreatePlaylistViewModel
import com.pierre.tunescout.feature.library.presentation.viewmodel.LibrarySearchViewModel
import com.pierre.tunescout.feature.library.presentation.viewmodel.LibraryViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val libraryModule: Module = module {
    factoryOf(::LibraryRepositoryImpl).bind<LibraryRepository>()
    factoryOf(::ObservePlaylistsUseCase).bind<ObservePlaylists>()
    factoryOf(::ObservePlaylistUseCase).bind<ObservePlaylist>()
    factoryOf(::ObservePlaylistSongsUseCase).bind<ObservePlaylistSongs>()
    factoryOf(::ObserveFavoritesUseCase).bind<ObserveFavorites>()
    factoryOf(::ObserveLibraryViewModeUseCase).bind<ObserveLibraryViewMode>()
    factoryOf(::ObserveRecentLibrarySearchesUseCase).bind<ObserveRecentLibrarySearches>()
    factoryOf(::SetLibraryViewModeUseCase).bind<SetLibraryViewMode>()
    factoryOf(::CreatePlaylistUseCase).bind<CreatePlaylist>()
    factoryOf(::DeletePlaylistUseCase).bind<DeletePlaylist>()
    factoryOf(::RemoveSongFromPlaylistUseCase).bind<RemoveSongFromPlaylist>()
    factoryOf(::RemoveFavoriteUseCase).bind<RemoveFavorite>()
    factoryOf(::RecordLibrarySearchUseCase).bind<RecordLibrarySearch>()
    factoryOf(::RemoveLibrarySearchUseCase).bind<RemoveLibrarySearch>()
    factoryOf(::LibraryUseCases)
    factoryOf(::LibrarySearchUseCases)
    factoryOf(::CollectionUseCases)
    viewModelOf(::LibraryViewModel)
    viewModelOf(::LibrarySearchViewModel)
    viewModelOf(::CollectionViewModel)
    viewModelOf(::CreatePlaylistViewModel)
}
