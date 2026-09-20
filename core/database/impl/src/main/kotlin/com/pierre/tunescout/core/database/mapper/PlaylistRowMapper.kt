package com.pierre.tunescout.core.database.mapper

import com.pierre.tunescout.core.database.relation.PlaylistRow
import com.pierre.tunescout.core.model.Artwork
import com.pierre.tunescout.core.model.Playlist

internal fun List<PlaylistRow>.toPlaylists(): List<Playlist> = groupBy(PlaylistRow::playlistId)
    .map { (playlistId, rows) -> rows.toPlaylist(playlistId) }

private fun List<PlaylistRow>.toPlaylist(playlistId: Long): Playlist {
    val artworkUrls = mapNotNull(PlaylistRow::artworkUrl)
    return Playlist(
        id = playlistId,
        name = first().name,
        songCount = artworkUrls.size,
        artworks = artworkUrls.map(::Artwork),
    )
}
