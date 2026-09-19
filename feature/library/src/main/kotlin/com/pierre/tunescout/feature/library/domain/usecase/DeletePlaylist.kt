package com.pierre.tunescout.feature.library.domain.usecase

fun interface DeletePlaylist {
    suspend operator fun invoke(playlistId: Long)
}
