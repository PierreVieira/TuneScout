package com.pierre.tunescout.feature.songs.domain.usecase

data class SongOptionsUseCases(
    val observeSong: ObserveSong,
    val observeRecentlyPlayed: ObserveRecentlyPlayed,
    val removeFromRecentlyPlayed: RemoveFromRecentlyPlayed,
)
