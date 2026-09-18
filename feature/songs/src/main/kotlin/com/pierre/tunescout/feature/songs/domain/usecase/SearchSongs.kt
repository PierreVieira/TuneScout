package com.pierre.tunescout.feature.songs.domain.usecase

import androidx.paging.PagingData
import com.pierre.tunescout.core.model.Song
import kotlinx.coroutines.flow.Flow

fun interface SearchSongs {
    operator fun invoke(term: String): Flow<PagingData<Song>>
}
