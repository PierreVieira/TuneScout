package com.quare.tunescout.feature.album.data.repository

internal class AlbumNotFoundException(
    albumId: Long,
) : Exception("Album $albumId was not found in the iTunes catalog")
