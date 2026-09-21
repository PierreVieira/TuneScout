package com.pierre.tunescout.feature.album.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.ui.component.R
import com.pierre.tunescout.ui.component.SongRowSkeleton
import com.pierre.tunescout.ui.component.shimmer.ShimmerBox
import com.pierre.tunescout.ui.theme.TuneScoutSpacing

private val titleHeight = 20.dp
private val artistHeight = 14.dp
private val titleWidth = 200.dp
private val artistWidth = 90.dp
private const val SKELETON_ROWS = 6

@Composable
internal fun AlbumSkeleton(
    artworkSize: Dp,
    artworkCornerPercent: Int,
    rowArtworkSize: Dp,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(R.string.ui_loading)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = TuneScoutSpacing.screen)
            .semantics {
                contentDescription = description
                liveRegion = LiveRegionMode.Polite
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ShimmerBox(
            shape = RoundedCornerShape(percent = artworkCornerPercent),
            modifier = Modifier.size(artworkSize),
        )
        ShimmerBox(
            modifier = Modifier
                .padding(top = TuneScoutSpacing.medium)
                .size(width = titleWidth, height = titleHeight),
        )
        ShimmerBox(
            modifier = Modifier
                .padding(top = TuneScoutSpacing.small, bottom = TuneScoutSpacing.extraLarge)
                .size(width = artistWidth, height = artistHeight),
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Top,
        ) {
            repeat(SKELETON_ROWS) {
                SongRowSkeleton(artworkSize = rowArtworkSize)
            }
        }
    }
}
