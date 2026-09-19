package com.pierre.tunescout.feature.library.domain.usecase.impl

import com.pierre.tunescout.core.model.LibraryItemKey
import com.pierre.tunescout.feature.library.domain.repository.LibraryRepository
import com.pierre.tunescout.feature.library.domain.usecase.RemoveLibrarySearch

class RemoveLibrarySearchUseCase(
    private val repository: LibraryRepository,
) : RemoveLibrarySearch {
    override suspend fun invoke(key: LibraryItemKey) {
        repository.removeSearch(key)
    }
}
