package com.pierre.tunescout.feature.library.domain.model

/**
 * How many covers a row of the library grid holds. Three sizes and no more: at two a cover is as
 * large as a phone can draw two side by side, and past four a name no longer fits under its cover.
 *
 * @property count the number of columns.
 */
enum class LibraryGridColumns(
    val count: Int,
) {
    TWO(2),
    THREE(3),
    FOUR(4),
    ;

    /** The size after this one, and [TWO] again after [FOUR]: one button cycles through the three. */
    val next: LibraryGridColumns
        get() = entries[(ordinal + 1) % entries.size]
}
