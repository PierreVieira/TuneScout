package com.pierre.tunescout.feature.songoptions.domain.usecase

data class SongOptionsUseCases(
    val observeSong: ObserveSong,
    val isRecentlyPlayed: IsRecentlyPlayed,
    val isFavorite: IsFavorite,
    val toggleFavorite: ToggleFavorite,
    val removeFromRecentlyPlayed: RemoveFromRecentlyPlayed,
)
