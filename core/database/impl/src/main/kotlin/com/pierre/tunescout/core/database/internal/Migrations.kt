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

internal val MIGRATION_4_5 = object : Migration(4, 5) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE `songs` ADD COLUMN `cachedAt` INTEGER NOT NULL DEFAULT 0")
    }
}

/**
 * Repeat grows a third mode, so the flag becomes the mode's name — a session that repeated its song
 * keeps doing so — and shuffle is saved beside it, with the order it would put back. SQLite cannot
 * drop a column on every version the app runs on, so the session table is rebuilt instead.
 */
internal val MIGRATION_5_6 = object : Migration(5, 6) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE `playback_queue` ADD COLUMN `unshuffledPosition` INTEGER")
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS `playback_session_new` (`id` INTEGER NOT NULL, `currentEntryId` TEXT, " +
                "`positionMillis` INTEGER NOT NULL, `repeatMode` TEXT NOT NULL, " +
                "`isShuffleEnabled` INTEGER NOT NULL, `contextAlbumId` INTEGER, `contextAlbumTitle` TEXT, " +
                "`hasEnded` INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(`id`))",
        )
        connection.execSQL(
            "INSERT INTO `playback_session_new` (`id`, `currentEntryId`, `positionMillis`, `repeatMode`, " +
                "`isShuffleEnabled`, `contextAlbumId`, `contextAlbumTitle`, `hasEnded`) " +
                "SELECT `id`, `currentEntryId`, `positionMillis`, " +
                "CASE WHEN `isRepeatEnabled` THEN 'One' ELSE 'Off' END, 0, " +
                "`contextAlbumId`, `contextAlbumTitle`, `hasEnded` FROM `playback_session`",
        )
        connection.execSQL("DROP TABLE `playback_session`")
        connection.execSQL("ALTER TABLE `playback_session_new` RENAME TO `playback_session`")
    }
}

/** Reordering an album's tracks keeps the order apart from them, so a refresh cannot undo it. */
internal val MIGRATION_6_7 = object : Migration(6, 7) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS `album_track_order` (`albumId` INTEGER NOT NULL, " +
                "`songId` INTEGER NOT NULL, `position` INTEGER NOT NULL, PRIMARY KEY(`albumId`, `songId`), " +
                "FOREIGN KEY(`songId`) REFERENCES `songs`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
        )
        connection.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_album_track_order_songId` ON `album_track_order` (`songId`)",
        )
    }
}
