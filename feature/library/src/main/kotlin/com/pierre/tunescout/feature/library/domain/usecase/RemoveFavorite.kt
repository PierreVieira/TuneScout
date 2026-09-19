package com.pierre.tunescout.feature.library.domain.usecase

fun interface RemoveFavorite {
    suspend operator fun invoke(songId: Long)
}
