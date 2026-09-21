package com.pierre.tunescout.feature.library.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.feature.library.presentation.model.LibraryItemUiModel
import com.pierre.tunescout.ui.component.TuneScoutIcons
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.theme.TuneScoutSpacing
import com.pierre.tunescout.ui.component.R as ComponentR

private val downloadedIconSize = 14.dp

/**
 * The line under an item's name, led by the accent arrow when the user downloaded the item, the way
 * Spotify marks it in the library.
 *
 * @param isDownloaded whether the user asked to keep the whole item on the device.
 */
@Composable
internal fun LibraryItemSubtitleLine(
    item: LibraryItemUiModel,
    isDownloaded: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(TuneScoutSpacing.extraSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isDownloaded) {
            Icon(
                imageVector = TuneScoutIcons.downloaded,
                contentDescription = stringResource(ComponentR.string.ui_collection_state_downloaded),
                tint = TuneScoutColors.accent,
                modifier = Modifier.size(downloadedIconSize),
            )
        }
        Text(
            text = libraryItemSubtitle(item),
            style = MaterialTheme.typography.bodySmall,
            color = TuneScoutColors.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
