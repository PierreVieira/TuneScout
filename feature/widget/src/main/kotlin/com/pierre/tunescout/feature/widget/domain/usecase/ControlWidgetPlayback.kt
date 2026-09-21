package com.pierre.tunescout.feature.widget.domain.usecase

/**
 * The transport a widget button drives. It is not [com.pierre.tunescout.core.playback.TransportControls]
 * directly because a tap on a widget may be the first thing that starts the app's process: the
 * queue the app was left with is only in the player once the database has answered, and every
 * call has to reach the player on the thread it was built on.
 */
interface ControlWidgetPlayback {
    suspend fun togglePlayPause()

    suspend fun skipToNext()

    suspend fun skipToPrevious()

    /**
     * Plays the recently played song [songId] names, with the rest of the recent songs behind it
     * as the queue. A song that is no longer recent does nothing.
     */
    suspend fun playSong(songId: Long)
}
