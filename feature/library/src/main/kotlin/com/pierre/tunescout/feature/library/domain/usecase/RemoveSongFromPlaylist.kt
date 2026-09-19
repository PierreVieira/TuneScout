package com.pierre.tunescout.feature.library.domain.usecase

fun interface RemoveSongFromPlaylist {
    suspend operator fun invoke(
        playlistId: Long,
        songId: Long,
    )
}
