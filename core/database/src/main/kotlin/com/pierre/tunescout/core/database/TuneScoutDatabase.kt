package com.pierre.tunescout.core.database

import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.pierre.tunescout.core.database.dao.AlbumDao
import com.pierre.tunescout.core.database.dao.RecentlyPlayedDao
import com.pierre.tunescout.core.database.dao.SongDao
import com.pierre.tunescout.core.database.entity.AlbumEntity
import com.pierre.tunescout.core.database.entity.RecentlyPlayedEntity
import com.pierre.tunescout.core.database.entity.SongEntity

@Database(
    entities = [SongEntity::class, AlbumEntity::class, RecentlyPlayedEntity::class],
    version = 1,
    exportSchema = false,
)
internal abstract class TuneScoutDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao

    abstract fun albumDao(): AlbumDao

    abstract fun recentlyPlayedDao(): RecentlyPlayedDao
}
