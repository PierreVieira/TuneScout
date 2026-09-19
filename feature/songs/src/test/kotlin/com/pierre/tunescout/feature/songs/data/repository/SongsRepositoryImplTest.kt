package com.pierre.tunescout.feature.songs.data.repository

import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.database.RecentlyPlayedLocalDataSource
import com.pierre.tunescout.core.database.SongLocalDataSource
import com.pierre.tunescout.core.model.Album
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.network.ITunesRemoteDataSource
import com.pierre.tunescout.core.testing.fixture.song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class SongsRepositoryImplTest {
    private lateinit var repository: SongsRepositoryImpl
    private lateinit var remoteDataSource: FakeRemoteDataSource
    private lateinit var songLocalDataSource: FakeSongLocalDataSource
    private lateinit var recentlyPlayedLocalDataSource: FakeRecentlyPlayedLocalDataSource

    @Test
    fun `GIVEN a catalog WHEN searching THEN the first page comes from the remote and is stored`() = runTest {
        // Given
        val catalog = (1..3L).map { id -> song(id = id) }
        prepareScenario(catalog = catalog)

        // When
        val snapshot = repository.searchSongs(term = "daft punk").asSnapshot()

        // Then
        assertThat(snapshot).containsExactlyElementsIn(catalog).inOrder()
        assertThat(remoteDataSource.requestedTerms).containsExactly("daft punk")
        assertThat(songLocalDataSource.saved).containsExactlyElementsIn(catalog).inOrder()
    }

    @Test
    fun `GIVEN recently played songs WHEN observing THEN asks for the list the screen shows`() = runTest {
        // Given
        val played = listOf(song(id = 2), song(id = 1))
        prepareScenario(recentlyPlayed = played)

        // When
        val observed = repository.observeRecentlyPlayed()

        // Then
        observed.test {
            assertThat(awaitItem()).containsExactlyElementsIn(played).inOrder()
            awaitComplete()
        }
        assertThat(recentlyPlayedLocalDataSource.requestedLimits).containsExactly(RECENTLY_PLAYED_LIMIT)
    }

    @Test
    fun `GIVEN a recently played song WHEN removing it THEN the local data source drops it`() = runTest {
        // Given
        prepareScenario()

        // When
        repository.removeFromRecentlyPlayed(songId = 7)

        // Then
        assertThat(recentlyPlayedLocalDataSource.removedSongIds).containsExactly(7L)
    }

    private fun prepareScenario(
        catalog: List<Song> = emptyList(),
        recentlyPlayed: List<Song> = emptyList(),
    ) {
        remoteDataSource = FakeRemoteDataSource(catalog = catalog)
        songLocalDataSource = FakeSongLocalDataSource()
        recentlyPlayedLocalDataSource = FakeRecentlyPlayedLocalDataSource(recentlyPlayed = recentlyPlayed)
        repository = SongsRepositoryImpl(
            remoteDataSource = remoteDataSource,
            songLocalDataSource = songLocalDataSource,
            recentlyPlayedLocalDataSource = recentlyPlayedLocalDataSource,
        )
    }

    private companion object {
        const val RECENTLY_PLAYED_LIMIT = 20
    }
}

private class FakeRemoteDataSource(
    private val catalog: List<Song>,
) : ITunesRemoteDataSource {
    val requestedTerms = mutableSetOf<String>()

    override suspend fun searchSongs(
        term: String,
        limit: Int,
    ): List<Song> {
        requestedTerms += term
        return catalog.take(limit)
    }

    override suspend fun fetchAlbum(albumId: Long): Album? = error("unused")
}

private class FakeSongLocalDataSource : SongLocalDataSource {
    val saved = mutableListOf<Song>()

    override suspend fun save(songs: List<Song>) {
        saved += songs
    }

    override fun observe(songId: Long): Flow<Song?> = error("unused")

    override suspend fun find(songId: Long): Song? = error("unused")
}

private class FakeRecentlyPlayedLocalDataSource(
    private val recentlyPlayed: List<Song>,
) : RecentlyPlayedLocalDataSource {
    val requestedLimits = mutableListOf<Int>()
    val removedSongIds = mutableListOf<Long>()

    override fun observe(limit: Int): Flow<List<Song>> {
        requestedLimits += limit
        return flowOf(recentlyPlayed)
    }

    override fun observeIsRecentlyPlayed(songId: Long): Flow<Boolean> = error("unused")

    override suspend fun record(song: Song) {
        error("unused")
    }

    override suspend fun remove(songId: Long) {
        removedSongIds += songId
    }
}
