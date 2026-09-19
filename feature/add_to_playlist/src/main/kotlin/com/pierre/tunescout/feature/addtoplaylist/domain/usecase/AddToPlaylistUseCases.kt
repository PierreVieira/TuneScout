package com.pierre.tunescout.feature.addtoplaylist.domain.usecase

data class AddToPlaylistUseCases(
    val observePlaylists: ObservePlaylists,
    val observeSong: ObserveSong,
    val addSongToPlaylist: AddSongToPlaylist,
    val createPlaylistWithSong: CreatePlaylistWithSong,
)
