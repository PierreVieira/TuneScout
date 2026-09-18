package com.quare.tunescout.feature.songs.domain.usecase.impl

import androidx.paging.PagingData
import com.quare.tunescout.core.model.Song
import com.quare.tunescout.feature.songs.domain.repository.SongsRepository
import com.quare.tunescout.feature.songs.domain.usecase.SearchSongs
import kotlinx.coroutines.flow.Flow

internal class SearchSongsUseCase(
    private val repository: SongsRepository,
) : SearchSongs {
    override fun invoke(term: String): Flow<PagingData<Song>> = repository.searchSongs(term)
}
