package com.pierre.tunescout.feature.player.domain.usecase.impl

import com.pierre.tunescout.core.database.SongLocalDataSource
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.feature.player.domain.usecase.ObserveSong
import kotlinx.coroutines.flow.Flow

internal class ObserveSongUseCase(
    private val songLocalDataSource: SongLocalDataSource,
) : ObserveSong {
    override fun invoke(songId: Long): Flow<Song?> = songLocalDataSource.observe(songId)
}
