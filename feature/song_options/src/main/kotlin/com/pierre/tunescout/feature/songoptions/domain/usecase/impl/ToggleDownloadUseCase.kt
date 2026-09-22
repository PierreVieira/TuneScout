package com.pierre.tunescout.feature.songoptions.domain.usecase.impl

import com.pierre.tunescout.core.database.DownloadLocalDataSource
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.feature.songoptions.domain.usecase.ToggleDownload

internal class ToggleDownloadUseCase(
    private val downloadLocalDataSource: DownloadLocalDataSource,
) : ToggleDownload {
    override suspend fun invoke(
        song: Song,
        isDownloaded: Boolean,
    ): Boolean {
        if (isDownloaded) {
            downloadLocalDataSource.removeSong(song.id)
        } else {
            downloadLocalDataSource.addSong(song)
        }
        return !isDownloaded
    }
}
