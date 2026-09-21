package com.pierre.tunescout.feature.library.domain.usecase

fun interface ReorderPlaylistSongs {
    /** @param songIds every song of the playlist, in the order the user dragged them into. */
    suspend operator fun invoke(
        playlistId: Long,
        songIds: List<Long>,
    )
}
