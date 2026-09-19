package com.pierre.tunescout.feature.library.presentation.model

import com.pierre.tunescout.feature.library.domain.model.LibraryViewMode

data class LibraryUiState(
    val items: List<LibraryItemUiModel>,
    val viewMode: LibraryViewMode,
)
