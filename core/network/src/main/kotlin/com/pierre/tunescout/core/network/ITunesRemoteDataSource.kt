package com.pierre.tunescout.core.network

import com.pierre.tunescout.core.model.Album
import com.pierre.tunescout.core.model.Song

interface ITunesRemoteDataSource {
    suspend fun searchSongs(
        term: String,
        limit: Int,
        forceRefresh: Boolean,
    ): List<Song>

    suspend fun fetchAlbum(albumId: Long): Album?
}
