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

    var repeatMode = Player.REPEAT_MODE_OFF

    var shuffleModeEnabled = false

    val mediaIds: List<String>
        get() = timeline.map { item -> item.mediaId }

    val player: ExoPlayer = mockk(relaxed = true) {
        every { mediaItemCount } answers { timeline.size }
        every { currentMediaItemIndex } answers { currentItemIndex }
        every { repeatMode } answers { this@FakeExoPlayer.repeatMode }
        every { repeatMode = any() } answers { this@FakeExoPlayer.repeatMode = firstArg() }
        every { shuffleModeEnabled } answers { this@FakeExoPlayer.shuffleModeEnabled }
        every { shuffleModeEnabled = any() } answers { this@FakeExoPlayer.shuffleModeEnabled = firstArg() }
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
        every { removeMediaItems(any(), any()) } answers { timeline.subList(firstArg(), secondArg()).clear() }
        every { replaceMediaItems(any(), any(), any()) } answers {
            val fromIndex = firstArg<Int>()
            val toIndex = secondArg<Int>()
            val items = thirdArg<List<MediaItem>>()
            val range = timeline.subList(fromIndex, toIndex)
            range.clear()
            range.addAll(items)
            if (currentItemIndex >= toIndex) currentItemIndex += items.size - (toIndex - fromIndex)
        }
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
