package com.pierre.tunescout.feature.album.domain.usecase

data class AlbumUseCases(
    val observeAlbum: ObserveAlbum,
    val refreshAlbum: RefreshAlbum,
    val isAlbumFavorite: IsAlbumFavorite,
    val toggleAlbumFavorite: ToggleAlbumFavorite,
    val observeIsOnline: ObserveIsOnline,
    val observeFavoriteSongIds: ObserveFavoriteSongIds,
    val toggleSongFavorite: ToggleSongFavorite,
)
