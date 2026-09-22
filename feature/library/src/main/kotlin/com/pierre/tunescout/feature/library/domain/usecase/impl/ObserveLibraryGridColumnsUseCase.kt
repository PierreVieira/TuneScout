package com.pierre.tunescout.feature.library.domain.usecase.impl

import com.pierre.tunescout.feature.library.domain.model.LibraryGridColumns
import com.pierre.tunescout.feature.library.domain.repository.LibraryRepository
import com.pierre.tunescout.feature.library.domain.usecase.ObserveLibraryGridColumns
import kotlinx.coroutines.flow.Flow

class ObserveLibraryGridColumnsUseCase(
    private val repository: LibraryRepository,
) : ObserveLibraryGridColumns {
    override fun invoke(): Flow<LibraryGridColumns> = repository.observeGridColumns()
}
