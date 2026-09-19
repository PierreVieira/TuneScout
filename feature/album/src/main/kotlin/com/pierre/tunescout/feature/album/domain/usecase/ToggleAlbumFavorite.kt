package com.pierre.tunescout.feature.album.domain.usecase

import com.pierre.tunescout.core.model.Album

fun interface ToggleAlbumFavorite {
    suspend operator fun invoke(
        album: Album,
        isFavorite: Boolean,
    )
}
