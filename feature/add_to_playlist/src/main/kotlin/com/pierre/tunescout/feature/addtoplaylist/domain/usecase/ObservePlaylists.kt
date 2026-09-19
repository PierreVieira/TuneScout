package com.pierre.tunescout.feature.addtoplaylist.domain.usecase

import com.pierre.tunescout.core.model.Playlist
import kotlinx.coroutines.flow.Flow

fun interface ObservePlaylists {
    operator fun invoke(): Flow<List<Playlist>>
}
