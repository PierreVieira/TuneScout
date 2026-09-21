package com.pierre.tunescout.core.playback

import android.net.Uri
import android.util.Log
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadCursor
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadRequest
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.SongDownloadStatus
import com.pierre.tunescout.core.playback.internal.SongDownloadTracker
import com.pierre.tunescout.core.playback.internal.TrackedDownloadState
import com.pierre.tunescout.core.utils.DispatcherProvider
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.slot
import io.mockk.unmockkStatic
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.io.IOException

class SongDownloadTrackerTest {
    private val listener = slot<DownloadManager.Listener>()
    private lateinit var downloadManager: DownloadManager
    private lateinit var tracker: SongDownloadTracker

    @Test
    fun `GIVEN downloads stored from an earlier run WHEN observing THEN each song has the status its download is in`() =
        runTest {
            // Given
            prepareScenario(
                stored = listOf(
                    download(songId = 1, state = Download.STATE_COMPLETED),
                    download(songId = 2, state = Download.STATE_QUEUED),
                    download(songId = 3, state = Download.STATE_DOWNLOADING),
                    download(songId = 4, state = Download.STATE_FAILED),
                ),
            )

            // When / Then
            tracker.observeDownloadStatuses().test {
                assertThat(awaitItem()).containsExactly(
                    1L,
                    SongDownloadStatus.Downloaded,
                    2L,
                    SongDownloadStatus.Downloading,
                    3L,
                    SongDownloadStatus.Downloading,
                )
            }
        }

    @Test
    fun `GIVEN a failed download WHEN reading what the player has THEN it is still there, marked failed`() = runTest {
        // Given
        prepareScenario(stored = listOf(download(songId = 4, state = Download.STATE_FAILED)))

        // When
        val states = tracker.awaitTrackedStates()

        // Then
        assertThat(states).containsExactly(4L, TrackedDownloadState.Failed)
    }

    @Test
    fun `GIVEN a download on its way WHEN it completes THEN the song is downloaded`() = runTest {
        // Given
        prepareScenario(stored = listOf(download(songId = 1, state = Download.STATE_DOWNLOADING)))

        // When
        listener.captured.onDownloadChanged(
            downloadManager,
            download(songId = 1, state = Download.STATE_COMPLETED),
            null,
        )

        // Then
        assertThat(tracker.awaitTrackedStates()).containsExactly(1L, TrackedDownloadState.Completed)
    }

    @Test
    fun `GIVEN a download WHEN it is being removed or is gone THEN the song has none`() = runTest {
        // Given
        prepareScenario(
            stored = listOf(
                download(songId = 1, state = Download.STATE_COMPLETED),
                download(songId = 2, state = Download.STATE_COMPLETED),
            ),
        )

        // When
        listener.captured.onDownloadChanged(
            downloadManager,
            download(songId = 1, state = Download.STATE_REMOVING),
            null,
        )
        listener.captured.onDownloadRemoved(downloadManager, download(songId = 2, state = Download.STATE_REMOVING))

        // Then
        assertThat(tracker.awaitTrackedStates()).isEmpty()
    }

    @Test
    fun `GIVEN a change reported while the index is still being read WHEN it is read THEN the change wins`() = runTest {
        // Given
        prepareScenario(stored = listOf(download(songId = 1, state = Download.STATE_DOWNLOADING)), readsIndex = false)

        // When
        listener.captured.onDownloadChanged(
            downloadManager,
            download(songId = 1, state = Download.STATE_COMPLETED),
            null,
        )
        runCurrent()

        // Then
        assertThat(tracker.awaitTrackedStates()).containsExactly(1L, TrackedDownloadState.Completed)
    }

    @Test
    fun `GIVEN a download that is not a song's WHEN it changes THEN it is ignored`() = runTest {
        // Given
        prepareScenario(stored = listOf(download(id = "not-a-song", state = Download.STATE_COMPLETED)))

        // When
        listener.captured.onDownloadChanged(
            downloadManager,
            download(id = "other", state = Download.STATE_COMPLETED),
            null,
        )
        listener.captured.onDownloadRemoved(
            downloadManager,
            download(id = "other", state = Download.STATE_COMPLETED),
        )

        // Then
        assertThat(tracker.awaitTrackedStates()).isEmpty()
    }

    @Test
    fun `GIVEN an index that cannot be read WHEN observing THEN no song is downloaded rather than no answer`() =
        runTest {
            // Given
            mockkStatic(Log::class)
            every { Log.e(any(), any()) } returns 0
            prepareScenario(stored = emptyList(), indexFails = true)

            // When / Then
            tracker.observeDownloadStatuses().test {
                assertThat(awaitItem()).isEmpty()
            }
            unmockkStatic(Log::class)
        }

    private fun TestScope.prepareScenario(
        stored: List<Download>,
        readsIndex: Boolean = true,
        indexFails: Boolean = false,
    ) {
        downloadManager = mockk {
            every { addListener(capture(listener)) } just runs
            every { downloadIndex.getDownloads() } answers {
                if (indexFails) throw IOException("corrupted") else FakeDownloadCursor(stored)
            }
        }
        val io = StandardTestDispatcher(testScheduler)
        tracker = SongDownloadTracker(
            downloadManager = downloadManager,
            dispatcherProvider = TestDispatcherProvider(io),
        )
        tracker.start(backgroundScope)
        if (readsIndex) runCurrent()
    }

    private fun download(
        songId: Long = 0,
        id: String = songId.toString(),
        state: Int,
    ): Download = Download(
        DownloadRequest.Builder(id, mockk<Uri>(relaxed = true)).build(),
        state,
        0,
        0,
        0,
        Download.STOP_REASON_NONE,
        if (state == Download.STATE_FAILED) Download.FAILURE_REASON_UNKNOWN else Download.FAILURE_REASON_NONE,
    )
}

private class FakeDownloadCursor(
    private val downloads: List<Download>,
) : DownloadCursor {
    private var position = -1
    private var isClosed = false

    override fun getDownload(): Download = downloads[position]

    override fun getCount(): Int = downloads.size

    override fun getPosition(): Int = position

    override fun moveToPosition(position: Int): Boolean {
        this.position = position
        return position in downloads.indices
    }

    override fun isClosed(): Boolean = isClosed

    override fun close() {
        isClosed = true
    }
}

private class TestDispatcherProvider(
    dispatcher: CoroutineDispatcher,
) : DispatcherProvider {
    override val io: CoroutineDispatcher = dispatcher
    override val default: CoroutineDispatcher = dispatcher
    override val main: CoroutineDispatcher = dispatcher
}
