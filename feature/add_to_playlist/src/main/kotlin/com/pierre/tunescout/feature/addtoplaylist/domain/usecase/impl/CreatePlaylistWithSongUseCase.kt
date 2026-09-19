package com.pierre.tunescout.feature.addtoplaylist.domain.usecase.impl

import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.feature.addtoplaylist.domain.repository.AddToPlaylistRepository
import com.pierre.tunescout.feature.addtoplaylist.domain.usecase.CreatePlaylistWithSong

internal class CreatePlaylistWithSongUseCase(
    private val repository: AddToPlaylistRepository,
) : CreatePlaylistWithSong {
    override suspend fun invoke(
        name: String,
        song: Song,
    ) {
        val playlistId = repository.createPlaylist(name)
        repository.addSong(playlistId = playlistId, song = song)
    }
}
