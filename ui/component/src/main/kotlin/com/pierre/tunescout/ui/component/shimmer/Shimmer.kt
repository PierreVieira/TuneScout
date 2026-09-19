package com.pierre.tunescout.ui.component.shimmer

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.defaultShimmerTheme
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmerSpec
import com.valentinilk.shimmer.shimmer as valentinilkShimmer

private const val SWEEP_EDGE_ALPHA = 0.55f
private const val SWEEP_MILLIS = 1_000
private const val SWEEP_PAUSE_MILLIS = 600
private val shimmerCornerRadius = 8.dp
private val tuneScoutShimmerTheme = defaultShimmerTheme.copy(
    animationSpec = infiniteRepeatable(
        animation = shimmerSpec(
            durationMillis = SWEEP_MILLIS,
            delayMillis = SWEEP_PAUSE_MILLIS,
            easing = LinearEasing,
        ),
        repeatMode = RepeatMode.Restart,
    ),
    shaderColors = listOf(
        Color.White.copy(alpha = SWEEP_EDGE_ALPHA),
        Color.White,
        Color.White.copy(alpha = SWEEP_EDGE_ALPHA),
    ),
)

/**
 * The library's sweep is a `DstIn` mask, so it only ever takes light away: the block is brightest
 * where the band is and fades towards its edges. On the dark palette the stock edge alpha of 0.25
 * leaves a placeholder at 5% white — invisible against a black background — so the edges are held
 * much higher and the pause between sweeps shortened.
 */
@Composable
fun Modifier.shimmer(): Modifier {
    val shimmer = rememberShimmer(shimmerBounds = ShimmerBounds.Window, theme = tuneScoutShimmerTheme)
    return valentinilkShimmer(customShimmer = shimmer)
}

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(shimmerCornerRadius),
    color: Color = TuneScoutColors.skeleton,
) {
    Box(
        modifier = modifier
            .clip(shape)
            .shimmer()
            .background(color),
    )
}
