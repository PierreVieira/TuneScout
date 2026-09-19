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
