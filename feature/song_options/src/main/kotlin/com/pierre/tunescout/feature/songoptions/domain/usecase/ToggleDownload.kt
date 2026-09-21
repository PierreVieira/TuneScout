package com.pierre.tunescout.feature.songoptions.domain.usecase

import com.pierre.tunescout.core.model.Song

fun interface ToggleDownload {
    /**
     * @param isDownloaded whether the song is downloaded now, which is what the toggle takes back.
     * @return whether the song is still downloaded afterwards: taking back its own request leaves it
     * on the device while an album or a playlist that holds it is downloaded too.
     */
    suspend operator fun invoke(
        song: Song,
        isDownloaded: Boolean,
    ): Boolean
}
