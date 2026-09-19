package com.pierre.tunescout.feature.songoptions.domain.usecase

import kotlinx.coroutines.flow.Flow

fun interface IsFavorite {
    operator fun invoke(songId: Long): Flow<Boolean>
}
