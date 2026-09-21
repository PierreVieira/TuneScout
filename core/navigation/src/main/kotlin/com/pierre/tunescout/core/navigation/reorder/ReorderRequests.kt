package com.pierre.tunescout.core.navigation.reorder

import kotlinx.coroutines.flow.Flow

/**
 * How a sheet asks the screen under it to start reordering its songs. The sheet closes as it asks,
 * so the request cannot travel as a route: it is heard by the screen that is already there.
 *
 * The song options sheet is its own feature and reaches a screen of another one, which is why the
 * request lives here, beside the routes both of them already share.
 */
interface ReorderRequests {
    fun request(target: ReorderTarget)

    /**
     * Only a screen collecting when the request is made hears it: one opened later must not start
     * reordering over a request it was never on screen for.
     *
     * @return an emission for every request made for [target].
     */
    fun observe(target: ReorderTarget): Flow<Unit>
}
