package com.pierre.tunescout.feature.library.domain.usecase

import com.pierre.tunescout.feature.library.domain.model.LibraryViewMode

fun interface SetLibraryViewMode {
    suspend operator fun invoke(viewMode: LibraryViewMode)
}
