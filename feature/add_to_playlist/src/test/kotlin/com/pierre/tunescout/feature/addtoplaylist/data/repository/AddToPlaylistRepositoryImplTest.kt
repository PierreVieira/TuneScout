package com.pierre.tunescout.feature.addtoplaylist.data.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.database.PlaylistLocalDataSource
import com.pierre.tunescout.core.database.SongLocalDataSource
import com.pierre.tunescout.core.model.Playlist
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.testing.fixture.playlist
import com.pierre.tunescout.core.testing.fixture.song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class AddToPlaylistRepositoryImplTest {
    private lateinit var repository: AddToPlaylistRepositoryImpl
    private lateinit var playlistLocalDataSource: FakePlaylistLocalDataSource

    @Test
    fun `GIVEN playlists in the library WHEN observing them THEN the sheet gets them in order`() = runTest {
        // Given
        val playlists = listOf(playlist(id = 1), playlist(id = 2, name = "Focus"))
        prepareScenario(playlists = playlists)

        // When
        val observed = repository.observePlaylists()

        // Then
        observed.test {
            assertThat(awaitItem()).containsExactlyElementsIn(playlists).inOrder()
            awaitComplete()
        }
    }

    @Test
    fun `GIVEN a stored song WHEN observing it THEN it comes back`() = runTest {
        // Given
        prepareScenario(song = song(id = 3, title = "Get Lucky"))

        // When
        val observed = repository.observeSong(songId = 3)

        // Then
        observed.test {
            assertThat(awaitItem()?.title).isEqualTo("Get Lucky")
            awaitComplete()
        }
    }

    @Test
    fun `WHEN adding a song to a playlist THEN the playlist takes it`() = runTest {
        // Given
        prepareScenario()

        // When
        repository.addSong(playlistId = 7, song = song(id = 1))

        // Then
        assertThat(playlistLocalDataSource.addedSongs).containsExactly(7L to 1L)
    }

    @Test
    fun `WHEN creating a playlist THEN its new id comes back`() = runTest {
        // Given
        prepareScenario(createdPlaylistId = 42)

        // When
        val playlistId = repository.createPlaylist(name = "Road trip")

        // Then
        assertThat(playlistId).isEqualTo(42)
        assertThat(playlistLocalDataSource.createdNames).containsExactly("Road trip")
    }

    private fun prepareScenario(
        playlists: List<Playlist> = emptyList(),
        song: Song? = null,
        createdPlaylistId: Long = 1,
    ) {
        playlistLocalDataSource = FakePlaylistLocalDataSource(
            playlists = playlists,
            createdPlaylistId = createdPlaylistId,
        )
        repository = AddToPlaylistRepositoryImpl(
            playlistLocalDataSource = playlistLocalDataSource,
            songLocalDataSource = FakeSongLocalDataSource(song = song),
        )
    }
}

private class FakePlaylistLocalDataSource(
    private val playlists: List<Playlist>,
    private val createdPlaylistId: Long,
) : PlaylistLocalDataSource {
    val createdNames = mutableListOf<String>()
    val addedSongs = mutableListOf<Pair<Long, Long>>()

    override fun observeAll(): Flow<List<Playlist>> = flowOf(playlists)

    override fun observe(playlistId: Long): Flow<Playlist?> = error("unused")

    override fun observeSongs(playlistId: Long): Flow<List<Song>> = error("unused")

    override fun observeContains(
        playlistId: Long,
        songId: Long,
    ): Flow<Boolean> = error("unused")

    override suspend fun create(name: String): Long {
        createdNames += name
        return createdPlaylistId
    }

    override suspend fun rename(
        playlistId: Long,
        name: String,
    ) {
        error("unused")
    }

    override suspend fun delete(playlistId: Long) {
        error("unused")
    }

    override suspend fun addSong(
        playlistId: Long,
        song: Song,
    ) {
        addedSongs += playlistId to song.id
    }

    override suspend fun removeSong(
        playlistId: Long,
        songId: Long,
    ) {
        error("unused")
    }
}

private class FakeSongLocalDataSource(
    private val song: Song?,
) : SongLocalDataSource {
    override suspend fun save(songs: List<Song>) {
        error("unused")
    }

    override fun observe(songId: Long): Flow<Song?> = flowOf(song)

    override suspend fun find(songId: Long): Song? = error("unused")

    override suspend fun findByTerm(
        term: String,
        limit: Int,
    ): List<Song> = error("unused")
}
