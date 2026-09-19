package com.pierre.tunescout.core.database.internal

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.database.dao.PlaylistDao
import com.pierre.tunescout.core.database.dao.SongDao
import com.pierre.tunescout.core.database.entity.PlaylistEntity
import com.pierre.tunescout.core.database.entity.PlaylistSongEntity
import com.pierre.tunescout.core.database.entity.SongEntity
import com.pierre.tunescout.core.database.relation.PlaylistRow
import com.pierre.tunescout.core.testing.fixture.song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class RoomPlaylistLocalDataSourceTest {
    private var now = 0L
    private lateinit var localDataSource: RoomPlaylistLocalDataSource

    @Test
    fun `GIVEN a created playlist WHEN observing all THEN it comes back empty under its name`() = runTest {
        // Given
        prepareScenario()

        // When
        localDataSource.create(name = "Road trip")

        // Then
        localDataSource.observeAll().test {
            val playlists = awaitItem()
            assertThat(playlists.map { playlist -> playlist.name }).containsExactly("Road trip")
            assertThat(playlists.single().songCount).isEqualTo(0)
        }
    }

    @Test
    fun `GIVEN playlists created one after the other WHEN observing all THEN the newest comes first`() = runTest {
        // Given
        prepareScenario()

        // When
        localDataSource.create(name = "Road trip")
        localDataSource.create(name = "Focus")

        // Then
        localDataSource.observeAll().test {
            assertThat(awaitItem().map { playlist -> playlist.name })
                .containsExactly("Focus", "Road trip")
                .inOrder()
        }
    }

    @Test
    fun `GIVEN a playlist WHEN adding songs THEN they are counted and their artworks kept in order`() = runTest {
        // Given
        prepareScenario()
        val playlistId = localDataSource.create(name = "Road trip")

        // When
        localDataSource.addSong(playlistId = playlistId, song = song(id = 1))
        localDataSource.addSong(playlistId = playlistId, song = song(id = 2))

        // Then
        localDataSource.observe(playlistId).test {
            val playlist = awaitItem()
            assertThat(playlist?.songCount).isEqualTo(2)
            assertThat(playlist?.artworks)
                .containsExactly(song(id = 1).artwork, song(id = 2).artwork)
                .inOrder()
        }
    }

    @Test
    fun `GIVEN a song already in the playlist WHEN adding it again THEN it keeps its place`() = runTest {
        // Given
        prepareScenario()
        val playlistId = localDataSource.create(name = "Road trip")
        localDataSource.addSong(playlistId = playlistId, song = song(id = 1))
        localDataSource.addSong(playlistId = playlistId, song = song(id = 2))

        // When
        localDataSource.addSong(playlistId = playlistId, song = song(id = 1))

        // Then
        localDataSource.observeSongs(playlistId).test {
            assertThat(awaitItem().map { song -> song.id }).containsExactly(1L, 2L).inOrder()
        }
    }

    @Test
    fun `GIVEN a playlist with songs WHEN removing one THEN the others keep their order`() = runTest {
        // Given
        prepareScenario()
        val playlistId = localDataSource.create(name = "Road trip")
        localDataSource.addSong(playlistId = playlistId, song = song(id = 1))
        localDataSource.addSong(playlistId = playlistId, song = song(id = 2))
        localDataSource.addSong(playlistId = playlistId, song = song(id = 3))

        // When
        localDataSource.removeSong(playlistId = playlistId, songId = 2)

        // Then
        localDataSource.observeSongs(playlistId).test {
            assertThat(awaitItem().map { song -> song.id }).containsExactly(1L, 3L).inOrder()
        }
    }

    @Test
    fun `GIVEN a song in a playlist WHEN asking whether it is there THEN only that song is`() = runTest {
        // Given
        prepareScenario()
        val playlistId = localDataSource.create(name = "Road trip")

        // When
        localDataSource.addSong(playlistId = playlistId, song = song(id = 1))

        // Then
        assertThat(localDataSource.observeContains(playlistId = playlistId, songId = 1).first()).isTrue()
        assertThat(localDataSource.observeContains(playlistId = playlistId, songId = 2).first()).isFalse()
    }

    @Test
    fun `GIVEN a playlist WHEN renaming it THEN the observed name follows`() = runTest {
        // Given
        prepareScenario()
        val playlistId = localDataSource.create(name = "Road trip")

        // When
        localDataSource.rename(playlistId = playlistId, name = "Long drive")

        // Then
        localDataSource.observe(playlistId).test {
            assertThat(awaitItem()?.name).isEqualTo("Long drive")
        }
    }

    @Test
    fun `GIVEN a playlist WHEN deleting it THEN it is gone from the library`() = runTest {
        // Given
        prepareScenario()
        val playlistId = localDataSource.create(name = "Road trip")

        // When
        localDataSource.delete(playlistId)

        // Then
        localDataSource.observe(playlistId).test {
            assertThat(awaitItem()).isNull()
        }
    }

    private fun prepareScenario() {
        val songDao = FakePlaylistSongDao()
        localDataSource = RoomPlaylistLocalDataSource(
            playlistDao = FakePlaylistDao(songs = songDao.songs),
            songDao = songDao,
            timestampProvider = { ++now },
        )
    }
}

