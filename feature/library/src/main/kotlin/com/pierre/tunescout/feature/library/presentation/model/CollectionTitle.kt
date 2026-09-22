package com.pierre.tunescout.feature.library.presentation.model

/**
 * The favourites list and the downloaded songs are named by a string resource and a playlist by whatever the user typed, so
 * the title only resolves to text inside the composable that draws it.
 */
sealed interface CollectionTitle {
    data object Favorites : CollectionTitle

    data object DownloadedSongs : CollectionTitle

    data class Custom(
        val name: String,
    ) : CollectionTitle
}
