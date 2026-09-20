package com.pierre.tunescout.core.database.internal

import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

internal val MIGRATION_1_2 = object : Migration(1, 2) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS `playback_queue` (`entryId` TEXT NOT NULL, " +
                "`position` INTEGER NOT NULL, `songId` INTEGER NOT NULL, `source` TEXT NOT NULL, " +
                "PRIMARY KEY(`entryId`), FOREIGN KEY(`songId`) REFERENCES `songs`(`id`) " +
                "ON UPDATE NO ACTION ON DELETE CASCADE )",
        )
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_playback_queue_songId` ON `playback_queue` (`songId`)")
        connection.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_playback_queue_position` ON `playback_queue` (`position`)",
        )
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS `playback_session` (`id` INTEGER NOT NULL, `currentEntryId` TEXT, " +
                "`positionMillis` INTEGER NOT NULL, `isRepeatEnabled` INTEGER NOT NULL, " +
                "`contextAlbumId` INTEGER, `contextAlbumTitle` TEXT, PRIMARY KEY(`id`))",
        )
    }
}

internal val MIGRATION_2_3 = object : Migration(2, 3) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS `playlists` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)",
        )
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS `playlist_songs` (`playlistId` INTEGER NOT NULL, " +
                "`songId` INTEGER NOT NULL, `position` INTEGER NOT NULL, " +
                "PRIMARY KEY(`playlistId`, `songId`), FOREIGN KEY(`playlistId`) REFERENCES `playlists`(`id`) " +
                "ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`songId`) REFERENCES `songs`(`id`) " +
                "ON UPDATE NO ACTION ON DELETE CASCADE )",
        )
        connection.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_playlist_songs_songId` ON `playlist_songs` (`songId`)",
        )
        connection.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_playlist_songs_position` ON `playlist_songs` (`position`)",
        )
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS `favorite_songs` (`songId` INTEGER NOT NULL, " +
                "`favoritedAt` INTEGER NOT NULL, PRIMARY KEY(`songId`), " +
                "FOREIGN KEY(`songId`) REFERENCES `songs`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
        )
        connection.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_favorite_songs_favoritedAt` ON `favorite_songs` (`favoritedAt`)",
        )
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS `favorite_albums` (`albumId` INTEGER NOT NULL, " +
                "`favoritedAt` INTEGER NOT NULL, PRIMARY KEY(`albumId`), " +
                "FOREIGN KEY(`albumId`) REFERENCES `albums`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
        )
        connection.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_favorite_albums_favoritedAt` ON `favorite_albums` (`favoritedAt`)",
        )
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS `library_recent_searches` (`itemId` TEXT NOT NULL, " +
                "`searchedAt` INTEGER NOT NULL, PRIMARY KEY(`itemId`))",
        )
        connection.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_library_recent_searches_searchedAt` " +
                "ON `library_recent_searches` (`searchedAt`)",
        )
    }
}

internal val MIGRATION_3_4 = object : Migration(3, 4) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE `playback_session` ADD COLUMN `hasEnded` INTEGER NOT NULL DEFAULT 0")
    }
}
