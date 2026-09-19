package com.pierre.tunescout.core.playback

interface QueueControls {
    fun skipTo(entryId: String)

    fun removeFromQueue(entryId: String)

    fun moveInQueue(
        fromIndex: Int,
        toIndex: Int,
    )
}
