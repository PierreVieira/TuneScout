package com.pierre.tunescout.feature.addtoplaylist.domain.usecase

import com.pierre.tunescout.core.model.Song

fun interface CreatePlaylistWithSong {
    suspend operator fun invoke(
        name: String,
        song: Song,
    )
}
