package com.pierre.tunescout.feature.songoptions.domain.usecase

data class SongOptionsUseCases(
    val observeSong: ObserveSong,
    val isFavorite: IsFavorite,
    val toggleFavorite: ToggleFavorite,
    val removeFromPlaylist: RemoveFromPlaylist,
)
