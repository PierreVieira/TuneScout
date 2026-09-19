package com.pierre.tunescout.core.database

import com.pierre.tunescout.core.model.PlaybackSession

interface PlaybackSessionLocalDataSource {
    suspend fun save(session: PlaybackSession)

    suspend fun find(): PlaybackSession?
}
