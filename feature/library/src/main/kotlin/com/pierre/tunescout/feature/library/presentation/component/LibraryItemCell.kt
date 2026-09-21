package com.pierre.tunescout.feature.library.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.feature.library.R
import com.pierre.tunescout.feature.library.presentation.model.LibraryItemUiModel
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.theme.TuneScoutSpacing

private val cellCornerRadius = 8.dp

@Composable
internal fun LibraryItemCell(
    item: LibraryItemUiModel,
    name: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDownloaded: Boolean = false,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(cellCornerRadius))
            .clickable(onClickLabel = stringResource(R.string.library_open_item), onClick = onClick)
            .padding(bottom = TuneScoutSpacing.small),
        verticalArrangement = Arrangement.spacedBy(TuneScoutSpacing.small),
    ) {
        LibraryItemArtwork(
            item = item,
            size = LibraryArtworkSize.CELL,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            color = TuneScoutColors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        LibraryItemSubtitleLine(item = item, isDownloaded = isDownloaded)
    }
}
