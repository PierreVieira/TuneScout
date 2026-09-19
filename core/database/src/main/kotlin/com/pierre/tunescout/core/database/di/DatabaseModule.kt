package com.pierre.tunescout.core.database.di

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.pierre.tunescout.core.database.AlbumLocalDataSource
import com.pierre.tunescout.core.database.PlaybackSessionLocalDataSource
import com.pierre.tunescout.core.database.RecentlyPlayedLocalDataSource
import com.pierre.tunescout.core.database.SongLocalDataSource
import com.pierre.tunescout.core.database.TuneScoutDatabase
import com.pierre.tunescout.core.database.dao.AlbumDao
import com.pierre.tunescout.core.database.dao.PlaybackSessionDao
import com.pierre.tunescout.core.database.dao.RecentlyPlayedDao
import com.pierre.tunescout.core.database.dao.SongDao
import com.pierre.tunescout.core.database.internal.MIGRATION_1_2
import com.pierre.tunescout.core.database.internal.RoomAlbumLocalDataSource
import com.pierre.tunescout.core.database.internal.RoomPlaybackSessionLocalDataSource
import com.pierre.tunescout.core.database.internal.RoomRecentlyPlayedLocalDataSource
import com.pierre.tunescout.core.database.internal.RoomSongLocalDataSource
import com.pierre.tunescout.core.database.internal.TimestampProvider
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

private const val DATABASE_NAME = "tunescout.db"
private const val MAX_RECENTLY_PLAYED = 20

val databaseModule: Module = module {
    single<TuneScoutDatabase> {
        Room
            .databaseBuilder<TuneScoutDatabase>(androidContext(), DATABASE_NAME)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .addMigrations(MIGRATION_1_2)
            .build()
    }
    single<SongDao> { get<TuneScoutDatabase>().songDao() }
    single<AlbumDao> { get<TuneScoutDatabase>().albumDao() }
    single<RecentlyPlayedDao> { get<TuneScoutDatabase>().recentlyPlayedDao() }
    single<PlaybackSessionDao> { get<TuneScoutDatabase>().playbackSessionDao() }
    single<TimestampProvider> { TimestampProvider(System::currentTimeMillis) }
    singleOf(::RoomSongLocalDataSource).bind<SongLocalDataSource>()
    singleOf(::RoomAlbumLocalDataSource).bind<AlbumLocalDataSource>()
    singleOf(::RoomPlaybackSessionLocalDataSource).bind<PlaybackSessionLocalDataSource>()
    single<RecentlyPlayedLocalDataSource> {
        RoomRecentlyPlayedLocalDataSource(
            recentlyPlayedDao = get(),
            songDao = get(),
            timestampProvider = get(),
            maxEntries = MAX_RECENTLY_PLAYED,
        )
    }
}
