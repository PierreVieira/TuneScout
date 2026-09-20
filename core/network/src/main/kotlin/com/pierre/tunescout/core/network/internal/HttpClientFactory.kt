package com.pierre.tunescout.core.network.internal

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.expectSuccess
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

internal class HttpClientFactory {
    fun create(engine: HttpClientEngine): HttpClient = HttpClient(engine) {
        expectSuccess = true
        defaultRequest {
            url(BASE_URL)
        }
        install(ContentNegotiation) {
            json(
                json = Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
                    isLenient = true
                    coerceInputValues = true
                },
                contentType = ContentType.Any,
            )
        }
        install(HttpCache)
        install(HttpTimeout) {
            requestTimeoutMillis = 15.seconds.inWholeMilliseconds
            connectTimeoutMillis = 10.seconds.inWholeMilliseconds
        }
    }

    private companion object {
        const val BASE_URL = "https://itunes.apple.com/"
    }
}
