package com.pierre.tunescout.core.database.mapper

import com.pierre.tunescout.core.database.entity.AlbumEntity
import com.pierre.tunescout.core.database.entity.SongEntity
import com.pierre.tunescout.core.database.relation.AlbumWithSongs
import com.pierre.tunescout.core.model.Album
import com.pierre.tunescout.core.model.AlbumSummary
import com.pierre.tunescout.core.model.Artwork

/**
 * @param trackOrder where the user put each track, by song id; empty for an album never reordered.
 * @return the whole album, its tracks in the user's order or else in the album's own.
 */
internal fun AlbumWithSongs.toAlbum(trackOrder: Map<Long, Int> = emptyMap()): Album = Album(
    id = album.id,
    title = album.title,
    artistName = album.artistName,
    artwork = Artwork(album.artworkUrl),
    songs = songs.sortByTrackOrder(trackOrder).map { song -> song.toSong() },
    isComplete = true,
)

/**
 * Puts an album together from the tracks the device saved on their own, for an album it never looked
 * up. Every track carries the album's title and cover, so the header draws the same as the full one.
 *
 * @param trackOrder where the user put each track, by song id; empty for an album never reordered.
 * @return the album holding only these tracks, or null when there are none.
 */
internal fun List<SongEntity>.toPartialAlbumOrNull(trackOrder: Map<Long, Int> = emptyMap()): Album? {
    val sorted = sortByTrackOrder(trackOrder)
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

/**
 * The order the user chose comes first; a track it does not know — one the album gained after it
 * was reordered — follows, in the album's own order.
 *
 * @return the tracks in that order.
 */
private fun List<SongEntity>.sortByTrackOrder(trackOrder: Map<Long, Int>): List<SongEntity> = sortedWith(
    compareBy<SongEntity> { song -> trackOrder[song.id] ?: Int.MAX_VALUE }.thenBy { song -> song.trackNumber },
)

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
