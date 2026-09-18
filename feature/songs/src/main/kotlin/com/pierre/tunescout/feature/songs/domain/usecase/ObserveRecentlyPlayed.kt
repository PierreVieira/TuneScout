package com.pierre.tunescout.feature.songs.domain.usecase

import com.pierre.tunescout.core.model.Song
import kotlinx.coroutines.flow.Flow

fun interface ObserveRecentlyPlayed {
    operator fun invoke(): Flow<List<Song>>
}
