package com.pierre.tunescout.core.network

import com.pierre.tunescout.core.model.Album

fun interface AlbumRemoteDataSource {
    suspend fun fetchAlbum(albumId: Long): Album?
}
