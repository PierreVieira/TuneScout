package com.pierre.tunescout.feature.library.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.pierre.tunescout.feature.library.R
import com.pierre.tunescout.feature.library.presentation.model.LibraryItemUiModel

/**
 * Only a playlist says it is one: calling the liked songs a playlist would name a row the user
 * never created.
 */
@Composable
internal fun libraryItemSubtitle(item: LibraryItemUiModel): String {
    val count = pluralStringResource(R.plurals.library_song_count, item.songCount, item.songCount)
    return when (item) {
        is LibraryItemUiModel.Favorites -> count
        is LibraryItemUiModel.Playlist -> "${stringResource(R.string.library_playlist)} • $count"
    }
}
