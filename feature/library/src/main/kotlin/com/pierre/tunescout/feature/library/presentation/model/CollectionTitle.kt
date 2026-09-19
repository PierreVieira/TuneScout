package com.pierre.tunescout.feature.library.presentation.model

/**
 * The favourites list is named by a string resource and a playlist by whatever the user typed, so
 * the title only resolves to text inside the composable that draws it.
 */
sealed interface CollectionTitle {
    data object Favorites : CollectionTitle

    data class Custom(
        val name: String,
    ) : CollectionTitle
}
