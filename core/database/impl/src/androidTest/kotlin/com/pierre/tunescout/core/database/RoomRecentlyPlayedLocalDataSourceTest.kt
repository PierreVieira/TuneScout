package com.pierre.tunescout.core.database

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.database.internal.RoomRecentlyPlayedLocalDataSource
import com.pierre.tunescout.core.model.Song
import com.pierre.tunescout.core.testing.fixture.song
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class RoomRecentlyPlayedLocalDataSourceTest {
    private lateinit var database: TuneScoutDatabase
    private lateinit var dataSource: RecentlyPlayedLocalDataSource
    private var now: Long = 0

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Test
    fun removingAnEntryDropsOnlyThatSongFromTheHistory() {
        runBlocking {
            // Given
            prepareScenario(recorded = listOf(song(id = 1), song(id = 2), song(id = 3)))

            // When
            dataSource.remove(songId = 2)

            // Then
            assertThat(observedIds()).containsExactly(3L, 1L).inOrder()
        }
    }

    @Test
    fun removingAnEntryKeepsTheSongItselfCached() {
        runBlocking {
            // Given
            prepareScenario(recorded = listOf(song(id = 1, title = "Get Lucky")))

            // When
            dataSource.remove(songId = 1)

            // Then
            assertThat(observedIds()).isEmpty()
            assertThat(database.songDao().getById(1)?.title).isEqualTo("Get Lucky")
        }
    }

    @Test
    fun removingASongThatWasNeverPlayedLeavesTheHistoryAlone() {
        runBlocking {
            // Given
            prepareScenario(recorded = listOf(song(id = 1)))

            // When
            dataSource.remove(songId = 99)

            // Then
            assertThat(observedIds()).containsExactly(1L)
        }
    }

    @Test
    fun aRemovedSongComesBackWhenItIsPlayedAgain() {
        runBlocking {
            // Given
            prepareScenario(recorded = listOf(song(id = 1), song(id = 2)))
            dataSource.remove(songId = 1)

            // When
            dataSource.record(song(id = 1))

            // Then
            assertThat(observedIds()).containsExactly(1L, 2L).inOrder()
        }
    }

    @Test
    fun aRecordedSongIsReportedAsRecentlyPlayed() {
        runBlocking {
            // Given
            prepareScenario(recorded = listOf(song(id = 1)))

            // When
            val isRecentlyPlayed = dataSource.observeIsRecentlyPlayed(songId = 1).first()

            // Then
            assertThat(isRecentlyPlayed).isTrue()
        }
    }

    @Test
    fun aSongOutsideTheHistoryIsNotReportedAsRecentlyPlayed() {
        runBlocking {
            // Given
            prepareScenario(recorded = listOf(song(id = 1)))

            // When
            val isRecentlyPlayed = dataSource.observeIsRecentlyPlayed(songId = 2).first()

            // Then
            assertThat(isRecentlyPlayed).isFalse()
        }
    }

    @Test
    fun aRemovedSongStopsBeingReportedAsRecentlyPlayed() {
        runBlocking {
            // Given
            prepareScenario(recorded = listOf(song(id = 1)))

            // When
            dataSource.remove(songId = 1)

            // Then
            assertThat(dataSource.observeIsRecentlyPlayed(songId = 1).first()).isFalse()
        }
    }

    private suspend fun observedIds(): List<Long> =
        dataSource.observe(limit = MAX_ENTRIES).first().map { song -> song.id }

    private suspend fun prepareScenario(recorded: List<Song>) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room
            .inMemoryDatabaseBuilder<TuneScoutDatabase>(context)
            .setDriver(BundledSQLiteDriver())
            .build()
        dataSource = RoomRecentlyPlayedLocalDataSource(
            recentlyPlayedDao = database.recentlyPlayedDao(),
            songDao = database.songDao(),
            timestampProvider = { ++now },
            maxEntries = MAX_ENTRIES,
        )
        recorded.forEach { song -> dataSource.record(song) }
    }

    private companion object {
        const val MAX_ENTRIES = 20
    }
}
