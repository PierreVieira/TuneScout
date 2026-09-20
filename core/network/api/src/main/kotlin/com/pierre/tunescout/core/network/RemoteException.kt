package com.pierre.tunescout.core.network

sealed class RemoteException(
    message: String,
    cause: Throwable?,
) : Exception(message, cause) {
    class RateLimited(
        cause: Throwable?,
    ) : RemoteException("The iTunes API is throttling requests", cause)

    class Unavailable(
        cause: Throwable?,
    ) : RemoteException("The iTunes API could not be reached", cause)

    class Unexpected(
        cause: Throwable?,
    ) : RemoteException("The iTunes API returned an unexpected response", cause)
}
