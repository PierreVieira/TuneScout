package com.pierre.tunescout.feature.songoptions.domain.usecase

import com.pierre.tunescout.core.model.Song

fun interface ToggleDownload {
    /**
     * @param isDownloaded whether the song is downloaded now, which is what the toggle takes back —
     * even while an album, a playlist or the liked songs still want it.
     * @return whether the song is downloaded afterwards.
     */
    suspend operator fun invoke(
        song: Song,
        isDownloaded: Boolean,
    ): Boolean
}
