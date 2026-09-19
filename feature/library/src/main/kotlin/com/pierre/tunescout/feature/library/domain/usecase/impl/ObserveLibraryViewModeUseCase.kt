package com.pierre.tunescout.feature.library.domain.usecase.impl

import com.pierre.tunescout.feature.library.domain.model.LibraryViewMode
import com.pierre.tunescout.feature.library.domain.repository.LibraryRepository
import com.pierre.tunescout.feature.library.domain.usecase.ObserveLibraryViewMode
import kotlinx.coroutines.flow.Flow

class ObserveLibraryViewModeUseCase(
    private val repository: LibraryRepository,
) : ObserveLibraryViewMode {
    override fun invoke(): Flow<LibraryViewMode> = repository.observeViewMode()
}
