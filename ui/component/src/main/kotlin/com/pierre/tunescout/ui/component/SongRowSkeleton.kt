package com.pierre.tunescout.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.ui.component.shimmer.ShimmerBox
import com.pierre.tunescout.ui.theme.TuneScoutSpacing

private val titleHeight = 16.dp
private val subtitleHeight = 12.dp
private val moreActionSize = 36.dp
private val moreDotSize = 3.5.dp
private val moreDotSpacing = 1.dp
private const val TITLE_WIDTH_FRACTION = 0.6f
private const val SUBTITLE_WIDTH_FRACTION = 0.4f
private const val MORE_DOT_COUNT = 3

@Composable
fun SongRowSkeleton(
    modifier: Modifier = Modifier,
    artworkSize: Dp = 52.dp,
    hasMoreAction: Boolean = false,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = TuneScoutSpacing.small),
        horizontalArrangement = Arrangement.spacedBy(TuneScoutSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(TuneScoutSpacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ShimmerBox(modifier = Modifier.size(artworkSize))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(TuneScoutSpacing.small),
            ) {
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
        if (hasMoreAction) {
            MoreActionSkeleton()
        }
    }
}

@Composable
fun SongListSkeleton(
    modifier: Modifier = Modifier,
    rows: Int = 6,
    artworkSize: Dp = 52.dp,
    hasMoreAction: Boolean = false,
) {
    val description = stringResource(R.string.ui_loading)
    Column(modifier = modifier.semantics { contentDescription = description }) {
        repeat(rows) {
            SongRowSkeleton(artworkSize = artworkSize, hasMoreAction = hasMoreAction)
        }
    }
}

/**
 * Stands in for [SongRowMoreAction], so it is laid out as the three dots of its icon rather than as
 * one block: a single box there reads as a piece of content the row does not have.
 */
@Composable
private fun MoreActionSkeleton() {
    Column(
        modifier = Modifier.size(moreActionSize),
        verticalArrangement = Arrangement.spacedBy(moreDotSpacing, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        repeat(MORE_DOT_COUNT) {
            ShimmerBox(shape = CircleShape, modifier = Modifier.size(moreDotSize))
        }
    }
}
