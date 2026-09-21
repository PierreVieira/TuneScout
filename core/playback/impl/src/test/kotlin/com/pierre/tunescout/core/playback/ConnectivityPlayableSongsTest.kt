package com.pierre.tunescout.core.playback

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.SongDownloadStatus
import com.pierre.tunescout.core.playback.internal.ConnectivityPlayableSongs
import com.pierre.tunescout.core.testing.fixture.song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ConnectivityPlayableSongsTest {
    private val cached = song(id = 1)
    private val notCached = song(id = 2)
    private val downloaded = song(id = 3)
    private lateinit var isOnline: MutableStateFlow<Boolean>
    private lateinit var downloadStatuses: MutableStateFlow<Map<Long, SongDownloadStatus>>
    private lateinit var playableSongs: ConnectivityPlayableSongs

    @Test
    fun `GIVEN the device is online WHEN asking about a song with no preview on it THEN it can play`() = runTest {
        // Given
        prepareScenario(isOnlineAtStart = true)

        // When
        val isPlayable = playableSongs.isPlayable(notCached)

        // Then
        assertThat(isPlayable).isTrue()
    }

    @Test
    fun `GIVEN the device is offline WHEN asking about a song with no preview on it THEN it cannot play`() = runTest {
        // Given
        prepareScenario(isOnlineAtStart = false)

        // When
        val isPlayable = playableSongs.isPlayable(notCached)

        // Then
        assertThat(isPlayable).isFalse()
    }

    @Test
    fun `GIVEN the device is offline WHEN asking about a song whose preview is on it THEN it can play`() = runTest {
        // Given
        prepareScenario(isOnlineAtStart = false)

        // When
        val isPlayable = playableSongs.isPlayable(cached)

        // Then
        assertThat(isPlayable).isTrue()
    }

    @Test
    fun `GIVEN the connection went away WHEN filtering songs THEN keeps only the ones on the device`() = runTest {
        // Given
        prepareScenario(isOnlineAtStart = true)
        isOnline.value = false
        runCurrent()

        // When
        val playable = playableSongs.filterPlayable(listOf(cached, notCached))

        // Then
        assertThat(playable).containsExactly(cached)
    }

    @Test
    fun `GIVEN songs being followed WHEN the connection goes away THEN the answer says what is left`() = runTest {
        // Given
        prepareScenario(isOnlineAtStart = true)

        // When
        val answers = mutableListOf<Set<Long>>()
        backgroundScope.launch {
            playableSongs.observePlayableSongs().collect { playable ->
                answers += playable.findUnplayableIds(listOf(cached, notCached))
            }
        }
        runCurrent()
        isOnline.value = false
        runCurrent()

        // Then
        assertThat(answers).containsExactly(emptySet<Long>(), setOf(notCached.id)).inOrder()
    }

    @Test
    fun `GIVEN the device is offline WHEN asking about a song the user downloaded THEN it can play`() = runTest {
        // Given
        prepareScenario(
            isOnlineAtStart = false,
            downloadsAtStart = mapOf(downloaded.id to SongDownloadStatus.Downloaded),
        )

        // When
        val isPlayable = playableSongs.isPlayable(downloaded)

        // Then
        assertThat(isPlayable).isTrue()
    }

    @Test
    fun `GIVEN the device is offline WHEN asking about a song still downloading THEN it cannot play`() = runTest {
        // Given
        prepareScenario(
            isOnlineAtStart = false,
            downloadsAtStart = mapOf(downloaded.id to SongDownloadStatus.Downloading),
        )

        // When
        val isPlayable = playableSongs.isPlayable(downloaded)

        // Then
        assertThat(isPlayable).isFalse()
    }

    @Test
    fun `GIVEN songs followed offline WHEN a download completes THEN the answer counts it in`() = runTest {
        // Given
        prepareScenario(
            isOnlineAtStart = false,
            downloadsAtStart = mapOf(downloaded.id to SongDownloadStatus.Downloading),
        )

        // When
        val answers = mutableListOf<Set<Long>>()
        backgroundScope.launch {
            playableSongs.observePlayableSongs().collect { playable ->
                answers += playable.findUnplayableIds(listOf(cached, downloaded))
            }
        }
        runCurrent()
        downloadStatuses.value = mapOf(downloaded.id to SongDownloadStatus.Downloaded)
        runCurrent()

        // Then
        assertThat(answers).containsExactly(setOf(downloaded.id), emptySet<Long>()).inOrder()
    }

    private fun TestScope.prepareScenario(
        isOnlineAtStart: Boolean,
        downloadsAtStart: Map<Long, SongDownloadStatus> = emptyMap(),
    ) {
        isOnline = MutableStateFlow(isOnlineAtStart)
        downloadStatuses = MutableStateFlow(downloadsAtStart)
        playableSongs = ConnectivityPlayableSongs(
            previewCache = { song -> song.id == cached.id },
            networkMonitor = { isOnline },
            observableDownloads = { downloadStatuses },
            scope = backgroundScope,
        )
        runCurrent()
    }
}
