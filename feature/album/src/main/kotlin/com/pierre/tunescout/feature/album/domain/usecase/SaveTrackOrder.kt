package com.pierre.tunescout.feature.album.domain.usecase

fun interface SaveTrackOrder {
    /** @param songIds every track of the album, in the order the user dragged them into. */
    suspend operator fun invoke(
        albumId: Long,
        songIds: List<Long>,
    )
}
