package com.pierre.tunescout.feature.songoptions.domain.usecase

import com.pierre.tunescout.core.database.PlaylistLocalDataSource
import com.pierre.tunescout.feature.songoptions.domain.usecase.impl.RemoveFromPlaylistUseCase
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class RemoveFromPlaylistUseCaseTest {
    private val playlistLocalDataSource: PlaylistLocalDataSource = mockk(relaxUnitFun = true)

    @Test
    fun `WHEN removing a song from a playlist THEN only that playlist drops it`() = runTest {
        // When
        RemoveFromPlaylistUseCase(playlistLocalDataSource)(playlistId = 7, songId = 1)

        // Then
        coVerify(exactly = 1) { playlistLocalDataSource.removeSong(playlistId = 7, songId = 1) }
    }
}
