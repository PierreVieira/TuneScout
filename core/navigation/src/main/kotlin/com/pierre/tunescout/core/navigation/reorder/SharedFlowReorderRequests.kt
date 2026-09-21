package com.pierre.tunescout.core.navigation.reorder

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

class SharedFlowReorderRequests : ReorderRequests {
    /** No replay, so a request nobody was collecting for is dropped rather than kept for later. */
    private val requests = MutableSharedFlow<ReorderTarget>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override fun request(target: ReorderTarget) {
        requests.tryEmit(target)
    }

    override fun observe(target: ReorderTarget): Flow<Unit> = requests
        .filter { requested -> requested == target }
        .map { }
}
