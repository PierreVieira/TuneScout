package com.pierre.tunescout.feature.album.domain.usecase

import com.pierre.tunescout.feature.album.domain.repository.AlbumRepository
import com.pierre.tunescout.feature.album.domain.usecase.impl.SaveTrackOrderUseCase
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class SaveTrackOrderUseCaseTest {
    private lateinit var repository: AlbumRepository

    @Test
    fun `WHEN saving a track order THEN hands it to the repository`() = runTest {
        // Given
        repository = mockk(relaxUnitFun = true)

        // When
        SaveTrackOrderUseCase(repository)(albumId = 10, songIds = listOf(2, 1))

        // Then
        coVerify { repository.saveTrackOrder(albumId = 10, songIds = listOf(2L, 1L)) }
    }
}
