package com.pierre.tunescout.feature.library.di

import com.pierre.tunescout.feature.library.data.repository.LibraryRepositoryImpl
import com.pierre.tunescout.feature.library.domain.repository.LibraryRepository
import com.pierre.tunescout.feature.library.domain.usecase.CollectionUseCases
import com.pierre.tunescout.feature.library.domain.usecase.CreatePlaylist
import com.pierre.tunescout.feature.library.domain.usecase.DeletePlaylist
import com.pierre.tunescout.feature.library.domain.usecase.LibrarySearchUseCases
import com.pierre.tunescout.feature.library.domain.usecase.LibraryUseCases
import com.pierre.tunescout.feature.library.domain.usecase.ObserveCollectionDownloads
import com.pierre.tunescout.feature.library.domain.usecase.ObserveDownloadedSongs
import com.pierre.tunescout.feature.library.domain.usecase.ObserveFavoriteAlbums
import com.pierre.tunescout.feature.library.domain.usecase.ObserveFavorites
import com.pierre.tunescout.feature.library.domain.usecase.ObserveLibraryViewMode
import com.pierre.tunescout.feature.library.domain.usecase.ObservePlaylist
import com.pierre.tunescout.feature.library.domain.usecase.ObservePlaylistSongs
import com.pierre.tunescout.feature.library.domain.usecase.ObservePlaylists
import com.pierre.tunescout.feature.library.domain.usecase.ObserveRecentLibrarySearches
import com.pierre.tunescout.feature.library.domain.usecase.RecordLibrarySearch
import com.pierre.tunescout.feature.library.domain.usecase.RemoveLibrarySearch
import com.pierre.tunescout.feature.library.domain.usecase.ReorderPlaylistSongs
import com.pierre.tunescout.feature.library.domain.usecase.SetLibraryViewMode
import com.pierre.tunescout.feature.library.domain.usecase.ToggleCollectionDownload
import com.pierre.tunescout.feature.library.domain.usecase.ToggleSongFavorite
import com.pierre.tunescout.feature.library.domain.usecase.impl.CreatePlaylistUseCase
import com.pierre.tunescout.feature.library.domain.usecase.impl.DeletePlaylistUseCase
import com.pierre.tunescout.feature.library.domain.usecase.impl.ObserveCollectionDownloadsUseCase
import com.pierre.tunescout.feature.library.domain.usecase.impl.ObserveDownloadedSongsUseCase
import com.pierre.tunescout.feature.library.domain.usecase.impl.ObserveFavoriteAlbumsUseCase
import com.pierre.tunescout.feature.library.domain.usecase.impl.ObserveFavoritesUseCase
import com.pierre.tunescout.feature.library.domain.usecase.impl.ObserveLibraryViewModeUseCase
import com.pierre.tunescout.feature.library.domain.usecase.impl.ObservePlaylistSongsUseCase
import com.pierre.tunescout.feature.library.domain.usecase.impl.ObservePlaylistUseCase
import com.pierre.tunescout.feature.library.domain.usecase.impl.ObservePlaylistsUseCase
import com.pierre.tunescout.feature.library.domain.usecase.impl.ObserveRecentLibrarySearchesUseCase
import com.pierre.tunescout.feature.library.domain.usecase.impl.RecordLibrarySearchUseCase
import com.pierre.tunescout.feature.library.domain.usecase.impl.RemoveLibrarySearchUseCase
import com.pierre.tunescout.feature.library.domain.usecase.impl.ReorderPlaylistSongsUseCase
import com.pierre.tunescout.feature.library.domain.usecase.impl.SetLibraryViewModeUseCase
import com.pierre.tunescout.feature.library.domain.usecase.impl.ToggleCollectionDownloadUseCase
import com.pierre.tunescout.feature.library.domain.usecase.impl.ToggleSongFavoriteUseCase
import com.pierre.tunescout.feature.library.presentation.mapper.CollectionStreams
import com.pierre.tunescout.feature.library.presentation.mapper.LibraryItemUiModelMapper
import com.pierre.tunescout.feature.library.presentation.viewmodel.CollectionOptionsViewModel
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
    factoryOf(::ObserveFavoriteAlbumsUseCase).bind<ObserveFavoriteAlbums>()
    factoryOf(::ObserveLibraryViewModeUseCase).bind<ObserveLibraryViewMode>()
    factoryOf(::ObserveRecentLibrarySearchesUseCase).bind<ObserveRecentLibrarySearches>()
    factoryOf(::SetLibraryViewModeUseCase).bind<SetLibraryViewMode>()
    factoryOf(::CreatePlaylistUseCase).bind<CreatePlaylist>()
    factoryOf(::DeletePlaylistUseCase).bind<DeletePlaylist>()
    factoryOf(::ReorderPlaylistSongsUseCase).bind<ReorderPlaylistSongs>()
    factoryOf(::ToggleSongFavoriteUseCase).bind<ToggleSongFavorite>()
    factoryOf(::RecordLibrarySearchUseCase).bind<RecordLibrarySearch>()
    factoryOf(::RemoveLibrarySearchUseCase).bind<RemoveLibrarySearch>()
    factoryOf(::ObserveCollectionDownloadsUseCase).bind<ObserveCollectionDownloads>()
    factoryOf(::ToggleCollectionDownloadUseCase).bind<ToggleCollectionDownload>()
    factoryOf(::ObserveDownloadedSongsUseCase).bind<ObserveDownloadedSongs>()
    factoryOf(::LibraryUseCases)
    factoryOf(::LibrarySearchUseCases)
    factoryOf(::CollectionUseCases)
    factoryOf(::CollectionStreams)
    factoryOf(::LibraryItemUiModelMapper)
    viewModelOf(::LibraryViewModel)
    viewModelOf(::LibrarySearchViewModel)
    viewModelOf(::CollectionViewModel)
    viewModelOf(::CollectionOptionsViewModel)
    viewModelOf(::CreatePlaylistViewModel)
}
