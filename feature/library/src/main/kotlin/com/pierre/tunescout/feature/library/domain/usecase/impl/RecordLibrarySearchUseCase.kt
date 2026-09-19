package com.pierre.tunescout.feature.library.domain.usecase.impl

import com.pierre.tunescout.core.model.LibraryItemKey
import com.pierre.tunescout.feature.library.domain.repository.LibraryRepository
import com.pierre.tunescout.feature.library.domain.usecase.RecordLibrarySearch

class RecordLibrarySearchUseCase(
    private val repository: LibraryRepository,
) : RecordLibrarySearch {
    override suspend fun invoke(key: LibraryItemKey) {
        repository.recordSearch(key)
    }
}
