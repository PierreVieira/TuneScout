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
import kotlinx.coroutines.runBlocking
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
    fun givenAVersionOneDatabaseTheMigrationKeepsTheHistoryAndAddsTheQueueTables() = runBlocking {
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
    fun givenAVersionOneDatabaseTheQueueTableAcceptsARowAfterTheMigration() = runBlocking {
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
    fun givenAVersionTwoDatabaseTheMigrationKeepsTheHistoryAndAddsTheLibraryTables() = runBlocking {
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
    fun givenAVersionTwoDatabaseTheLibraryTablesAcceptRowsAfterTheMigration() = runBlocking {
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
    fun givenAVersionThreeDatabaseTheMigrationKeepsTheSessionAndMarksItAsNotEnded() = runBlocking {
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
    fun givenAVersionFourDatabaseTheMigrationKeepsTheSongsAndTreatsThemAsTheOldestCached() = runBlocking {
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
