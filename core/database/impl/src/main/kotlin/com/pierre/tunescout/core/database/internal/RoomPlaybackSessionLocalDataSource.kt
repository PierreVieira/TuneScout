package com.pierre.tunescout.core.database.internal

import com.pierre.tunescout.core.database.PlaybackSessionLocalDataSource
import com.pierre.tunescout.core.database.dao.PlaybackSessionDao
import com.pierre.tunescout.core.database.entity.PLAYBACK_SESSION_ID
import com.pierre.tunescout.core.database.entity.PlaybackQueueEntity
import com.pierre.tunescout.core.database.entity.PlaybackSessionEntity
import com.pierre.tunescout.core.database.mapper.toEntity
import com.pierre.tunescout.core.database.mapper.toSong
import com.pierre.tunescout.core.model.PlaybackContext
import com.pierre.tunescout.core.model.PlaybackSession
import com.pierre.tunescout.core.model.QueueEntry
import com.pierre.tunescout.core.model.QueueSource
import kotlin.time.Duration.Companion.milliseconds

internal class RoomPlaybackSessionLocalDataSource(
    private val playbackSessionDao: PlaybackSessionDao,
    private val timestampProvider: TimestampProvider,
) : PlaybackSessionLocalDataSource {
    override suspend fun save(session: PlaybackSession) {
        val album = session.context as? PlaybackContext.Album
        val cachedAt = timestampProvider.provide()
        playbackSessionDao.save(
            session = PlaybackSessionEntity(
                id = PLAYBACK_SESSION_ID,
                currentEntryId = session.currentEntryId,
                positionMillis = session.position.inWholeMilliseconds,
                isRepeatEnabled = session.isRepeatEnabled,
                contextAlbumId = album?.id,
                contextAlbumTitle = album?.title,
                hasEnded = session.hasEnded,
            ),
            entries = session.entries.mapIndexed { position, entry ->
                PlaybackQueueEntity(
                    entryId = entry.id,
                    position = position,
                    songId = entry.song.id,
                    source = entry.source.name,
                )
            },
            songs = session.entries.map { entry -> entry.song.toEntity(cachedAt = cachedAt) },
        )
    }

    override suspend fun find(): PlaybackSession? {
        val session = playbackSessionDao.findSession() ?: return null
        val rows = playbackSessionDao.findQueue()
        val songs = playbackSessionDao
            .findSongs(rows.map { row -> row.songId }.distinct())
            .associateBy { song -> song.id }
        val entries = rows.mapNotNull { row ->
            val song = songs[row.songId] ?: return@mapNotNull null
            QueueEntry(
                id = row.entryId,
                song = song.toSong(),
                source = QueueSource.entries.firstOrNull { source -> source.name == row.source }
                    ?: QueueSource.Context,
            )
        }
        if (entries.isEmpty()) return null
        return PlaybackSession(
            entries = entries,
            currentEntryId = session.currentEntryId,
            context = session.contextAlbumId?.let { albumId ->
                PlaybackContext.Album(id = albumId, title = session.contextAlbumTitle.orEmpty())
            } ?: PlaybackContext.SingleSong,
            position = session.positionMillis.milliseconds,
            isRepeatEnabled = session.isRepeatEnabled,
            hasEnded = session.hasEnded,
        )
    }
}
