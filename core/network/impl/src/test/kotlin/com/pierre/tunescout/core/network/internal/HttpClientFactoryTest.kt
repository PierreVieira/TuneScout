package com.pierre.tunescout.core.network.internal

import com.google.common.truth.Truth.assertThat
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.IOException

class HttpClientFactoryTest {
    @TempDir
    lateinit var cacheDir: File

    private val body = """{ "resultCount": 0, "results": [] }"""

    @Test
    fun `GIVEN a cacheable response WHEN requesting it twice THEN reaches the network once`() = runTest {
        // Given
        var networkHits = 0
        val client = HttpClientFactory().create(
            engine = cacheableEngine { networkHits++ },
            cacheDir = cacheDir,
        )

        // When
        client.get(SEARCH_PATH).bodyAsText()
        client.get(SEARCH_PATH).bodyAsText()

        // Then
        assertThat(networkHits).isEqualTo(1)
    }

    @Test
    fun `GIVEN a response cached by a previous client WHEN a new client requests it THEN reads it from disk`() =
        runTest {
            // Given
            val previousClient = HttpClientFactory().create(engine = cacheableEngine {}, cacheDir = cacheDir)
            previousClient.get(SEARCH_PATH).bodyAsText()
            previousClient.close()
            val offlineClient = HttpClientFactory().create(engine = offlineEngine, cacheDir = cacheDir)

            // When
            val response = offlineClient.get(SEARCH_PATH).bodyAsText()

            // Then
            assertThat(response).isEqualTo(body)
        }

    @Test
    fun `GIVEN a missing cache directory WHEN creating the client THEN creates it`() {
        // Given
        val missingDir = File(cacheDir, "http_cache")

        // When
        HttpClientFactory().create(engine = offlineEngine, cacheDir = missingDir)

        // Then
        assertThat(missingDir.isDirectory).isTrue()
    }

    private val offlineEngine: HttpClientEngine
        get() = MockEngine { throw IOException("offline") }

    private fun cacheableEngine(onRequest: () -> Unit): HttpClientEngine = MockEngine {
        onRequest()
        respond(
            content = body,
            status = HttpStatusCode.OK,
            headers = headersOf(
                HttpHeaders.ContentType to listOf("text/javascript"),
                HttpHeaders.CacheControl to listOf("max-age=86400"),
            ),
        )
    }

    private companion object {
        const val SEARCH_PATH = "search"
    }
}
