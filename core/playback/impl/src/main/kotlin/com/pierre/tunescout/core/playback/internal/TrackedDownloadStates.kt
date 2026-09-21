package com.pierre.tunescout.core.playback.internal

/** The downloads the player already has, read once they are all known. */
internal fun interface TrackedDownloadStates {
    /** @return every download by song, waiting for the stored ones to be read first. */
    suspend fun awaitTrackedStates(): Map<Long, TrackedDownloadState>
}
