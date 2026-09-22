package com.pierre.tunescout.feature.library.presentation.model

/**
 * The favourites row and the downloaded songs are named by a string resource, so the label has to
 * come from the composable that resolved it rather than from the ViewModel that built the list.
 *
 * @return whether the name of the item contains [query], ignoring case and surrounding spaces.
 */
fun LibraryItemUiModel.isMatching(
    query: String,
    favoritesName: String,
    downloadedSongsName: String,
): Boolean = getName(favoritesName, downloadedSongsName).contains(query.trim(), ignoreCase = true)

fun LibraryItemUiModel.getName(
    favoritesName: String,
    downloadedSongsName: String,
): String = when (this) {
    is LibraryItemUiModel.Favorites -> favoritesName
    is LibraryItemUiModel.Playlist -> name
    is LibraryItemUiModel.Album -> title
    is LibraryItemUiModel.DownloadedSongs -> downloadedSongsName
}
