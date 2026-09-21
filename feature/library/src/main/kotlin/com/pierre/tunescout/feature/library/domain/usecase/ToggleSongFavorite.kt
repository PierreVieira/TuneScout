package com.pierre.tunescout.feature.library.domain.usecase

import com.pierre.tunescout.core.model.Song

fun interface ToggleSongFavorite {
    suspend operator fun invoke(
        song: Song,
        isFavorite: Boolean,
    )
}
