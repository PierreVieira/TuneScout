package com.pierre.tunescout.core.playback

import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.playback.internal.DownloadCommands
import com.pierre.tunescout.core.playback.internal.DownloadReconciler
import com.pierre.tunescout.core.playback.internal.TrackedDownloadState
import com.pierre.tunescout.core.testing.fake.FakeDownloadLocalDataSource
import com.pierre.tunescout.core.testing.fixture.song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class DownloadReconcilerTest {
    private lateinit var wanted: MutableStateFlow<List<Song>>
    private lateinit var commands: RecordingDownloadCommands

    @Test
    fun `GIVEN wanted songs the player does not have WHEN starting THEN each one is fetched`() = runTest {
        // Given
        prepareScenario(wantedAtStart = listOf(song(id = 1), song(id = 2)))

        // Then
        assertThat(commands.added).containsExactly(1L, 2L).inOrder()
        assertThat(commands.removed).isEmpty()
    }

    @Test
    fun `GIVEN a wanted song already downloaded WHEN starting THEN it is not fetched again`() = runTest {
        // Given
        prepareScenario(
            wantedAtStart = listOf(song(id = 1)),
            tracked = mapOf(1L to TrackedDownloadState.Completed),
        )

        // Then
        assertThat(commands.added).isEmpty()
    }

    @Test
    fun `GIVEN a download nothing wants any more WHEN starting THEN it is dropped`() = runTest {
        // Given
        prepareScenario(
            wantedAtStart = emptyList(),
            tracked = mapOf(1L to TrackedDownloadState.Completed),
        )

        // Then
        assertThat(commands.removed).containsExactly(1L)
    }

    @Test
    fun `GIVEN a download that failed WHEN the song is still wanted THEN it is asked for again`() = runTest {
        // Given
        prepareScenario(
            wantedAtStart = listOf(song(id = 1)),
            tracked = mapOf(1L to TrackedDownloadState.Failed),
        )

        // Then
        assertThat(commands.added).containsExactly(1L)
    }

    @Test
    fun `GIVEN a song just handed over WHEN another is wanted before it is reported THEN only that one is fetched`() =
        runTest {
            // Given
            prepareScenario(wantedAtStart = listOf(song(id = 1)))

            // When
            wanted.value = listOf(song(id = 1), song(id = 2))
            runCurrent()

            // Then
            assertThat(commands.added).containsExactly(1L, 2L).inOrder()
        }

    @Test
    fun `GIVEN a song just handed over WHEN nothing wants it any more THEN it is dropped`() = runTest {
        // Given
        prepareScenario(wantedAtStart = listOf(song(id = 1)))

        // When
        wanted.value = emptyList()
        runCurrent()

        // Then
        assertThat(commands.removed).containsExactly(1L)
    }

    @Test
    fun `GIVEN a song dropped WHEN it is wanted again THEN it is fetched again`() = runTest {
        // Given
        prepareScenario(wantedAtStart = listOf(song(id = 1)))
        wanted.value = emptyList()
        runCurrent()

        // When
        wanted.value = listOf(song(id = 1))
        runCurrent()

        // Then
        assertThat(commands.added).containsExactly(1L, 1L)
    }

    private fun TestScope.prepareScenario(
        wantedAtStart: List<Song>,
        tracked: Map<Long, TrackedDownloadState> = emptyMap(),
    ) {
        commands = RecordingDownloadCommands()
        DownloadReconciler(
            downloadLocalDataSource = FakeDownloadLocalDataSource(wanted = wantedAtStart).also { downloads ->
                wanted = downloads.wanted
            },
            tracker = { tracked },
            downloadCommands = commands,
        ).start(backgroundScope)
        runCurrent()
    }
}

private class RecordingDownloadCommands : DownloadCommands {
    val added = mutableListOf<Long>()
    val removed = mutableListOf<Long>()

    override fun add(song: Song) {
        added += song.id
    }

    override fun remove(songId: Long) {
        removed += songId
    }
}
