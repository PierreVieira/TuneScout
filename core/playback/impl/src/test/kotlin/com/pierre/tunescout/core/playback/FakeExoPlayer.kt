package com.pierre.tunescout.core.playback

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.pierre.tunescout.core.model.QueueEntry
import io.mockk.every
import io.mockk.mockk

internal class FakeExoPlayer {
    private val timeline = mutableListOf<MediaItem>()

    var currentItemIndex = 0

    val mediaIds: List<String>
        get() = timeline.map { item -> item.mediaId }

    val player: ExoPlayer = mockk(relaxed = true) {
        every { mediaItemCount } answers { timeline.size }
        every { currentMediaItemIndex } answers { currentItemIndex }
        every { repeatMode } returns Player.REPEAT_MODE_OFF
        every { playerError } returns null
        every { setMediaItems(any(), any<Int>(), any<Long>()) } answers {
            timeline.clear()
            timeline += firstArg<List<MediaItem>>()
            currentItemIndex = secondArg()
        }
        every { addMediaItems(any<Int>(), any()) } answers {
            timeline.addAll(firstArg(), secondArg<List<MediaItem>>())
        }
        every { removeMediaItem(any()) } answers { timeline.removeAt(firstArg()) }
        every { moveMediaItem(any(), any()) } answers {
            timeline.add(secondArg(), timeline.removeAt(firstArg()))
        }
        every { seekTo(any<Int>(), any<Long>()) } answers { currentItemIndex = firstArg() }
    }
}

internal fun createTestMediaItem(entry: QueueEntry): MediaItem = MediaItem
    .Builder()
    .setMediaId(entry.id)
    .build()
