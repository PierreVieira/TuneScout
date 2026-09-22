package com.pierre.tunescout.core.database

import androidx.room3.testing.MigrationTestHelper
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.pierre.tunescout.core.database.internal.MIGRATION_1_2
import com.pierre.tunescout.core.database.internal.MIGRATION_2_3
import com.pierre.tunescout.core.database.internal.MIGRATION_3_4
import com.pierre.tunescout.core.database.internal.MIGRATION_4_5
import com.pierre.tunescout.core.database.internal.MIGRATION_5_6
import com.pierre.tunescout.core.database.internal.MIGRATION_6_7
import com.pierre.tunescout.core.database.internal.MIGRATION_7_8
import com.pierre.tunescout.core.database.internal.MIGRATION_8_9
import com.pierre.tunescout.core.database.internal.MIGRATION_9_10
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class TuneScoutDatabaseMigrationTest {
    private lateinit var databaseFile: File
    private lateinit var helper: MigrationTestHelper

    @BeforeEach
    fun setUp() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        databaseFile = File(instrumentation.targetContext.noBackupFilesDir, TEST_DATABASE)
        deleteDatabaseFiles()
        helper = MigrationTestHelper(
            instrumentation = instrumentation,
            file = databaseFile,
            driver = BundledSQLiteDriver(),
            databaseClass = TuneScoutDatabase::class,
        )
    }

    @AfterEach
    fun tearDown() {
        deleteDatabaseFiles()
    }

    @Test
    fun givenAVersionOneDatabaseTheMigrationKeepsTheHistoryAndAddsTheQueueTables() = runTest {
        // Given
        helper.createDatabase(version = 1).use { connection ->
            connection.execSQL(
                "INSERT INTO songs VALUES (1, 'Get Lucky', 'Daft Punk', 10, " +
                    "'Random Access Memories', 'https://art/100x100bb.jpg', 'https://preview.m4a', 29000, 8)",
            )
            connection.execSQL("INSERT INTO recently_played VALUES (1, 1700000000000)")
        }

        // When
        val migrated = helper.runMigrationsAndValidate(version = 2, migrations = listOf(MIGRATION_1_2))

        // Then
        migrated.use { connection ->
            assertThat(connection.selectCount("SELECT COUNT(*) FROM songs")).isEqualTo(1)
            assertThat(connection.selectCount("SELECT COUNT(*) FROM recently_played")).isEqualTo(1)
            assertThat(connection.selectCount("SELECT COUNT(*) FROM playback_queue")).isEqualTo(0)
            assertThat(connection.selectCount("SELECT COUNT(*) FROM playback_session")).isEqualTo(0)
        }
    }

    @Test
    fun givenAVersionOneDatabaseTheQueueTableAcceptsARowAfterTheMigration() = runTest {
        // Given
        helper.createDatabase(version = 1).use { connection ->
            connection.execSQL(
                "INSERT INTO songs VALUES (1, 'Get Lucky', 'Daft Punk', 10, " +
                    "'Random Access Memories', 'https://art/100x100bb.jpg', 'https://preview.m4a', 29000, 8)",
            )
        }

        // When
        val migrated = helper.runMigrationsAndValidate(version = 2, migrations = listOf(MIGRATION_1_2))

        // Then
        migrated.use { connection ->
            connection.execSQL("INSERT INTO playback_queue VALUES ('entry-1', 0, 1, 'Context')")
            connection.execSQL("INSERT INTO playback_session VALUES (0, 'entry-1', 5000, 0, 10, 'Album')")
            assertThat(connection.selectCount("SELECT COUNT(*) FROM playback_queue")).isEqualTo(1)
            assertThat(connection.selectCount("SELECT COUNT(*) FROM playback_session")).isEqualTo(1)
        }
    }

    @Test
    fun givenAVersionTwoDatabaseTheMigrationKeepsTheHistoryAndAddsTheLibraryTables() = runTest {
        // Given
        helper.createDatabase(version = 2).use { connection ->
            connection.execSQL(
                "INSERT INTO songs VALUES (1, 'Get Lucky', 'Daft Punk', 10, " +
                    "'Random Access Memories', 'https://art/100x100bb.jpg', 'https://preview.m4a', 29000, 8)",
            )
            connection.execSQL("INSERT INTO recently_played VALUES (1, 1700000000000)")
        }

        // When
        val migrated = helper.runMigrationsAndValidate(version = 3, migrations = listOf(MIGRATION_2_3))

        // Then
        migrated.use { connection ->
            assertThat(connection.selectCount("SELECT COUNT(*) FROM recently_played")).isEqualTo(1)
            assertThat(connection.selectCount("SELECT COUNT(*) FROM playlists")).isEqualTo(0)
            assertThat(connection.selectCount("SELECT COUNT(*) FROM playlist_songs")).isEqualTo(0)
            assertThat(connection.selectCount("SELECT COUNT(*) FROM favorite_songs")).isEqualTo(0)
            assertThat(connection.selectCount("SELECT COUNT(*) FROM favorite_albums")).isEqualTo(0)
            assertThat(connection.selectCount("SELECT COUNT(*) FROM library_recent_searches")).isEqualTo(0)
        }
    }

    @Test
    fun givenAVersionTwoDatabaseTheLibraryTablesAcceptRowsAfterTheMigration() = runTest {
        // Given
        helper.createDatabase(version = 2).use { connection ->
            connection.execSQL(
                "INSERT INTO songs VALUES (1, 'Get Lucky', 'Daft Punk', 10, " +
                    "'Random Access Memories', 'https://art/100x100bb.jpg', 'https://preview.m4a', 29000, 8)",
            )
            connection.execSQL(
                "INSERT INTO albums VALUES (10, 'Random Access Memories', 'Daft Punk', " +
                    "'https://art/100x100bb.jpg', 1700000000000)",
            )
        }

        // When
        val migrated = helper.runMigrationsAndValidate(version = 3, migrations = listOf(MIGRATION_2_3))

        // Then
        migrated.use { connection ->
            connection.execSQL("INSERT INTO playlists VALUES (1, 'Road trip', 1700000000000)")
            connection.execSQL("INSERT INTO playlist_songs VALUES (1, 1, 0)")
            connection.execSQL("INSERT INTO favorite_songs VALUES (1, 1700000000000)")
            connection.execSQL("INSERT INTO favorite_albums VALUES (10, 1700000000000)")
            connection.execSQL("INSERT INTO library_recent_searches VALUES ('playlist:1', 1700000000000)")
            assertThat(connection.selectCount("SELECT COUNT(*) FROM playlist_songs")).isEqualTo(1)
            assertThat(connection.selectCount("SELECT COUNT(*) FROM favorite_songs")).isEqualTo(1)
            assertThat(connection.selectCount("SELECT COUNT(*) FROM favorite_albums")).isEqualTo(1)
            assertThat(connection.selectCount("SELECT COUNT(*) FROM library_recent_searches")).isEqualTo(1)
        }
    }

    @Test
    fun givenAVersionThreeDatabaseTheMigrationKeepsTheSessionAndMarksItAsNotEnded() = runTest {
        // Given
        helper.createDatabase(version = 3).use { connection ->
            connection.execSQL("INSERT INTO playback_session VALUES (0, 'entry-1', 5000, 0, 10, 'Album')")
        }

        // When
        val migrated = helper.runMigrationsAndValidate(version = 4, migrations = listOf(MIGRATION_3_4))

        // Then
        migrated.use { connection ->
            assertThat(connection.selectCount("SELECT COUNT(*) FROM playback_session")).isEqualTo(1)
            assertThat(connection.selectCount("SELECT hasEnded FROM playback_session")).isEqualTo(0)
        }
    }

    @Test
    fun givenAVersionFourDatabaseTheMigrationKeepsTheSongsAndTreatsThemAsTheOldestCached() = runTest {
        // Given
        helper.createDatabase(version = 4).use { connection ->
            connection.execSQL(
                "INSERT INTO songs VALUES (1, 'Get Lucky', 'Daft Punk', 10, " +
                    "'Random Access Memories', 'https://art/100x100bb.jpg', 'https://preview.m4a', 29000, 8)",
            )
        }

        // When
        val migrated = helper.runMigrationsAndValidate(version = 5, migrations = listOf(MIGRATION_4_5))

        // Then
        migrated.use { connection ->
            assertThat(connection.selectCount("SELECT COUNT(*) FROM songs")).isEqualTo(1)
            assertThat(connection.selectCount("SELECT cachedAt FROM songs WHERE id = 1")).isEqualTo(0)
        }
    }

    @Test
    fun givenAVersionFiveSessionThatRepeatedItsSongTheMigrationKeepsItRepeatingAndNotShuffled() = runTest {
        // Given
        helper.createDatabase(version = 5).use { connection ->
            connection.execSQL("INSERT INTO playback_session VALUES (0, 'entry-1', 5000, 1, 10, 'Album', 1)")
        }

        // When
        val migrated = helper.runMigrationsAndValidate(version = 6, migrations = listOf(MIGRATION_5_6))

        // Then
        migrated.use { connection ->
            assertThat(connection.selectText("SELECT repeatMode FROM playback_session")).isEqualTo("One")
            assertThat(connection.selectCount("SELECT isShuffleEnabled FROM playback_session")).isEqualTo(0)
            assertThat(connection.selectCount("SELECT positionMillis FROM playback_session")).isEqualTo(5000)
            assertThat(connection.selectCount("SELECT hasEnded FROM playback_session")).isEqualTo(1)
            assertThat(connection.selectText("SELECT contextAlbumTitle FROM playback_session")).isEqualTo("Album")
        }
    }

    @Test
    fun givenAVersionFiveSessionThatDidNotRepeatTheMigrationTurnsRepeatOffAndKeepsTheQueue() = runTest {
        // Given
        helper.createDatabase(version = 5).use { connection ->
            connection.execSQL(
                "INSERT INTO songs VALUES (1, 'Get Lucky', 'Daft Punk', 10, " +
                    "'Random Access Memories', 'https://art/100x100bb.jpg', 'https://preview.m4a', 29000, 8, 0)",
            )
            connection.execSQL("INSERT INTO playback_queue VALUES ('entry-1', 0, 1, 'Context')")
            connection.execSQL("INSERT INTO playback_session VALUES (0, 'entry-1', 5000, 0, NULL, NULL, 0)")
        }

        // When
        val migrated = helper.runMigrationsAndValidate(version = 6, migrations = listOf(MIGRATION_5_6))

        // Then
        migrated.use { connection ->
            assertThat(connection.selectText("SELECT repeatMode FROM playback_session")).isEqualTo("Off")
            assertThat(connection.selectCount("SELECT COUNT(*) FROM playback_queue")).isEqualTo(1)
            assertThat(
                connection.selectCount("SELECT COUNT(*) FROM playback_queue WHERE unshuffledPosition IS NULL"),
            ).isEqualTo(1)
        }
    }

    @Test
    fun givenAVersionSixSessionOnAnAlbumTheMigrationKeepsTheAlbumAsItsContext() = runTest {
        // Given
        helper.createDatabase(version = 6).use { connection ->
            connection.execSQL(
                "INSERT INTO playback_session VALUES (0, 'entry-1', 5000, 'All', 1, 10, 'Discovery', 1)",
            )
        }

        // When
        val migrated = helper.runMigrationsAndValidate(version = 7, migrations = listOf(MIGRATION_6_7))

        // Then
        migrated.use { connection ->
            assertThat(connection.selectText("SELECT contextType FROM playback_session")).isEqualTo("Album")
            assertThat(connection.selectCount("SELECT contextId FROM playback_session")).isEqualTo(10)
            assertThat(connection.selectText("SELECT contextTitle FROM playback_session")).isEqualTo("Discovery")
            assertThat(connection.selectText("SELECT repeatMode FROM playback_session")).isEqualTo("All")
            assertThat(connection.selectCount("SELECT isShuffleEnabled FROM playback_session")).isEqualTo(1)
            assertThat(connection.selectCount("SELECT positionMillis FROM playback_session")).isEqualTo(5000)
            assertThat(connection.selectCount("SELECT hasEnded FROM playback_session")).isEqualTo(1)
        }
    }

    @Test
    fun givenAVersionSixSessionWithoutAnAlbumTheMigrationMakesItASingleSongAndKeepsTheQueue() = runTest {
        // Given
        helper.createDatabase(version = 6).use { connection ->
            connection.execSQL(
                "INSERT INTO songs VALUES (1, 'Get Lucky', 'Daft Punk', 10, " +
                    "'Random Access Memories', 'https://art/100x100bb.jpg', 'https://preview.m4a', 29000, 8, 0)",
            )
            connection.execSQL("INSERT INTO playback_queue VALUES ('entry-1', 0, 1, 'Context', NULL)")
            connection.execSQL("INSERT INTO playback_session VALUES (0, 'entry-1', 5000, 'Off', 0, NULL, NULL, 0)")
        }

        // When
        val migrated = helper.runMigrationsAndValidate(version = 7, migrations = listOf(MIGRATION_6_7))

        // Then
        migrated.use { connection ->
            assertThat(connection.selectText("SELECT contextType FROM playback_session")).isEqualTo("SingleSong")
            assertThat(
                connection.selectCount("SELECT COUNT(*) FROM playback_session WHERE contextId IS NULL"),
            ).isEqualTo(1)
            assertThat(connection.selectCount("SELECT COUNT(*) FROM playback_queue")).isEqualTo(1)
        }
    }

    @Test
    fun givenAVersionSixDatabaseTheSessionAcceptsAPlaylistContextAfterTheMigration() = runTest {
        // Given
        helper.createDatabase(version = 6).close()

        // When
        val migrated = helper.runMigrationsAndValidate(version = 7, migrations = listOf(MIGRATION_6_7))

        // Then
        migrated.use { connection ->
            connection.execSQL(
                "INSERT INTO playback_session VALUES (0, 'entry-1', 0, 'Off', 0, 'Playlist', 3, 'Road trip', 0)",
            )
            assertThat(connection.selectText("SELECT contextType FROM playback_session")).isEqualTo("Playlist")
            assertThat(connection.selectText("SELECT contextTitle FROM playback_session")).isEqualTo("Road trip")
        }
    }

    @Test
    fun givenAVersionSevenDatabaseTheMigrationKeepsTheSongsAndAcceptsAnAlbumTrackOrder() = runTest {
        // Given
        helper.createDatabase(version = 7).use { connection ->
            connection.execSQL(
                "INSERT INTO songs VALUES (1, 'Get Lucky', 'Daft Punk', 10, " +
                    "'Random Access Memories', 'https://art/100x100bb.jpg', 'https://preview.m4a', 29000, 8, 0)",
            )
        }

        // When
        val migrated = helper.runMigrationsAndValidate(version = 8, migrations = listOf(MIGRATION_7_8))

        // Then
        migrated.use { connection ->
            connection.execSQL("INSERT INTO album_track_order VALUES (10, 1, 0)")
            assertThat(connection.selectCount("SELECT COUNT(*) FROM songs")).isEqualTo(1)
            assertThat(connection.selectCount("SELECT COUNT(*) FROM album_track_order")).isEqualTo(1)
        }
    }

    @Test
    fun givenAVersionEightDatabaseTheMigrationKeepsTheSongsAndAcceptsDownloadRequests() = runTest {
        // Given
        helper.createDatabase(version = 8).use { connection ->
            connection.execSQL(
                "INSERT INTO songs VALUES (1, 'Get Lucky', 'Daft Punk', 10, " +
                    "'Random Access Memories', 'https://art/100x100bb.jpg', 'https://preview.m4a', 29000, 8, 0)",
            )
        }

        // When
        val migrated = helper.runMigrationsAndValidate(version = 9, migrations = listOf(MIGRATION_8_9))

        // Then
        migrated.use { connection ->
            connection.execSQL("INSERT INTO downloaded_songs VALUES (1, 0)")
            connection.execSQL("INSERT INTO downloaded_collections VALUES ('Album', 10, 0)")
            assertThat(connection.selectCount("SELECT COUNT(*) FROM songs")).isEqualTo(1)
            assertThat(connection.selectCount("SELECT COUNT(*) FROM downloaded_songs")).isEqualTo(1)
            assertThat(connection.selectCount("SELECT COUNT(*) FROM downloaded_collections")).isEqualTo(1)
        }
    }

    @Test
    fun givenAVersionNineDatabaseTheMigrationKeepsTheSongsAndAcceptsDownloadExclusions() = runTest {
        // Given
        helper.createDatabase(version = 9).use { connection ->
            connection.execSQL(
                "INSERT INTO songs VALUES (1, 'Get Lucky', 'Daft Punk', 10, " +
                    "'Random Access Memories', 'https://art/100x100bb.jpg', 'https://preview.m4a', 29000, 8, 0)",
            )
            connection.execSQL("INSERT INTO downloaded_songs VALUES (1, 0)")
        }

        // When
        val migrated = helper.runMigrationsAndValidate(version = 10, migrations = listOf(MIGRATION_9_10))

        // Then
        migrated.use { connection ->
            connection.execSQL("INSERT INTO downloaded_song_exclusions VALUES (1, 0)")
            assertThat(connection.selectCount("SELECT COUNT(*) FROM songs")).isEqualTo(1)
            assertThat(connection.selectCount("SELECT COUNT(*) FROM downloaded_songs")).isEqualTo(1)
            assertThat(connection.selectCount("SELECT COUNT(*) FROM downloaded_song_exclusions")).isEqualTo(1)
        }
    }

    private fun deleteDatabaseFiles() {
        listOf("", "-wal", "-shm").forEach { suffix -> File("${databaseFile.path}$suffix").delete() }
    }

    private companion object {
        const val TEST_DATABASE = "tunescout-migration-test.db"
    }
}

private fun SQLiteConnection.selectCount(sql: String): Long = prepare(sql).use { statement ->
    statement.step()
    statement.getLong(0)
}

private fun SQLiteConnection.selectText(sql: String): String = prepare(sql).use { statement ->
    statement.step()
    statement.getText(0)
}
