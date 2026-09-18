package com.pierre.tunescout.feature.album.domain.usecase

import com.pierre.tunescout.core.model.Album
import kotlinx.coroutines.flow.Flow

fun interface ObserveAlbum {
    operator fun invoke(albumId: Long): Flow<Album?>
}
