package com.pierre.tunescout.feature.library.domain.usecase

import com.pierre.tunescout.core.model.AlbumSummary
import kotlinx.coroutines.flow.Flow

fun interface ObserveFavoriteAlbums {
    operator fun invoke(): Flow<List<AlbumSummary>>
}
