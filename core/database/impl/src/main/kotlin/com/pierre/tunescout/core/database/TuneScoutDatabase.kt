package com.pierre.tunescout.core.database

import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.pierre.tunescout.core.database.dao.AlbumDao
import com.pierre.tunescout.core.database.dao.DownloadDao
import com.pierre.tunescout.core.database.dao.FavoriteAlbumDao
import com.pierre.tunescout.core.database.dao.FavoriteSongDao
import com.pierre.tunescout.core.database.dao.LibrarySearchDao
import com.pierre.tunescout.core.database.dao.PlaybackSessionDao
import com.pierre.tunescout.core.database.dao.PlaylistDao
import com.pierre.tunescout.core.database.dao.RecentlyPlayedDao
import com.pierre.tunescout.core.database.dao.SongDao
import com.pierre.tunescout.core.database.entity.AlbumEntity
import com.pierre.tunescout.core.database.entity.AlbumTrackOrderEntity
import com.pierre.tunescout.core.database.entity.DownloadedCollectionEntity
import com.pierre.tunescout.core.database.entity.DownloadedSongEntity
import com.pierre.tunescout.core.database.entity.DownloadedSongExclusionEntity
import com.pierre.tunescout.core.database.entity.FavoriteAlbumEntity
import com.pierre.tunescout.core.database.entity.FavoriteSongEntity
import com.pierre.tunescout.core.database.entity.LibrarySearchEntity
import com.pierre.tunescout.core.database.entity.PlaybackQueueEntity
import com.pierre.tunescout.core.database.entity.PlaybackSessionEntity
import com.pierre.tunescout.core.database.entity.PlaylistEntity
import com.pierre.tunescout.core.database.entity.PlaylistSongEntity
import com.pierre.tunescout.core.database.entity.RecentlyPlayedEntity
import com.pierre.tunescout.core.database.entity.SongEntity

@Database(
    entities = [
        SongEntity::class,
        AlbumEntity::class,
        RecentlyPlayedEntity::class,
        PlaybackQueueEntity::class,
        PlaybackSessionEntity::class,
        PlaylistEntity::class,
        PlaylistSongEntity::class,
        FavoriteSongEntity::class,
        FavoriteAlbumEntity::class,
        LibrarySearchEntity::class,
        AlbumTrackOrderEntity::class,
        DownloadedSongEntity::class,
        DownloadedCollectionEntity::class,
        DownloadedSongExclusionEntity::class,
    ],
    version = 10,
    exportSchema = true,
)
internal abstract class TuneScoutDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao

    abstract fun albumDao(): AlbumDao

    abstract fun recentlyPlayedDao(): RecentlyPlayedDao

    abstract fun playbackSessionDao(): PlaybackSessionDao

    abstract fun playlistDao(): PlaylistDao

    abstract fun favoriteSongDao(): FavoriteSongDao

    abstract fun favoriteAlbumDao(): FavoriteAlbumDao

    abstract fun librarySearchDao(): LibrarySearchDao

    abstract fun downloadDao(): DownloadDao
}
