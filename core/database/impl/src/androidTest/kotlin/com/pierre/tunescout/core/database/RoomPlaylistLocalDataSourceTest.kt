package com.pierre.tunescout.core.database

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.database.internal.RoomPlaylistLocalDataSource
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.testing.fixture.song
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RoomPlaylistLocalDataSourceTest {
    private lateinit var database: TuneScoutDatabase
    private lateinit var dataSource: PlaylistLocalDataSource
    private var now: Long = 0

    @BeforeEach
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room
            .inMemoryDatabaseBuilder<TuneScoutDatabase>(context)
            .setDriver(BundledSQLiteDriver())
            .build()
        dataSource = RoomPlaylistLocalDataSource(
            playlistDao = database.playlistDao(),
            songDao = database.songDao(),
            timestampProvider = { ++now },
        )
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Test
    fun aNewPlaylistIsListedWithNoSongs() = runTest {
        // Given
        val playlistId = dataSource.create(name = "Road trip")

        // When
        val playlists = dataSource.observeAll().first()

        // Then
        assertThat(playlists).hasSize(1)
        assertThat(playlists.single().id).isEqualTo(playlistId)
        assertThat(playlists.single().songCount).isEqualTo(0)
    }

    @Test
    fun theNewestPlaylistIsListedFirst() = runTest {
        // Given
        dataSource.create(name = "Focus")
        dataSource.create(name = "Road trip")

        // When
        val names = dataSource.observeAll().first().map { playlist -> playlist.name }

        // Then
        assertThat(names).containsExactly("Road trip", "Focus").inOrder()
    }

    @Test
    fun addedSongsKeepTheOrderTheyWereAddedIn() = runTest {
        // Given
        val playlistId = dataSource.create(name = "Road trip")

        // When
        listOf(song(id = 3), song(id = 1), song(id = 2)).forEach { added ->
            dataSource.addSong(playlistId = playlistId, song = added)
        }

        // Then
        assertThat(dataSource.observeSongs(playlistId).first().map(Song::id))
            .containsExactly(3L, 1L, 2L)
            .inOrder()
    }

    @Test
    fun aSongThatWasOnlyASearchResultIsCachedWhenItIsAdded() = runTest {
        // Given
        val playlistId = dataSource.create(name = "Road trip")

        // When
        dataSource.addSong(playlistId = playlistId, song = song(id = 1, title = "Get Lucky"))

        // Then
        assertThat(database.songDao().getById(1)?.title).isEqualTo("Get Lucky")
    }

    @Test
    fun addingTheSameSongTwiceLeavesItInPlaceOnce() = runTest {
        // Given
        val playlistId = dataSource.create(name = "Road trip")
        dataSource.addSong(playlistId = playlistId, song = song(id = 1))
        dataSource.addSong(playlistId = playlistId, song = song(id = 2))

        // When
        dataSource.addSong(playlistId = playlistId, song = song(id = 1))

        // Then
        assertThat(dataSource.observeSongs(playlistId).first().map(Song::id))
            .containsExactly(1L, 2L)
            .inOrder()
    }

    @Test
    fun removingASongLeavesTheRestInOrder() = runTest {
        // Given
        val playlistId = dataSource.create(name = "Road trip")
        listOf(song(id = 1), song(id = 2), song(id = 3)).forEach { added ->
            dataSource.addSong(playlistId = playlistId, song = added)
        }

        // When
        dataSource.removeSong(playlistId = playlistId, songId = 2)

        // Then
        assertThat(dataSource.observeSongs(playlistId).first().map(Song::id))
            .containsExactly(1L, 3L)
            .inOrder()
    }

    @Test
    fun deletingAPlaylistTakesItsSongEntriesWithItAndKeepsTheSongs() = runTest {
        // Given
        val playlistId = dataSource.create(name = "Road trip")
        dataSource.addSong(playlistId = playlistId, song = song(id = 1))

        // When
        dataSource.delete(playlistId)

        // Then
        assertThat(dataSource.observeAll().first()).isEmpty()
        assertThat(dataSource.observeSongs(playlistId).first()).isEmpty()
        assertThat(database.songDao().getById(1)).isNotNull()
    }

    @Test
    fun renamingAPlaylistKeepsItsSongs() = runTest {
        // Given
        val playlistId = dataSource.create(name = "Road trip")
        dataSource.addSong(playlistId = playlistId, song = song(id = 1))

        // When
        dataSource.rename(playlistId = playlistId, name = "Long drive")

        // Then
        assertThat(dataSource.observe(playlistId).first()?.name).isEqualTo("Long drive")
        assertThat(dataSource.observeSongs(playlistId).first()).hasSize(1)
    }

    @Test
    fun aPlaylistReportsWhetherItAlreadyHoldsASong() = runTest {
        // Given
        val playlistId = dataSource.create(name = "Road trip")
        dataSource.addSong(playlistId = playlistId, song = song(id = 1))

        // When
        val holdsAdded = dataSource.observeContains(playlistId = playlistId, songId = 1).first()
        val holdsOther = dataSource.observeContains(playlistId = playlistId, songId = 2).first()

        // Then
        assertThat(holdsAdded).isTrue()
        assertThat(holdsOther).isFalse()
    }
}
