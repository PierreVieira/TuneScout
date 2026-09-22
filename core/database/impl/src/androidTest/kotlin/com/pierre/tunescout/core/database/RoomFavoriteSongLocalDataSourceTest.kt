package com.pierre.tunescout.core.database

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.database.internal.RoomFavoriteSongLocalDataSource
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.testing.fixture.song
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RoomFavoriteSongLocalDataSourceTest {
    private lateinit var database: TuneScoutDatabase
    private lateinit var dataSource: FavoriteSongLocalDataSource
    private var now: Long = 0

    @BeforeEach
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room
            .inMemoryDatabaseBuilder<TuneScoutDatabase>(context)
            .setDriver(BundledSQLiteDriver())
            .build()
        dataSource = RoomFavoriteSongLocalDataSource(
            favoriteSongDao = database.favoriteSongDao(),
            songDao = database.songDao(),
            timestampProvider = { ++now },
        )
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Test
    fun theMostRecentlyLikedSongComesFirst() = runTest {
        // Given
        listOf(song(id = 1), song(id = 2), song(id = 3)).forEach { liked -> dataSource.add(liked) }

        // When
        val ids = dataSource.observeAll().first().map(Song::id)

        // Then
        assertThat(ids).containsExactly(3L, 2L, 1L).inOrder()
    }

    @Test
    fun likingASongThatWasOnlyASearchResultCachesIt() = runTest {
        // When
        dataSource.add(song(id = 1, title = "Get Lucky"))

        // Then
        assertThat(database.songDao().getById(1)?.title).isEqualTo("Get Lucky")
    }

    @Test
    fun unlikingASongDropsItFromTheListAndKeepsItCached() = runTest {
        // Given
        dataSource.add(song(id = 1))

        // When
        dataSource.remove(songId = 1)

        // Then
        assertThat(dataSource.observeAll().first()).isEmpty()
        assertThat(database.songDao().getById(1)).isNotNull()
    }

    @Test
    fun aSongReportsWhetherItIsLiked() = runTest {
        // Given
        dataSource.add(song(id = 1))

        // When
        val isLiked = dataSource.observeIsFavorite(songId = 1).first()
        val isOtherLiked = dataSource.observeIsFavorite(songId = 2).first()

        // Then
        assertThat(isLiked).isTrue()
        assertThat(isOtherLiked).isFalse()
    }

    @Test
    fun likingTheSameSongTwiceKeepsOneEntry() = runTest {
        // Given
        dataSource.add(song(id = 1))

        // When
        dataSource.add(song(id = 1))

        // Then
        assertThat(dataSource.observeAll().first()).hasSize(1)
    }
}
