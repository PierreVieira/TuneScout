package com.pierre.tunescout.feature.songoptions.domain.usecase

fun interface RemoveFromRecentlyPlayed {
    suspend operator fun invoke(songId: Long)
}
