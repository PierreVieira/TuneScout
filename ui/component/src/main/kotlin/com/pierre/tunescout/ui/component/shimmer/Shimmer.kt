package com.pierre.tunescout.ui.component.shimmer

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
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer as valentinilkShimmer

private val shimmerCornerRadius = 8.dp

@Composable
fun Modifier.shimmer(): Modifier {
    val shimmer = rememberShimmer(shimmerBounds = ShimmerBounds.Window)
    return valentinilkShimmer(customShimmer = shimmer)
}

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(shimmerCornerRadius),
    color: Color = TuneScoutColors.white10,
) {
    Box(
        modifier = modifier
            .clip(shape)
            .shimmer()
            .background(color),
    )
}