private class FakePlaylistSongDao : SongDao {
    val songs = mutableMapOf<Long, SongEntity>()

    override suspend fun upsertAll(songs: List<SongEntity>) {
        songs.forEach { song -> this.songs[song.id] = song }
    }

    override fun observeById(songId: Long): Flow<SongEntity?> = error("unused")

    override suspend fun getById(songId: Long): SongEntity? = error("unused")
}

/**
 * Implements only the members the DAO declares, so `appendSong` — the one with a body of its own,
 * and the guard against adding the same song twice — runs for real.
 */
private class FakePlaylistDao(
    private val songs: Map<Long, SongEntity>,
) : PlaylistDao {
    private val playlists = MutableStateFlow(emptyList<PlaylistEntity>())
    private val entries = MutableStateFlow(emptyList<PlaylistSongEntity>())
    private var lastId = 0L

    override fun observeAllRows(): Flow<List<PlaylistRow>> = combine(playlists, entries) { playlists, entries ->
        playlists
            .sortedByDescending { playlist -> playlist.createdAt }
            .flatMap { playlist -> playlist.toRows(entries) }
    }

    override fun observeRows(playlistId: Long): Flow<List<PlaylistRow>> = combine(playlists, entries) { all, entries ->
        all.filter { playlist -> playlist.id == playlistId }.flatMap { playlist -> playlist.toRows(entries) }
    }

    override fun observeSongs(playlistId: Long): Flow<List<SongEntity>> = entries.map { current ->
        current.of(playlistId).mapNotNull { entry -> songs[entry.songId] }
    }

    override fun observeContains(
        playlistId: Long,
        songId: Long,
    ): Flow<Boolean> = entries.map { current -> current.hasSong(playlistId = playlistId, songId = songId) }

    override suspend fun hasSong(
        playlistId: Long,
        songId: Long,
    ): Boolean = entries.value.hasSong(playlistId = playlistId, songId = songId)

    override suspend fun insert(playlist: PlaylistEntity): Long {
        val id = ++lastId
        playlists.value += playlist.copy(id = id)
        return id
    }

    override suspend fun updateName(
        playlistId: Long,
        name: String,
    ) {
        playlists.value = playlists.value.map { playlist ->
            if (playlist.id == playlistId) playlist.copy(name = name) else playlist
        }
    }

    override suspend fun deleteById(playlistId: Long) {
        playlists.value = playlists.value.filterNot { playlist -> playlist.id == playlistId }
        entries.value = entries.value.filterNot { entry -> entry.playlistId == playlistId }
    }

    override suspend fun getNextPosition(playlistId: Long): Int = entries.value
        .of(playlistId)
        .maxOfOrNull { entry -> entry.position }
        ?.plus(1) ?: 0

    override suspend fun upsertSong(entry: PlaylistSongEntity) {
        entries.value = entries.value.filterNot { current ->
            current.isFor(playlistId = entry.playlistId, songId = entry.songId)
        } + entry
    }

    override suspend fun deleteSong(
        playlistId: Long,
        songId: Long,
    ) {
        entries.value = entries.value.filterNot { entry -> entry.isFor(playlistId = playlistId, songId = songId) }
    }

    private fun PlaylistEntity.toRows(entries: List<PlaylistSongEntity>): List<PlaylistRow> {
        val mine = entries.of(id)
        return when {
            mine.isEmpty() -> listOf(PlaylistRow(playlistId = id, name = name, artworkUrl = null))

            else -> mine.map { entry ->
                PlaylistRow(playlistId = id, name = name, artworkUrl = songs[entry.songId]?.artworkUrl)
            }
        }
    }
}

private fun List<PlaylistSongEntity>.of(playlistId: Long): List<PlaylistSongEntity> =
    filter { entry -> entry.playlistId == playlistId }.sortedBy { entry -> entry.position }

private fun List<PlaylistSongEntity>.hasSong(
    playlistId: Long,
    songId: Long,
): Boolean = any { entry -> entry.isFor(playlistId = playlistId, songId = songId) }

private fun PlaylistSongEntity.isFor(
    playlistId: Long,
    songId: Long,
): Boolean = this.playlistId == playlistId && this.songId == songId
