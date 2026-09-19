package com.pierre.tunescout.feature.library.domain.usecase.impl

import com.pierre.tunescout.core.model.LibraryItemKey
import com.pierre.tunescout.feature.library.domain.repository.LibraryRepository
import com.pierre.tunescout.feature.library.domain.usecase.ObserveRecentLibrarySearches
import kotlinx.coroutines.flow.Flow

class ObserveRecentLibrarySearchesUseCase(
    private val repository: LibraryRepository,
) : ObserveRecentLibrarySearches {
    override fun invoke(): Flow<List<LibraryItemKey>> = repository.observeRecentSearches()
}
