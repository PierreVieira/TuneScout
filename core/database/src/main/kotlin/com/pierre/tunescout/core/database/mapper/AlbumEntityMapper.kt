package com.pierre.tunescout.core.database.mapper

import com.pierre.tunescout.core.database.entity.AlbumEntity
import com.pierre.tunescout.core.database.relation.AlbumWithSongs
import com.pierre.tunescout.core.model.Album
import com.pierre.tunescout.core.model.Artwork

internal fun AlbumWithSongs.toAlbum(): Album = Album(
    id = album.id,
    title = album.title,
    artistName = album.artistName,
    artwork = Artwork(album.artworkUrl),
    songs = songs.sortedBy { song -> song.trackNumber }.map { song -> song.toSong() },
)

internal fun Album.toEntity(cachedAt: Long): AlbumEntity = AlbumEntity(
    id = id,
    title = title,
    artistName = artistName,
    artworkUrl = artwork.sourceUrl,
    cachedAt = cachedAt,
)
