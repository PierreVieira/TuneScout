package com.pierre.tunescout.feature.songoptions.domain.usecase.impl

import com.pierre.tunescout.core.database.PlaylistLocalDataSource
import com.pierre.tunescout.feature.songoptions.domain.usecase.RemoveFromPlaylist

internal class RemoveFromPlaylistUseCase(
    private val playlistLocalDataSource: PlaylistLocalDataSource,
) : RemoveFromPlaylist {
    override suspend fun invoke(
        playlistId: Long,
        songId: Long,
    ) {
        playlistLocalDataSource.removeSong(playlistId = playlistId, songId = songId)
    }
}
