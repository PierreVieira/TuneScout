package com.pierre.tunescout.feature.songoptions.domain.usecase

fun interface RemoveFromPlaylist {
    suspend operator fun invoke(
        playlistId: Long,
        songId: Long,
    )
}
