package com.pierre.tunescout.feature.library.domain.usecase.impl

import com.pierre.tunescout.feature.library.domain.model.LibraryGridColumns
import com.pierre.tunescout.feature.library.domain.repository.LibraryRepository
import com.pierre.tunescout.feature.library.domain.usecase.SetLibraryGridColumns

class SetLibraryGridColumnsUseCase(
    private val repository: LibraryRepository,
) : SetLibraryGridColumns {
    override suspend fun invoke(columns: LibraryGridColumns) {
        repository.setGridColumns(columns)
    }
}
