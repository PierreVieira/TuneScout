package com.pierre.tunescout.core.database.relation

import androidx.room3.Embedded
import androidx.room3.Relation
import com.pierre.tunescout.core.database.entity.AlbumEntity
import com.pierre.tunescout.core.database.entity.SongEntity

internal data class AlbumWithSongs(
    @Embedded val album: AlbumEntity,
    @Relation(parentColumns = ["id"], entityColumns = ["albumId"])
    val songs: List<SongEntity>,
)
