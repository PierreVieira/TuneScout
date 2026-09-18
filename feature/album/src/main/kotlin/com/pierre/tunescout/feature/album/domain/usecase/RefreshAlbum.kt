package com.pierre.tunescout.feature.album.domain.usecase

fun interface RefreshAlbum {
    suspend operator fun invoke(albumId: Long): Result<Unit>
}
