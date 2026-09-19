package com.pierre.tunescout.feature.addtoplaylist.domain.usecase.impl

import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.feature.addtoplaylist.domain.repository.AddToPlaylistRepository
import com.pierre.tunescout.feature.addtoplaylist.domain.usecase.AddSongToPlaylist

internal class AddSongToPlaylistUseCase(
    private val repository: AddToPlaylistRepository,
) : AddSongToPlaylist {
    override suspend fun invoke(
        playlistId: Long,
        song: Song,
    ) {
        repository.addSong(playlistId = playlistId, song = song)
    }
}
