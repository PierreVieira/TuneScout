package com.quare.tunescout.feature.songs.data.paging

import androidx.paging.PagingConfig
import androidx.paging.PagingSource.LoadResult
import androidx.paging.testing.TestPager
import com.google.common.truth.Truth.assertThat
import com.quare.tunescout.core.database.SongLocalDataSource
import com.quare.tunescout.core.model.Album
import com.quare.tunescout.core.model.Song
import com.quare.tunescout.core.network.ITunesRemoteDataSource
import com.quare.tunescout.core.network.RemoteException
import com.quare.tunescout.core.testing.fixture.song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

private const val PAGE_SIZE = 25

class SearchSongsPagingSourceTest {
    private lateinit var pagingSource: SearchSongsPagingSource
    private lateinit var pager: TestPager<Int, Song>
    private lateinit var remoteDataSource: FakeRemoteDataSource
    private lateinit var songLocalDataSource: FakeSongLocalDataSource

    @Test
    fun `GIVEN a first page WHEN refreshing THEN requests the page size and delivers everything`() = runTest {
        // Given
        prepareScenario(catalog = songs(count = 60))

        // When
        val page = pager.refresh() as LoadResult.Page

        // Then
        assertThat(remoteDataSource.requestedLimits).containsExactly(PAGE_SIZE)
        assertThat(page.data).hasSize(PAGE_SIZE)
        assertThat(page.nextKey).isEqualTo(PAGE_SIZE)
    }

    @Test
    fun `GIVEN a delivered page WHEN appending THEN asks a bigger limit and keeps only the new tail`() = runTest {
        // Given
        prepareScenario(catalog = songs(count = 60))
        pager.refresh()

        // When
        val page = pager.append() as LoadResult.Page

        // Then
        assertThat(remoteDataSource.requestedLimits).containsExactly(PAGE_SIZE, PAGE_SIZE * 2).inOrder()
        assertThat(page.data.map { song -> song.id }).isEqualTo((26L..50L).toList())
        assertThat(page.nextKey).isEqualTo(PAGE_SIZE * 2)
    }

    @Test
    fun `GIVEN fewer results than the limit WHEN loading THEN marks the end of the list`() = runTest {
        // Given
        prepareScenario(catalog = songs(count = 10))

        // When
        val page = pager.refresh() as LoadResult.Page

        // Then
        assertThat(page.data).hasSize(10)
        assertThat(page.nextKey).isNull()
    }

    @Test
    fun `GIVEN the API cap is reached WHEN loading THEN never asks beyond 200 and marks the end`() = runTest {
        // Given
        prepareScenario(catalog = songs(count = 300))
        pager.refresh()
        repeat(6) { pager.append() }

        // When
        val page = pager.append() as LoadResult.Page

        // Then
        assertThat(remoteDataSource.requestedLimits.last()).isEqualTo(MAX_SEARCH_RESULTS)
        assertThat(page.nextKey).isNull()
    }

    @Test
    fun `GIVEN a song repeated across pages WHEN appending THEN delivers it only once`() = runTest {
        // Given
        val catalog = songs(count = 30).toMutableList().also { list -> list[27] = song(id = 3) }
        prepareScenario(catalog = catalog)
        pager.refresh()

        // When
        val page = pager.append() as LoadResult.Page

        // Then
        assertThat(page.data.map { song -> song.id }).doesNotContain(3L)
    }

    @Test
    fun `GIVEN loaded songs WHEN loading THEN caches them locally`() = runTest {
        // Given
        prepareScenario(catalog = songs(count = 5))

        // When
        pager.refresh()

        // Then
        assertThat(songLocalDataSource.saved.map { song -> song.id }).containsExactly(1L, 2L, 3L, 4L, 5L)
    }

    @Test
    fun `GIVEN the API is throttling WHEN loading THEN returns an error result with the cause`() = runTest {
        // Given
        prepareScenario(failure = RemoteException.RateLimited(cause = null))

        // When
        val result = pager.refresh()

        // Then
        assertThat(result).isInstanceOf(LoadResult.Error::class.java)
        assertThat((result as LoadResult.Error).throwable).isInstanceOf(RemoteException.RateLimited::class.java)
    }

    private fun prepareScenario(
        catalog: List<Song> = emptyList(),
        failure: RemoteException? = null,
    ) {
        remoteDataSource = FakeRemoteDataSource(catalog = catalog, failure = failure)
        songLocalDataSource = FakeSongLocalDataSource()
        pagingSource = SearchSongsPagingSource(
            remoteDataSource = remoteDataSource,
            songLocalDataSource = songLocalDataSource,
            term = "daft punk",
        )
        pager = TestPager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                initialLoadSize = PAGE_SIZE,
                enablePlaceholders = false,
            ),
            pagingSource = pagingSource,
        )
    }

    private fun songs(count: Int): List<Song> = (1..count).map { index -> song(id = index.toLong()) }
}

private class FakeRemoteDataSource(
    private val catalog: List<Song>,
    private val failure: RemoteException?,
) : ITunesRemoteDataSource {
    val requestedLimits = mutableListOf<Int>()

    override suspend fun searchSongs(
        term: String,
        limit: Int,
    ): List<Song> {
        requestedLimits += limit
        failure?.let { throw it }
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
