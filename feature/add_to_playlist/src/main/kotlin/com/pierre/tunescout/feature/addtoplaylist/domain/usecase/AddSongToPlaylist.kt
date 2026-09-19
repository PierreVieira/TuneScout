package com.pierre.tunescout.feature.addtoplaylist.domain.usecase

import com.pierre.tunescout.core.model.Song

fun interface AddSongToPlaylist {
    suspend operator fun invoke(
        playlistId: Long,
        song: Song,
    )
}
