package com.pierre.tunescout.feature.library.domain.usecase

import com.pierre.tunescout.core.model.Playlist
import kotlinx.coroutines.flow.Flow

fun interface ObservePlaylist {
    operator fun invoke(playlistId: Long): Flow<Playlist?>
}
