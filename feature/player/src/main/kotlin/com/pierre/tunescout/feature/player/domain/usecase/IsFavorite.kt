package com.pierre.tunescout.feature.player.domain.usecase

import kotlinx.coroutines.flow.Flow

fun interface IsFavorite {
    operator fun invoke(songId: Long): Flow<Boolean>
}
