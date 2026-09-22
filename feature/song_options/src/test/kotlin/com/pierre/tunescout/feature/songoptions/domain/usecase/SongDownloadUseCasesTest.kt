package com.pierre.tunescout.feature.songoptions.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.testing.fake.FakeDownloadLocalDataSource
import com.pierre.tunescout.core.testing.fixture.song
import com.pierre.tunescout.feature.songoptions.domain.usecase.impl.IsDownloadedUseCase
import com.pierre.tunescout.feature.songoptions.domain.usecase.impl.ToggleDownloadUseCase
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class SongDownloadUseCasesTest {
    private lateinit var downloads: FakeDownloadLocalDataSource

    @Test
    fun `GIVEN a song not downloaded WHEN toggling it THEN it is asked for and is downloaded`() = runTest {
        // Given
        prepareScenario()
        val song = song(id = 1)

        // When
        val isDownloaded = ToggleDownloadUseCase(downloads)(song = song, isDownloaded = false)

        // Then
        assertThat(isDownloaded).isTrue()
        assertThat(downloads.addedSongs).containsExactly(song)
    }

    @Test
    fun `GIVEN a song downloaded on its own WHEN toggling it THEN it is taken back and no longer downloaded`() =
        runTest {
            // Given
            prepareScenario(wantedIds = listOf(1))

            // When
            val isDownloaded = ToggleDownloadUseCase(downloads)(song = song(id = 1), isDownloaded = true)

            // Then
            assertThat(isDownloaded).isFalse()
            assertThat(downloads.removedSongIds).containsExactly(1L)
        }

    @Test
    fun `GIVEN a wanted song WHEN asking whether it is downloaded THEN it is`() = runTest {
        // Given
        prepareScenario(wantedIds = listOf(1))

        // When / Then
        IsDownloadedUseCase(downloads)(songId = 1).test {
            assertThat(awaitItem()).isTrue()
        }
    }

    private fun prepareScenario(wantedIds: List<Long> = emptyList()) {
        downloads = FakeDownloadLocalDataSource(wanted = wantedIds.map { id -> song(id = id) })
    }
}
