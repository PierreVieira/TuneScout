package com.pierre.tunescout.core.model

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class CollectionDownloadStateTest {
    @Test
    fun `GIVEN every song on the device but the collection not asked for WHEN reading its state THEN it is not`() {
        // Given
        val statuses = mapOf(1L to SongDownloadStatus.Downloaded, 2L to SongDownloadStatus.Downloaded)

        // When
        val state = CollectionDownloadState.of(isRequested = false, songIds = listOf(1, 2), statuses = statuses)

        // Then
        assertThat(state).isEqualTo(CollectionDownloadState.NotDownloaded)
    }

    @Test
    fun `GIVEN a requested collection with a song on its way WHEN reading its state THEN it counts the rest`() {
        // Given
        val statuses = mapOf(1L to SongDownloadStatus.Downloaded, 2L to SongDownloadStatus.Downloading)

        // When
        val state = CollectionDownloadState.of(isRequested = true, songIds = listOf(1, 2, 3), statuses = statuses)

        // Then
        assertThat(state).isEqualTo(CollectionDownloadState.Downloading(downloadedCount = 1, totalCount = 3))
    }

    @Test
    fun `GIVEN a requested collection with every song on the device WHEN reading its state THEN it is downloaded`() {
        // Given
        val statuses = mapOf(1L to SongDownloadStatus.Downloaded, 2L to SongDownloadStatus.Downloaded)

        // When
        val state = CollectionDownloadState.of(isRequested = true, songIds = listOf(1, 2), statuses = statuses)

        // Then
        assertThat(state).isEqualTo(CollectionDownloadState.Downloaded)
    }

    @Test
    fun `GIVEN a requested collection with no songs WHEN reading its state THEN it is downloaded`() {
        // When
        val state = CollectionDownloadState.of(isRequested = true, songIds = emptyList(), statuses = emptyMap())

        // Then
        assertThat(state).isEqualTo(CollectionDownloadState.Downloaded)
    }

    @Test
    fun `GIVEN each state WHEN reading its progress THEN it is none, the share already there, or all of it`() {
        // When / Then
        assertThat(CollectionDownloadState.NotDownloaded.progress).isNull()
        assertThat(CollectionDownloadState.Downloading(downloadedCount = 1, totalCount = 4).progress).isEqualTo(0.25f)
        assertThat(CollectionDownloadState.Downloaded.progress).isEqualTo(1f)
    }
}
