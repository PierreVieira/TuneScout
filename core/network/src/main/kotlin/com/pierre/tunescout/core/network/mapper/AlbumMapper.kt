package com.pierre.tunescout.core.network.mapper

import com.pierre.tunescout.core.model.Album
import com.pierre.tunescout.core.network.dto.ResultDto

private const val COLLECTION_WRAPPER = "collection"

internal fun List<ResultDto>.toAlbumOrNull(): Album? {
    val collection = firstOrNull { result -> result.wrapperType == COLLECTION_WRAPPER } ?: return null
    val songs = mapNotNull { result -> result.toSongOrNull() }.sortedBy { song -> song.trackNumber }
    return Album(
        id = collection.collectionId ?: return null,
        title = collection.collectionName ?: return null,
        artistName = collection.artistName ?: return null,
        artworkUrl = collection.artworkUrl100.orEmpty(),
        songs = songs,
    )
}
