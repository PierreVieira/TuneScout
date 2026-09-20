package com.pierre.tunescout.core.database.mapper

import com.pierre.tunescout.core.database.entity.AlbumEntity
import com.pierre.tunescout.core.database.entity.SongEntity
import com.pierre.tunescout.core.database.relation.AlbumWithSongs
import com.pierre.tunescout.core.model.Album
import com.pierre.tunescout.core.model.AlbumSummary
import com.pierre.tunescout.core.model.Artwork

internal fun AlbumWithSongs.toAlbum(): Album = Album(
    id = album.id,
    title = album.title,
    artistName = album.artistName,
    artwork = Artwork(album.artworkUrl),
    songs = songs.sortedBy { song -> song.trackNumber }.map { song -> song.toSong() },
    isComplete = true,
)

/**
 * Puts an album together from the tracks the device saved on their own, for an album it never looked
 * up. Every track carries the album's title and cover, so the header draws the same as the full one.
 *
 * @return the album holding only these tracks, or null when there are none.
 */
internal fun List<SongEntity>.toPartialAlbumOrNull(): Album? {
    val sorted = sortedBy { song -> song.trackNumber }
    val first = sorted.firstOrNull() ?: return null
    return Album(
        id = first.albumId,
        title = first.albumTitle,
        artistName = first.artistName,
        artwork = Artwork(first.artworkUrl),
        songs = sorted.map { song -> song.toSong() },
        isComplete = false,
    )
}

internal fun AlbumEntity.toSummary(): AlbumSummary = AlbumSummary(
    id = id,
    title = title,
    artistName = artistName,
    artwork = Artwork(artworkUrl),
)

internal fun Album.toEntity(cachedAt: Long): AlbumEntity = AlbumEntity(
    id = id,
    title = title,
    artistName = artistName,
    artworkUrl = artwork.sourceUrl,
    cachedAt = cachedAt,
)
