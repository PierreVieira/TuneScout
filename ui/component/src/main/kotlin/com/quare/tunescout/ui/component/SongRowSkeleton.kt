package com.quare.tunescout.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.quare.tunescout.ui.component.shimmer.ShimmerBox
import com.quare.tunescout.ui.theme.TuneScoutSpacing

private val titleHeight = 16.dp
private val subtitleHeight = 12.dp
private const val TITLE_WIDTH_FRACTION = 0.6f
private const val SUBTITLE_WIDTH_FRACTION = 0.4f

@Composable
fun SongRowSkeleton(
    modifier: Modifier = Modifier,
    artworkSize: Dp = 52.dp,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = TuneScoutSpacing.small),
        horizontalArrangement = Arrangement.spacedBy(TuneScoutSpacing.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ShimmerBox(modifier = Modifier.size(artworkSize))
        Column(verticalArrangement = Arrangement.spacedBy(TuneScoutSpacing.small)) {
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(TITLE_WIDTH_FRACTION)
                    .height(titleHeight),
            )
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(SUBTITLE_WIDTH_FRACTION)
                    .height(subtitleHeight),
            )
        }
    }
}

@Composable
fun SongListSkeleton(
    modifier: Modifier = Modifier,
    rows: Int = 6,
    artworkSize: Dp = 52.dp,
) {
    val description = stringResource(R.string.ui_loading)
    Column(modifier = modifier.semantics { contentDescription = description }) {
        repeat(rows) {
            SongRowSkeleton(artworkSize = artworkSize)
        }
    }
}
