package com.pierre.tunescout.feature.library.domain.usecase.impl

import com.pierre.tunescout.feature.library.domain.model.LibraryViewMode
import com.pierre.tunescout.feature.library.domain.repository.LibraryRepository
import com.pierre.tunescout.feature.library.domain.usecase.SetLibraryViewMode

class SetLibraryViewModeUseCase(
    private val repository: LibraryRepository,
) : SetLibraryViewMode {
    override suspend fun invoke(viewMode: LibraryViewMode) {
        repository.setViewMode(viewMode)
    }
}
