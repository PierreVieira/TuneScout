package com.pierre.tunescout.feature.library.domain.usecase

data class CollectionUseCases(
    val observePlaylist: ObservePlaylist,
    val observePlaylistSongs: ObservePlaylistSongs,
    val observeFavorites: ObserveFavorites,
    val removeSongFromPlaylist: RemoveSongFromPlaylist,
    val removeFavorite: RemoveFavorite,
    val deletePlaylist: DeletePlaylist,
)
