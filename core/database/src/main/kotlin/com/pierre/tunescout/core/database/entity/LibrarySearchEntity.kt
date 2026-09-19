package com.pierre.tunescout.core.database.entity

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(tableName = "library_recent_searches", indices = [Index(value = ["searchedAt"])])
internal data class LibrarySearchEntity(
    @PrimaryKey val itemId: String,
    val searchedAt: Long,
)
