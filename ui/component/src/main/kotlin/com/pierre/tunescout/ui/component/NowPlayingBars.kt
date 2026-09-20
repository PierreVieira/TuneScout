package com.pierre.tunescout.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.ui.theme.TuneScoutColors

private val barsSize = 14.dp
private val barDurationsMillis = listOf(420, 580, 500)
private val pausedBarFractions = listOf(0.4f, 0.75f, 0.55f)
private const val LOWEST_BAR_FRACTION = 0.2f
private const val TALLEST_BAR_FRACTION = 1f
private const val BAR_AND_GAP_WIDTHS = 2

/**
 * The three bars beside the song that is playing. A pause freezes them at a shape of their own
 * rather than wherever the animation was: an infinite transition cannot be paused, and resuming one
 * from the height it stopped at would leave that bar bouncing between there and the top forever.
 */
@Composable
fun NowPlayingBars(
    state: NowPlayingState,
    modifier: Modifier = Modifier,
) {
    if (state == NowPlayingState.None) return
    val fractions = barFractions(isAnimating = state == NowPlayingState.Playing)
    val color = TuneScoutColors.accent
    val label = stringResource(
        if (state == NowPlayingState.Playing) R.string.ui_now_playing else R.string.ui_now_playing_paused,
    )
    Canvas(
        modifier = modifier
            .size(barsSize)
            .semantics { contentDescription = label },
    ) {
        drawBars(fractions = fractions, color = color)
    }
}

@Composable
private fun barFractions(isAnimating: Boolean): List<Float> {
    if (!isAnimating) return pausedBarFractions
    val transition = rememberInfiniteTransition(label = "nowPlayingBars")
    return barDurationsMillis.map { durationMillis ->
        transition
            .animateFloat(
                initialValue = LOWEST_BAR_FRACTION,
                targetValue = TALLEST_BAR_FRACTION,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = durationMillis, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "nowPlayingBar$durationMillis",
            ).value
    }
}

private fun DrawScope.drawBars(
    fractions: List<Float>,
    color: Color,
) {
    val barWidth = size.width / (fractions.size * BAR_AND_GAP_WIDTHS - 1)
    fractions.forEachIndexed { index, fraction ->
        val barHeight = size.height * fraction
        drawRoundRect(
            color = color,
            topLeft = Offset(x = index * barWidth * BAR_AND_GAP_WIDTHS, y = size.height - barHeight),
            size = Size(width = barWidth, height = barHeight),
            cornerRadius = CornerRadius(barWidth / 2),
        )
    }
}
