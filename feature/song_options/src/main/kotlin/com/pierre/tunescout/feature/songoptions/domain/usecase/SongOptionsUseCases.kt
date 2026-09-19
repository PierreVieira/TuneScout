package com.pierre.tunescout.feature.songoptions.domain.usecase

data class SongOptionsUseCases(
    val observeSong: ObserveSong,
    val isRecentlyPlayed: IsRecentlyPlayed,
    val removeFromRecentlyPlayed: RemoveFromRecentlyPlayed,
)
