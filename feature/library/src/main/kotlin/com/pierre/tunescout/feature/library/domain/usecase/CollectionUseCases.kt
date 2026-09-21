package com.pierre.tunescout.feature.library.domain.usecase

data class CollectionUseCases(
    val observePlaylist: ObservePlaylist,
    val observePlaylistSongs: ObservePlaylistSongs,
    val observeFavorites: ObserveFavorites,
    val toggleSongFavorite: ToggleSongFavorite,
    val deletePlaylist: DeletePlaylist,
)
