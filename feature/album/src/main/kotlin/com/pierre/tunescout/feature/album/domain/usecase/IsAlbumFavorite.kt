package com.pierre.tunescout.feature.album.domain.usecase

import kotlinx.coroutines.flow.Flow

fun interface IsAlbumFavorite {
    operator fun invoke(albumId: Long): Flow<Boolean>
}
