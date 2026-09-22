package com.pierre.tunescout.feature.library.domain.usecase

import com.pierre.tunescout.feature.library.domain.model.LibraryGridColumns

fun interface SetLibraryGridColumns {
    suspend operator fun invoke(columns: LibraryGridColumns)
}
