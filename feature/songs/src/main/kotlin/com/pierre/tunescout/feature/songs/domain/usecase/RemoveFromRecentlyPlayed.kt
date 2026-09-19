package com.pierre.tunescout.feature.songs.domain.usecase

fun interface RemoveFromRecentlyPlayed {
    suspend operator fun invoke(songId: Long)
}
