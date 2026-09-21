package com.pierre.tunescout.feature.album.domain.usecase

import kotlinx.coroutines.flow.Flow

fun interface ObserveFavoriteSongIds {
    operator fun invoke(): Flow<Set<Long>>
}
