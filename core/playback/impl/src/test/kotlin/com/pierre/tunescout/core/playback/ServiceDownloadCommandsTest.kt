package com.pierre.tunescout.core.playback

import android.content.Context
import android.net.Uri
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.playback.internal.ServiceDownloadCommands
import com.pierre.tunescout.core.playback.internal.SongDownloadService
import com.pierre.tunescout.core.testing.fixture.song
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class ServiceDownloadCommandsTest {
    private val context = mockk<Context>()
    private val downloadManager = mockk<DownloadManager>(relaxed = true)
    private val sent = slot<DownloadRequest>()
    private val previewUri = mockk<Uri>(relaxed = true)
    private lateinit var commands: ServiceDownloadCommands

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `GIVEN a song WHEN fetching it THEN the service gets a download keyed by the song, from its preview`() {
        // Given
        prepareScenario()

        // When
        commands.add(song(id = 7, previewUrl = PREVIEW_URL))

        // Then
        verify { DownloadService.sendAddDownload(context, SongDownloadService::class.java, any(), true) }
        assertThat(sent.captured.id).isEqualTo("7")
        assertThat(sent.captured.uri).isSameInstanceAs(previewUri)
    }

    @Test
    fun `GIVEN the service cannot start from the background WHEN fetching a song THEN the manager takes it directly`() {
        // Given
        prepareScenario(serviceStartFails = true)

        // When
        commands.add(song(id = 7, previewUrl = PREVIEW_URL))

        // Then
        verify { downloadManager.addDownload(match { request -> request.id == "7" }) }
    }

    @Test
    fun `GIVEN a downloaded song WHEN dropping it THEN the manager removes its download`() {
        // Given
        prepareScenario()

        // When
        commands.remove(songId = 7)

        // Then
        verify { downloadManager.removeDownload("7") }
    }

    private fun prepareScenario(serviceStartFails: Boolean = false) {
        mockkStatic(Uri::class, DownloadService::class)
        every { Uri.parse(PREVIEW_URL) } returns previewUri
        val send = every {
            DownloadService.sendAddDownload(context, SongDownloadService::class.java, capture(sent), true)
        }
        if (serviceStartFails) send throws IllegalStateException("background") else send just runs
        commands = ServiceDownloadCommands(context = context, downloadManager = downloadManager)
    }

    private companion object {
        const val PREVIEW_URL = "https://example.com/preview/7.m4a"
    }
}
