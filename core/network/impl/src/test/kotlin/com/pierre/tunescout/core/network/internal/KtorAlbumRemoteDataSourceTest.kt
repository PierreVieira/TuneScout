package com.pierre.tunescout.core.network.internal

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.network.RemoteException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.io.File

class KtorAlbumRemoteDataSourceTest {
    @TempDir
    lateinit var cacheDir: File

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

    private lateinit var server: FakeITunesServer
    private lateinit var dataSource: KtorAlbumRemoteDataSource

    @Test
    fun `GIVEN a lookup response WHEN fetching an album THEN builds the album with its tracks`() = runTest {
        // Given
        prepareScenario(body = lookupBody)

        // When
        val album = dataSource.fetchAlbum(albumId = 10)

        // Then
        assertThat(album?.title).isEqualTo("Random Access Memories")
        assertThat(album?.songs?.map { song -> song.trackNumber }).containsExactly(1, 2).inOrder()
    }

    @Test
    fun `GIVEN an album WHEN fetching it THEN sends id, country and entity to the lookup endpoint`() = runTest {
        // Given
        prepareScenario(body = lookupBody, country = "BR")

        // When
        dataSource.fetchAlbum(albumId = 10)

        // Then
        val url = server.requests.single().url
        assertThat(url.encodedPath).isEqualTo("/lookup")
        assertThat(url.parameters["id"]).isEqualTo("10")
        assertThat(url.parameters["country"]).isEqualTo("BR")
        assertThat(url.parameters["entity"]).isEqualTo("song")
    }

    @Test
    fun `GIVEN a 500 response WHEN fetching an album THEN throws Unavailable`() = runTest {
        // Given
        prepareScenario(status = HttpStatusCode.InternalServerError)

        // When / Then
        assertThrows<RemoteException.Unavailable> { dataSource.fetchAlbum(albumId = 10) }
    }

    private fun prepareScenario(
        body: String = "",
        status: HttpStatusCode = HttpStatusCode.OK,
        country: String = "US",
    ) {
        server = FakeITunesServer(body = body, status = status)
        dataSource = KtorAlbumRemoteDataSource(
            client = HttpClientFactory().create(engine = server.engine, cacheDir = cacheDir),
            countryProvider = { country },
        )
    }
}
