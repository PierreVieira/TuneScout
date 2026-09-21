package com.pierre.tunescout.feature.songoptions.domain.usecase.impl

import com.pierre.tunescout.core.database.DownloadLocalDataSource
import com.pierre.tunescout.feature.songoptions.domain.usecase.IsDownloaded
import kotlinx.coroutines.flow.Flow

internal class IsDownloadedUseCase(
    private val downloadLocalDataSource: DownloadLocalDataSource,
) : IsDownloaded {
    override fun invoke(songId: Long): Flow<Boolean> = downloadLocalDataSource.observeIsWanted(songId)
}
