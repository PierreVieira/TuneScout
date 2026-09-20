package com.pierre.tunescout.core.network.internal

import com.pierre.tunescout.core.network.RemoteException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

/**
 * Runs a call to the iTunes API and turns whatever Ktor throws into a [RemoteException], so no Ktor
 * type crosses the module boundary.
 *
 * @return what [block] returns.
 */
internal inline fun <T> runRemoteRequest(block: () -> T): T = try {
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

internal fun ClientRequestException.toRemoteException(): RemoteException = when (response.status) {
    HttpStatusCode.TooManyRequests, HttpStatusCode.Forbidden -> RemoteException.RateLimited(this)
    else -> RemoteException.Unexpected(this)
}
