package com.pierre.tunescout.core.playback

interface QueueControls {
    fun skipTo(entryId: String)

    fun removeFromQueue(entryId: String)

    /** Drops every entry but the one playing now, which keeps playing undisturbed. */
    fun clearQueue()

    fun moveInQueue(
        fromIndex: Int,
        toIndex: Int,
    )
}
