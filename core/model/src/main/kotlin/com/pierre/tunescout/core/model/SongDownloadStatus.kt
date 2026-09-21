package com.pierre.tunescout.core.model

/**
 * How far a song the user asked to keep on the device has got. A song nobody asked for has no
 * status at all, so a list tells the three cases apart by looking the song up and finding nothing.
 */
enum class SongDownloadStatus {
    /** Waiting for its turn or for a connection, or on its way to the device. */
    Downloading,

    /** The whole preview is on the device, where clearing the app's cache cannot reach it. */
    Downloaded,
}
