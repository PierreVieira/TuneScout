package com.quare.tunescout.feature.songs.domain.usecase

import androidx.paging.PagingData
import com.quare.tunescout.core.model.Song
import kotlinx.coroutines.flow.Flow

fun interface SearchSongs {
    operator fun invoke(term: String): Flow<PagingData<Song>>
}
