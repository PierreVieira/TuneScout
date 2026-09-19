package com.pierre.tunescout.feature.library.domain.model

enum class LibraryViewMode {
    LIST,
    GRID,
    ;

    val toggled: LibraryViewMode
        get() = if (this == LIST) GRID else LIST
}
