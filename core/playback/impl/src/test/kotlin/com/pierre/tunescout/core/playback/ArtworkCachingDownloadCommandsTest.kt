package com.pierre.tunescout.core.playback

import android.content.Context
import android.util.Log
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.ImageResult
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.Artwork
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.playback.internal.ArtworkCachingDownloadCommands
import com.pierre.tunescout.core.playback.internal.DownloadCommands
import com.pierre.tunescout.core.testing.fixture.song
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ArtworkCachingDownloadCommandsTest {
    private val context = mockk<Context>()
    private val imageLoader = mockk<ImageLoader>()
    private val delegate = RecordingArtworkDownloadCommands()

    @Test
    fun `GIVEN a song WHEN adding it THEN the delegate still fetches its audio`() = runTest {
        // Given
        prepareSuccessfulPrefetch()
        val commands = ArtworkCachingDownloadCommands(delegate, imageLoader, context, backgroundScope)

        // When
        commands.add(song(id = 7))
        runCurrent()

        // Then
        assertThat(delegate.added).containsExactly(7L)
    }

    @Test
    fun `GIVEN a song WHEN adding it THEN every artwork size the app draws is prefetched`() = runTest {
        // Given
        val requests = prepareSuccessfulPrefetch()
        val commands = ArtworkCachingDownloadCommands(delegate, imageLoader, context, backgroundScope)
        val addedSong = song(id = 7, artwork = Artwork(sourceUrl = "https://example.com/art/7/100x100bb.jpg"))

        // When
        commands.add(addedSong)
        runCurrent()

        // Then
        assertThat(requests.map { request -> request.data })
            .containsExactly(
                "https://example.com/art/7/200x200bb.jpg",
                "https://example.com/art/7/600x600bb.jpg",
                "https://example.com/art/7/1000x1000bb.jpg",
            ).inOrder()
    }

    @Test
    fun `GIVEN one artwork size fails to fetch WHEN adding a song THEN the other sizes are still fetched`() = runTest {
        // Given
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        val requests = slot<ImageRequest>()
        val calls = mutableListOf<ImageRequest>()
        coEvery { imageLoader.execute(capture(requests)) } answers {
            calls += requests.captured
            if (calls.size == 1) throw RuntimeException("boom") else mockk<ImageResult>(relaxed = true)
        }
        val commands = ArtworkCachingDownloadCommands(delegate, imageLoader, context, backgroundScope)

        // When
        commands.add(song(id = 7))
        runCurrent()

        // Then
        assertThat(calls).hasSize(3)
        unmockkStatic(Log::class)
    }

    @Test
    fun `GIVEN a song id WHEN removing it THEN only the delegate is asked to drop it, artwork untouched`() = runTest {
        // Given
        val commands = ArtworkCachingDownloadCommands(delegate, imageLoader, context, backgroundScope)

        // When
        commands.remove(songId = 7)
        runCurrent()

        // Then
        assertThat(delegate.removed).containsExactly(7L)
    }

    private fun prepareSuccessfulPrefetch(): MutableList<ImageRequest> {
        val requests = mutableListOf<ImageRequest>()
        coEvery { imageLoader.execute(capture(requests)) } returns mockk<ImageResult>(relaxed = true)
        return requests
    }
}

private class RecordingArtworkDownloadCommands : DownloadCommands {
    val added = mutableListOf<Long>()
    val removed = mutableListOf<Long>()

    override fun add(song: Song) {
        added += song.id
    }

    override fun remove(songId: Long) {
        removed += songId
    }
}
