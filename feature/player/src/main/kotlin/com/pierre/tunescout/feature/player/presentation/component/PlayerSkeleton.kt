package com.pierre.tunescout.feature.player.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.feature.player.presentation.model.PlayerLayout
import com.pierre.tunescout.ui.component.R
import com.pierre.tunescout.ui.component.shimmer.ShimmerBox
import com.pierre.tunescout.ui.theme.TuneScoutSpacing

private val titleHeight = 32.dp
private val subtitleHeight = 16.dp
private val timelineHeight = 8.dp
private val playButtonSize = 72.dp
private val skipButtonSize = 36.dp
private val repeatButtonSize = 24.dp
private const val TITLE_WIDTH_FRACTION = 0.7f
private const val SUBTITLE_WIDTH_FRACTION = 0.45f

@Composable
internal fun PlayerSkeleton(
    layout: PlayerLayout,
    artworkSize: Dp,
    artworkTopSpacing: Dp,
    artworkCornerPercent: Int,
) {
    val description = stringResource(R.string.ui_loading)
    val artwork = @Composable {
        ShimmerBox(
            shape = RoundedCornerShape(percent = artworkCornerPercent),
            modifier = Modifier
                .size(artworkSize)
                .semantics {
                    contentDescription = description
                    liveRegion = LiveRegionMode.Polite
                },
        )
    }
    when (layout) {
        PlayerLayout.Stacked -> Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(artworkTopSpacing))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                artwork()
            }
            Spacer(modifier = Modifier.weight(1f))
            Column(
                modifier = Modifier.padding(horizontal = TuneScoutSpacing.large, vertical = TuneScoutSpacing.medium),
                verticalArrangement = Arrangement.spacedBy(TuneScoutSpacing.screen),
            ) {
                HeadingSkeleton()
                ControlsSkeleton()
            }
        }

        PlayerLayout.SideBySide -> Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = TuneScoutSpacing.large, vertical = TuneScoutSpacing.small),
            horizontalArrangement = Arrangement.spacedBy(TuneScoutSpacing.large),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            artwork()
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(TuneScoutSpacing.screen),
            ) {
                HeadingSkeleton()
                ControlsSkeleton()
            }
        }

        PlayerLayout.Compact -> Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = TuneScoutSpacing.large, vertical = TuneScoutSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(TuneScoutSpacing.screen, Alignment.CenterVertically),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(TuneScoutSpacing.medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                artwork()
                HeadingSkeleton(modifier = Modifier.weight(1f))
            }
            ControlsSkeleton()
        }
    }
}

@Composable
private fun HeadingSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
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

@Composable
private fun ControlsSkeleton() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(TuneScoutSpacing.screen),
    ) {
        ShimmerBox(
            shape = CircleShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(timelineHeight),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(TuneScoutSpacing.large),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ShimmerBox(shape = CircleShape, modifier = Modifier.size(playButtonSize))
            ShimmerBox(shape = CircleShape, modifier = Modifier.size(skipButtonSize))
            ShimmerBox(shape = CircleShape, modifier = Modifier.size(skipButtonSize))
            Spacer(modifier = Modifier.weight(1f))
            ShimmerBox(shape = CircleShape, modifier = Modifier.size(repeatButtonSize))
            ShimmerBox(shape = CircleShape, modifier = Modifier.size(repeatButtonSize))
            ShimmerBox(shape = CircleShape, modifier = Modifier.size(repeatButtonSize))
        }
    }
}
