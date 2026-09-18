package com.pierre.tunescout.core.utils

import kotlin.coroutines.cancellation.CancellationException

/**
 * [runCatching] for suspend code: a [CancellationException] is rethrown so structured
 * concurrency keeps working, every other failure becomes a [Result.failure].
 */
inline fun <T> suspendRunCatching(block: () -> T): Result<T> = try {
    Result.success(block())
} catch (exception: CancellationException) {
    throw exception
} catch (
    @Suppress("TooGenericExceptionCaught")
    exception: Exception,
) {
    Result.failure(exception)
}
