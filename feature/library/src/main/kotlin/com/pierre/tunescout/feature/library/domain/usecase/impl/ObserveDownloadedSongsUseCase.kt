package com.pierre.tunescout.feature.library.domain.usecase.impl

import com.pierre.tunescout.core.database.DownloadLocalDataSource
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.feature.library.domain.usecase.ObserveDownloadedSongs
import kotlinx.coroutines.flow.Flow

internal class ObserveDownloadedSongsUseCase(
    private val downloadLocalDataSource: DownloadLocalDataSource,
) : ObserveDownloadedSongs {
    override fun invoke(): Flow<List<Song>> = downloadLocalDataSource.observeOwnSongs()
}
