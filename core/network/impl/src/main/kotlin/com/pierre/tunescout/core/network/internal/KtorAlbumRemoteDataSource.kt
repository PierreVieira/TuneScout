package com.pierre.tunescout.core.network.internal

import com.pierre.tunescout.core.model.Album
import com.pierre.tunescout.core.network.AlbumRemoteDataSource
import com.pierre.tunescout.core.network.CountryProvider
import com.pierre.tunescout.core.network.dto.SearchResponseDto
import com.pierre.tunescout.core.network.mapper.toAlbumOrNull
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

internal class KtorAlbumRemoteDataSource(
    private val client: HttpClient,
    private val countryProvider: CountryProvider,
) : AlbumRemoteDataSource {
    override suspend fun fetchAlbum(albumId: Long): Album? = runRemoteRequest {
        client
            .get(LOOKUP_PATH) {
                parameter("id", albumId)
                parameter("country", countryProvider.provide())
                parameter("entity", "song")
            }.body<SearchResponseDto>()
            .results
            .toAlbumOrNull()
    }

    private companion object {
        const val LOOKUP_PATH = "lookup"
    }
}
