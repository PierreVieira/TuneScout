package com.pierre.tunescout.core.database.di

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.pierre.tunescout.core.database.AlbumLocalDataSource
import com.pierre.tunescout.core.database.FavoriteAlbumLocalDataSource
import com.pierre.tunescout.core.database.FavoriteSongLocalDataSource
import com.pierre.tunescout.core.database.LibrarySearchLocalDataSource
import com.pierre.tunescout.core.database.PlaybackSessionLocalDataSource
import com.pierre.tunescout.core.database.PlaylistLocalDataSource
import com.pierre.tunescout.core.database.RecentlyPlayedLocalDataSource
import com.pierre.tunescout.core.database.SongLocalDataSource
import com.pierre.tunescout.core.database.TuneScoutDatabase
import com.pierre.tunescout.core.database.dao.AlbumDao
import com.pierre.tunescout.core.database.dao.FavoriteAlbumDao
import com.pierre.tunescout.core.database.dao.FavoriteSongDao
import com.pierre.tunescout.core.database.dao.LibrarySearchDao
import com.pierre.tunescout.core.database.dao.PlaybackSessionDao
import com.pierre.tunescout.core.database.dao.PlaylistDao
import com.pierre.tunescout.core.database.dao.RecentlyPlayedDao
import com.pierre.tunescout.core.database.dao.SongDao
import com.pierre.tunescout.core.database.internal.MIGRATION_1_2
import com.pierre.tunescout.core.database.internal.MIGRATION_2_3
import com.pierre.tunescout.core.database.internal.MIGRATION_3_4
import com.pierre.tunescout.core.database.internal.MIGRATION_4_5
import com.pierre.tunescout.core.database.internal.RoomAlbumLocalDataSource
import com.pierre.tunescout.core.database.internal.RoomFavoriteAlbumLocalDataSource
import com.pierre.tunescout.core.database.internal.RoomFavoriteSongLocalDataSource
import com.pierre.tunescout.core.database.internal.RoomLibrarySearchLocalDataSource
import com.pierre.tunescout.core.database.internal.RoomPlaybackSessionLocalDataSource
import com.pierre.tunescout.core.database.internal.RoomPlaylistLocalDataSource
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
private const val MAX_RECENT_LIBRARY_SEARCHES = 10
private const val MAX_CACHED_SONGS = 500

val databaseModule: Module = module {
    single<TuneScoutDatabase> {
        Room
            .databaseBuilder<TuneScoutDatabase>(androidContext(), DATABASE_NAME)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
            .build()
    }
    single<SongDao> { get<TuneScoutDatabase>().songDao() }
    single<AlbumDao> { get<TuneScoutDatabase>().albumDao() }
    single<RecentlyPlayedDao> { get<TuneScoutDatabase>().recentlyPlayedDao() }
    single<PlaybackSessionDao> { get<TuneScoutDatabase>().playbackSessionDao() }
    single<PlaylistDao> { get<TuneScoutDatabase>().playlistDao() }
    single<FavoriteSongDao> { get<TuneScoutDatabase>().favoriteSongDao() }
    single<FavoriteAlbumDao> { get<TuneScoutDatabase>().favoriteAlbumDao() }
    single<LibrarySearchDao> { get<TuneScoutDatabase>().librarySearchDao() }
    single<TimestampProvider> { TimestampProvider(System::currentTimeMillis) }
    single<SongLocalDataSource> {
        RoomSongLocalDataSource(
            songDao = get(),
            timestampProvider = get(),
            maxCachedSongs = MAX_CACHED_SONGS,
        )
    }
    singleOf(::RoomAlbumLocalDataSource).bind<AlbumLocalDataSource>()
    singleOf(::RoomPlaybackSessionLocalDataSource).bind<PlaybackSessionLocalDataSource>()
    singleOf(::RoomPlaylistLocalDataSource).bind<PlaylistLocalDataSource>()
    singleOf(::RoomFavoriteSongLocalDataSource).bind<FavoriteSongLocalDataSource>()
    singleOf(::RoomFavoriteAlbumLocalDataSource).bind<FavoriteAlbumLocalDataSource>()
    single<LibrarySearchLocalDataSource> {
        RoomLibrarySearchLocalDataSource(
            librarySearchDao = get(),
            timestampProvider = get(),
            maxEntries = MAX_RECENT_LIBRARY_SEARCHES,
        )
    }
    single<RecentlyPlayedLocalDataSource> {
        RoomRecentlyPlayedLocalDataSource(
            recentlyPlayedDao = get(),
            songDao = get(),
            timestampProvider = get(),
            maxEntries = MAX_RECENTLY_PLAYED,
        )
    }
}
