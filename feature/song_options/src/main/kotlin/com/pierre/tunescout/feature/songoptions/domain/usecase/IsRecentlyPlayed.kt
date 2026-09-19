package com.pierre.tunescout.feature.songoptions.domain.usecase

import kotlinx.coroutines.flow.Flow

fun interface IsRecentlyPlayed {
    operator fun invoke(songId: Long): Flow<Boolean>
}
