package com.quare.tunescout.feature.songs.domain.usecase.impl

import com.quare.tunescout.core.database.SongLocalDataSource
import com.quare.tunescout.core.model.Song
import com.quare.tunescout.feature.songs.domain.usecase.ObserveSong
import kotlinx.coroutines.flow.Flow

internal class ObserveSongUseCase(
    private val songLocalDataSource: SongLocalDataSource,
) : ObserveSong {
    override fun invoke(songId: Long): Flow<Song?> = songLocalDataSource.observe(songId)
}
