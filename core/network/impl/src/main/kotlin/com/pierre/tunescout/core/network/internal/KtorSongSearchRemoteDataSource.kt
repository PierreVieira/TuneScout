package com.pierre.tunescout.core.network.internal

import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.network.CountryProvider
import com.pierre.tunescout.core.network.SongSearchRemoteDataSource
import com.pierre.tunescout.core.network.dto.SearchResponseDto
import com.pierre.tunescout.core.network.mapper.toSongOrNull
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.HttpHeaders

internal class KtorSongSearchRemoteDataSource(
    private val client: HttpClient,
    private val countryProvider: CountryProvider,
) : SongSearchRemoteDataSource {
    override suspend fun searchSongs(
        term: String,
        limit: Int,
        forceRefresh: Boolean,
    ): List<Song> = runRemoteRequest {
        client
            .get(SEARCH_PATH) {
                parameter("term", term)
                parameter("country", countryProvider.provide())
                parameter("media", "music")
                parameter("entity", "song")
                parameter("limit", limit)
                if (forceRefresh) header(HttpHeaders.CacheControl, "no-cache")
            }.body<SearchResponseDto>()
            .results
            .mapNotNull { result -> result.toSongOrNull() }
    }

    private companion object {
        const val SEARCH_PATH = "search"
    }
}
