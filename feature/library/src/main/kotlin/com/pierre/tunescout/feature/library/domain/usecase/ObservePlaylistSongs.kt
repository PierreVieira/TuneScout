package com.pierre.tunescout.feature.library.domain.usecase

import com.pierre.tunescout.core.model.Song
import kotlinx.coroutines.flow.Flow

fun interface ObservePlaylistSongs {
    operator fun invoke(playlistId: Long): Flow<List<Song>>
}
