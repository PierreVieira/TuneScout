package com.pierre.tunescout.feature.songs.domain.usecase

data class SongsUseCases(
    val searchSongs: SearchSongs,
    val observeRecentlyPlayed: ObserveRecentlyPlayed,
    val removeFromRecentlyPlayed: RemoveFromRecentlyPlayed,
    val observeIsOnline: ObserveIsOnline,
)
