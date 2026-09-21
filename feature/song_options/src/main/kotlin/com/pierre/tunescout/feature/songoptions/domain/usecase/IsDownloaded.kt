package com.pierre.tunescout.feature.songoptions.domain.usecase

import kotlinx.coroutines.flow.Flow

fun interface IsDownloaded {
    /** @return whether anything keeps the song on the device: its own request or a collection's. */
    operator fun invoke(songId: Long): Flow<Boolean>
}
