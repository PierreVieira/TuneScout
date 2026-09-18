package com.pierre.tunescout.core.database.mapper

import com.pierre.tunescout.core.database.entity.AlbumEntity
import com.pierre.tunescout.core.database.relation.AlbumWithSongs
import com.pierre.tunescout.core.model.Album

internal fun AlbumWithSongs.toAlbum(): Album = Album(
    id = album.id,
    title = album.title,
    artistName = album.artistName,
    artworkUrl = album.artworkUrl,
    songs = songs.sortedBy { song -> song.trackNumber }.map { song -> song.toSong() },
)

internal fun Album.toEntity(cachedAt: Long): AlbumEntity = AlbumEntity(
    id = id,
    title = title,
    artistName = artistName,
    artworkUrl = artworkUrl,
    cachedAt = cachedAt,
)
