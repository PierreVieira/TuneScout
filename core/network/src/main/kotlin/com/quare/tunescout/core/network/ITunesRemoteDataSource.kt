package com.quare.tunescout.core.network

import com.quare.tunescout.core.model.Album
import com.quare.tunescout.core.model.Song

interface ITunesRemoteDataSource {
    suspend fun searchSongs(
        term: String,
        limit: Int,
    ): List<Song>

    suspend fun fetchAlbum(albumId: Long): Album?
}
