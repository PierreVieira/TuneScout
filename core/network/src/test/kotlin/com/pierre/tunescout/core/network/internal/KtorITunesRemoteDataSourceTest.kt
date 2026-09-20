package com.pierre.tunescout.core.network.internal

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.network.RemoteException
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class KtorITunesRemoteDataSourceTest {
    private val searchBody =
        """
        {
          "resultCount": 2,
          "results": [
            {
              "wrapperType": "track", "kind": "song", "trackId": 1, "trackName": "Get Lucky",
              "artistName": "Daft Punk", "collectionId": 10, "collectionName": "Random Access Memories",
              "artworkUrl100": "https://example.com/a.jpg", "previewUrl": "https://example.com/p.m4a",
              "trackTimeMillis": 369000, "trackNumber": 8
            },
            {
              "wrapperType": "track", "kind": "music-video", "trackId": 2, "trackName": "Get Lucky (Video)",
              "artistName": "Daft Punk", "collectionId": 10, "previewUrl": "https://example.com/v.m4v"
            }
          ]
        }
        """.trimIndent()

    private val lookupBody =
        """
        {
          "resultCount": 3,
          "results": [
            {
              "wrapperType": "collection", "collectionId": 10, "collectionName": "Random Access Memories",
              "artistName": "Daft Punk", "artworkUrl100": "https://example.com/album.jpg"
            },
            {
              "wrapperType": "track", "kind": "song", "trackId": 2, "trackName": "The Game of Love",
              "artistName": "Daft Punk", "collectionId": 10, "previewUrl": "https://example.com/2.m4a", "trackNumber": 2
            },
            {
              "wrapperType": "track", "kind": "song", "trackId": 1, "trackName": "Give Life Back to Music",
              "artistName": "Daft Punk", "collectionId": 10, "previewUrl": "https://example.com/1.m4a", "trackNumber": 1
            }
          ]
        }
        """.trimIndent()

    private lateinit var dataSource: KtorITunesRemoteDataSource
    private lateinit var requestedUrls: MutableList<Url>

    @Test
    fun `GIVEN a successful search WHEN searching songs THEN returns only the song results`() = runTest {
        // Given
        prepareScenario(body = searchBody)

        // When
        val songs = dataSource.searchSongs(term = "daft punk", limit = 25)

        // Then
        assertThat(songs.map { song -> song.title }).containsExactly("Get Lucky")
    }

    @Test
    fun `GIVEN a search WHEN requesting THEN sends term, country, media, entity and limit`() = runTest {
        // Given
        prepareScenario(body = searchBody, country = "BR")

        // When
        dataSource.searchSongs(term = "daft punk", limit = 25)

        // Then
        val url = requestedUrls.single()
        assertThat(url.encodedPath).isEqualTo("/search")
        assertThat(url.parameters["term"]).isEqualTo("daft punk")
        assertThat(url.parameters["country"]).isEqualTo("BR")
        assertThat(url.parameters["media"]).isEqualTo("music")
        assertThat(url.parameters["entity"]).isEqualTo("song")
        assertThat(url.parameters["limit"]).isEqualTo("25")
    }

    @Test
    fun `GIVEN a lookup response WHEN fetching an album THEN builds the album with its tracks`() = runTest {
        // Given
        prepareScenario(body = lookupBody)

        // When
        val album = dataSource.fetchAlbum(albumId = 10)

        // Then
        assertThat(requestedUrls.single().encodedPath).isEqualTo("/lookup")
        assertThat(requestedUrls.single().parameters["id"]).isEqualTo("10")
        assertThat(album?.title).isEqualTo("Random Access Memories")
        assertThat(album?.songs?.map { song -> song.trackNumber }).containsExactly(1, 2).inOrder()
    }

    @Test
    fun `GIVEN a 429 response WHEN searching THEN throws RateLimited`() = runTest {
        // Given
        prepareScenario(status = HttpStatusCode.TooManyRequests)

        // When / Then
        assertThrows<RemoteException.RateLimited> { dataSource.searchSongs(term = "x", limit = 1) }
    }

    @Test
    fun `GIVEN a 403 response WHEN searching THEN throws RateLimited`() = runTest {
        // Given
        prepareScenario(status = HttpStatusCode.Forbidden)

        // When / Then
        assertThrows<RemoteException.RateLimited> { dataSource.searchSongs(term = "x", limit = 1) }
    }

    @Test
    fun `GIVEN a 500 response WHEN searching THEN throws Unavailable`() = runTest {
        // Given
        prepareScenario(status = HttpStatusCode.InternalServerError)

        // When / Then
        assertThrows<RemoteException.Unavailable> { dataSource.searchSongs(term = "x", limit = 1) }
    }

    @Test
    fun `GIVEN a malformed body WHEN searching THEN throws Unexpected`() = runTest {
        // Given
        prepareScenario(body = "not json")

        // When / Then
        assertThrows<RemoteException.Unexpected> { dataSource.searchSongs(term = "x", limit = 1) }
    }

    private fun prepareScenario(
        body: String = "",
        status: HttpStatusCode = HttpStatusCode.OK,
        country: String = "US",
    ) {
        requestedUrls = mutableListOf()
        val engine = MockEngine { request ->
            requestedUrls += request.url
            if (status.value >= 400) {
                respondError(status)
            } else {
                respond(
                    content = body,
                    status = status,
                    headers = headersOf(HttpHeaders.ContentType, "text/javascript"),
                )
            }
        }
        dataSource = KtorITunesRemoteDataSource(
            client = HttpClientFactory().create(engine),
            countryProvider = { country },
        )
    }
}
