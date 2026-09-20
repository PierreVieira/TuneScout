package com.pierre.tunescout.core.network.internal

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf

/**
 * Answers every request with the same response and remembers what was asked.
 *
 * @param body what a successful response carries.
 * @param status the response status; from 400 on, the response is an error with no body.
 */
internal class FakeITunesServer(
    body: String = "",
    status: HttpStatusCode = HttpStatusCode.OK,
) {
    val requests = mutableListOf<HttpRequestData>()

    val engine: HttpClientEngine = MockEngine { request ->
        requests += request
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
}
