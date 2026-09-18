package com.quare.tunescout.feature.player.domain.usecase

import com.quare.tunescout.core.model.Song
import kotlinx.coroutines.flow.Flow

fun interface ObserveSong {
    operator fun invoke(songId: Long): Flow<Song?>
}
