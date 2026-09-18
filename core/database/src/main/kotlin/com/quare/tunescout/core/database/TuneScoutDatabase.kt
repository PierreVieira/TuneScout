package com.quare.tunescout.core.database

import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.quare.tunescout.core.database.dao.AlbumDao
import com.quare.tunescout.core.database.dao.RecentlyPlayedDao
import com.quare.tunescout.core.database.dao.SongDao
import com.quare.tunescout.core.database.entity.AlbumEntity
import com.quare.tunescout.core.database.entity.RecentlyPlayedEntity
import com.quare.tunescout.core.database.entity.SongEntity

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
