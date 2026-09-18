package com.pierre.tunescout.feature.player.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.ui.component.R
import com.pierre.tunescout.ui.component.shimmer.ShimmerBox
import com.pierre.tunescout.ui.theme.TuneScoutSpacing

private val titleHeight = 32.dp
private val subtitleHeight = 16.dp
private val timelineHeight = 8.dp
private val playButtonSize = 72.dp
private val skipButtonSize = 36.dp
private val controlsGap = 28.dp
private const val TITLE_WIDTH_FRACTION = 0.7f
private const val SUBTITLE_WIDTH_FRACTION = 0.45f

@Composable
internal fun ColumnScope.PlayerSkeleton(
    artworkTopSpacing: Dp,
    artworkSize: Dp,
    artworkCornerRadius: Dp,
) {
    val description = stringResource(R.string.ui_loading)
    Spacer(modifier = Modifier.height(artworkTopSpacing))
    ShimmerBox(
        shape = RoundedCornerShape(artworkCornerRadius),
        modifier = Modifier
            .size(artworkSize)
            .align(Alignment.CenterHorizontally)
            .semantics { contentDescription = description },
    )
    Spacer(modifier = Modifier.weight(1f))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = TuneScoutSpacing.large, vertical = TuneScoutSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(TuneScoutSpacing.screen),
    ) {
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
        ShimmerBox(
            shape = CircleShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(timelineHeight),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(controlsGap, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ShimmerBox(shape = CircleShape, modifier = Modifier.size(skipButtonSize))
            ShimmerBox(shape = CircleShape, modifier = Modifier.size(playButtonSize))
            ShimmerBox(shape = CircleShape, modifier = Modifier.size(skipButtonSize))
        }
    }
}
