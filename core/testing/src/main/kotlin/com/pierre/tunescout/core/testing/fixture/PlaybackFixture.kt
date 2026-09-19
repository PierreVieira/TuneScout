package com.pierre.tunescout.core.testing.fixture

import com.pierre.tunescout.core.model.PlaybackContext
import com.pierre.tunescout.core.model.PlaybackState
import com.pierre.tunescout.core.model.PlaybackStatus
import com.pierre.tunescout.core.model.QueueEntry
import com.pierre.tunescout.core.model.QueueSource
import com.pierre.tunescout.core.model.Song
import kotlin.time.Duration

fun queueEntry(
    song: Song = song(),
    id: String = "entry-${song.id}",
    source: QueueSource = QueueSource.Context,
): QueueEntry = QueueEntry(
    id = id,
    song = song,
    source = source,
)

fun queueEntries(
    songs: List<Song>,
    source: QueueSource = QueueSource.Context,
): List<QueueEntry> = songs.map { song -> queueEntry(song = song, source = source) }

fun playbackState(
    songs: List<Song> = listOf(song()),
    entries: List<QueueEntry> = queueEntries(songs),
    currentIndex: Int = 0,
    status: PlaybackStatus = PlaybackStatus.Playing,
    context: PlaybackContext = PlaybackContext.SingleSong,
    position: Duration = Duration.ZERO,
    duration: Duration = Duration.ZERO,
    isRepeatEnabled: Boolean = false,
): PlaybackState = PlaybackState(
    entries = entries,
    currentIndex = currentIndex,
    context = context,
    status = status,
    position = position,
    duration = duration,
    isRepeatEnabled = isRepeatEnabled,
)
