package com.pierre.tunescout.core.network.internal

import com.pierre.tunescout.core.model.Album
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.network.CountryProvider
import com.pierre.tunescout.core.network.ITunesRemoteDataSource
import com.pierre.tunescout.core.network.RemoteException
import com.pierre.tunescout.core.network.dto.SearchResponseDto
import com.pierre.tunescout.core.network.mapper.toAlbumOrNull
import com.pierre.tunescout.core.network.mapper.toSongOrNull
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.HttpStatusCode
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

internal class KtorITunesRemoteDataSource(
    private val client: HttpClient,
    private val countryProvider: CountryProvider,
) : ITunesRemoteDataSource {
    override suspend fun searchSongs(
        term: String,
        limit: Int,
    ): List<Song> = request {
        client
            .get(SEARCH_PATH) {
                parameter("term", term)
                parameter("country", countryProvider.provide())
                parameter("media", "music")
                parameter("entity", "song")
                parameter("limit", limit)
            }.body<SearchResponseDto>()
            .results
            .mapNotNull { result -> result.toSongOrNull() }
    }

    override suspend fun fetchAlbum(albumId: Long): Album? = request {
        client
            .get(LOOKUP_PATH) {
                parameter("id", albumId)
                parameter("country", countryProvider.provide())
                parameter("entity", "song")
            }.body<SearchResponseDto>()
            .results
            .toAlbumOrNull()
    }

    private inline fun <T> request(block: () -> T): T = try {
        block()
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: ClientRequestException) {
        throw exception.toRemoteException()
    } catch (exception: ServerResponseException) {
        throw RemoteException.Unavailable(exception)
    } catch (exception: IOException) {
        throw RemoteException.Unavailable(exception)
    } catch (
        @Suppress("TooGenericExceptionCaught")
        exception: Exception,
    ) {
        throw RemoteException.Unexpected(exception)
    }

    private fun ClientRequestException.toRemoteException(): RemoteException = when (response.status) {
        HttpStatusCode.TooManyRequests, HttpStatusCode.Forbidden -> RemoteException.RateLimited(this)
        else -> RemoteException.Unexpected(this)
    }

    private companion object {
        const val SEARCH_PATH = "search"
        const val LOOKUP_PATH = "lookup"
    }
}
