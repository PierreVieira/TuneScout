package com.pierre.tunescout.feature.songs.domain.usecase

import androidx.paging.PagingData
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.feature.songs.domain.repository.SongsRepository
import com.pierre.tunescout.feature.songs.domain.usecase.impl.ObserveIsOnlineUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ObserveIsOnlineUseCaseTest {
    @Test
    fun `GIVEN a connected device WHEN observing THEN reports it as online`() = runTest {
        // Given
        val useCase = ObserveIsOnlineUseCase(FakeSongsRepository(isOnline = true))

        // When
        val observed = useCase()

        // Then
        observed.test {
            assertThat(awaitItem()).isTrue()
            awaitComplete()
        }
    }

    @Test
    fun `GIVEN a device with no connection WHEN observing THEN reports it as offline`() = runTest {
        // Given
        val useCase = ObserveIsOnlineUseCase(FakeSongsRepository(isOnline = false))

        // When
        val observed = useCase()

        // Then
        observed.test {
            assertThat(awaitItem()).isFalse()
            awaitComplete()
        }
    }
}

private class FakeSongsRepository(
    private val isOnline: Boolean,
) : SongsRepository {
    override fun searchSongs(term: String): Flow<PagingData<Song>> = error("unused")

    override fun observeRecentlyPlayed(): Flow<List<Song>> = error("unused")

    override fun observeIsOnline(): Flow<Boolean> = flowOf(isOnline)

    override suspend fun removeFromRecentlyPlayed(songId: Long) {
        error("unused")
    }
}
