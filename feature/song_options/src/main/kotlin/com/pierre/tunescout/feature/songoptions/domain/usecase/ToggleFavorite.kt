package com.pierre.tunescout.feature.songoptions.domain.usecase

import com.pierre.tunescout.core.model.Song

fun interface ToggleFavorite {
    suspend operator fun invoke(
        song: Song,
        isFavorite: Boolean,
    )
}
