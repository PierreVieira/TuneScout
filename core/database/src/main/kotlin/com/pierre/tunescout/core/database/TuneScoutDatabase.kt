package com.pierre.tunescout.core.database

import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.pierre.tunescout.core.database.dao.AlbumDao
import com.pierre.tunescout.core.database.dao.PlaybackSessionDao
import com.pierre.tunescout.core.database.dao.RecentlyPlayedDao
import com.pierre.tunescout.core.database.dao.SongDao
import com.pierre.tunescout.core.database.entity.AlbumEntity
import com.pierre.tunescout.core.database.entity.PlaybackQueueEntity
import com.pierre.tunescout.core.database.entity.PlaybackSessionEntity
import com.pierre.tunescout.core.database.entity.RecentlyPlayedEntity
import com.pierre.tunescout.core.database.entity.SongEntity

@Database(
    entities = [
        SongEntity::class,
        AlbumEntity::class,
        RecentlyPlayedEntity::class,
        PlaybackQueueEntity::class,
        PlaybackSessionEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
internal abstract class TuneScoutDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao

    abstract fun albumDao(): AlbumDao

    abstract fun recentlyPlayedDao(): RecentlyPlayedDao

    abstract fun playbackSessionDao(): PlaybackSessionDao
}
