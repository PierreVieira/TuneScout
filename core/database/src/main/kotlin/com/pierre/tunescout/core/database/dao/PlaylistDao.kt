package com.pierre.tunescout.core.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import com.pierre.tunescout.core.database.entity.PlaylistEntity
import com.pierre.tunescout.core.database.entity.PlaylistSongEntity
import com.pierre.tunescout.core.database.entity.SongEntity
import com.pierre.tunescout.core.database.relation.PlaylistRow
import kotlinx.coroutines.flow.Flow

@Dao
internal interface PlaylistDao {
    @Query(
        """
        SELECT playlists.id AS playlistId, playlists.name AS name, songs.artworkUrl AS artworkUrl
        FROM playlists
        LEFT JOIN playlist_songs ON playlist_songs.playlistId = playlists.id
        LEFT JOIN songs ON songs.id = playlist_songs.songId
        ORDER BY playlists.createdAt DESC, playlist_songs.position ASC
        """,
    )
    fun observeAllRows(): Flow<List<PlaylistRow>>

    @Query(
        """
        SELECT playlists.id AS playlistId, playlists.name AS name, songs.artworkUrl AS artworkUrl
        FROM playlists
        LEFT JOIN playlist_songs ON playlist_songs.playlistId = playlists.id
        LEFT JOIN songs ON songs.id = playlist_songs.songId
        WHERE playlists.id = :playlistId
        ORDER BY playlist_songs.position ASC
        """,
    )
    fun observeRows(playlistId: Long): Flow<List<PlaylistRow>>

    @Query(
        """
        SELECT songs.* FROM songs
        INNER JOIN playlist_songs ON playlist_songs.songId = songs.id
        WHERE playlist_songs.playlistId = :playlistId
        ORDER BY playlist_songs.position ASC
        """,
    )
    fun observeSongs(playlistId: Long): Flow<List<SongEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId)")
    fun observeContains(
        playlistId: Long,
        songId: Long,
    ): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId)")
    suspend fun hasSong(
        playlistId: Long,
        songId: Long,
    ): Boolean

    @Insert
    suspend fun insert(playlist: PlaylistEntity): Long

    @Query("UPDATE playlists SET name = :name WHERE id = :playlistId")
    suspend fun updateName(
        playlistId: Long,
        name: String,
    )

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deleteById(playlistId: Long)

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun getNextPosition(playlistId: Long): Int

    @Upsert
    suspend fun upsertSong(entry: PlaylistSongEntity)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun deleteSong(
        playlistId: Long,
        songId: Long,
    )

    @Transaction
    suspend fun appendSong(
        playlistId: Long,
        songId: Long,
    ) {
        if (hasSong(playlistId = playlistId, songId = songId)) return
        upsertSong(
            PlaylistSongEntity(
                playlistId = playlistId,
                songId = songId,
                position = getNextPosition(playlistId),
            ),
        )
    }
}
