package com.quare.tunescout.core.database.di

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.quare.tunescout.core.database.AlbumLocalDataSource
import com.quare.tunescout.core.database.RecentlyPlayedLocalDataSource
import com.quare.tunescout.core.database.SongLocalDataSource
import com.quare.tunescout.core.database.TuneScoutDatabase
import com.quare.tunescout.core.database.internal.RoomAlbumLocalDataSource
import com.quare.tunescout.core.database.internal.RoomRecentlyPlayedLocalDataSource
import com.quare.tunescout.core.database.internal.RoomSongLocalDataSource
import com.quare.tunescout.core.database.internal.TimestampProvider
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

private const val DATABASE_NAME = "tunescout.db"
private const val MAX_RECENTLY_PLAYED = 20

val databaseModule: Module = module {
    single<TuneScoutDatabase> {
        Room
            .databaseBuilder<TuneScoutDatabase>(androidContext(), DATABASE_NAME)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }
    single<TimestampProvider> { TimestampProvider(System::currentTimeMillis) }
    single<SongLocalDataSource> { RoomSongLocalDataSource(songDao = get<TuneScoutDatabase>().songDao()) }
    single<AlbumLocalDataSource> {
        RoomAlbumLocalDataSource(
            albumDao = get<TuneScoutDatabase>().albumDao(),
            songDao = get<TuneScoutDatabase>().songDao(),
            timestampProvider = get(),
        )
    }
    single<RecentlyPlayedLocalDataSource> {
        RoomRecentlyPlayedLocalDataSource(
            recentlyPlayedDao = get<TuneScoutDatabase>().recentlyPlayedDao(),
            songDao = get<TuneScoutDatabase>().songDao(),
            timestampProvider = get(),
            maxEntries = MAX_RECENTLY_PLAYED,
        )
    }
}
