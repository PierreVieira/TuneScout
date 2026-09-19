package com.pierre.tunescout.feature.library.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.pierre.tunescout.feature.library.R
import com.pierre.tunescout.feature.library.presentation.model.LibraryItemUiModel

/**
 * Only a playlist says it is one: calling the liked songs a playlist would name a row the user
 * never created, and an album is better described by who made it than by how long it is.
 */
@Composable
internal fun libraryItemSubtitle(item: LibraryItemUiModel): String = when (item) {
    is LibraryItemUiModel.Favorites -> songCountText(item.songCount)

    is LibraryItemUiModel.Playlist ->
        "${stringResource(R.string.library_playlist)} • ${songCountText(item.songCount)}"

    is LibraryItemUiModel.Album -> "${stringResource(R.string.library_album)} • ${item.artistName}"
}

@Composable
private fun songCountText(songCount: Int): String =
    pluralStringResource(R.plurals.library_song_count, songCount, songCount)
