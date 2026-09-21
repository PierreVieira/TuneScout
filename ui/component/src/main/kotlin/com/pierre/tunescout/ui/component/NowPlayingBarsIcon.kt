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
import androidx.compose.runtime.State
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
private val barStartPhases = listOf(0.25f, 0.5f, 0.375f)
private const val LOWEST_BAR_FRACTION = 0.2f
private const val TALLEST_BAR_FRACTION = 1f
private const val BAR_AND_GAP_WIDTHS = 2
private const val HALF_CYCLE = 0.5f
private const val CYCLES_PER_SWEEP = 2

/**
 * The three bouncing bars beside the song that is playing. They only exist while it plays: a
 * paused song keeps its highlighted title, but the bars leave with the sound, so the caller decides
 * when to show them.
 */
@Composable
fun NowPlayingBarsIcon(modifier: Modifier = Modifier) {
    val phases = barPhases()
    val color = TuneScoutColors.accent
    val label = stringResource(R.string.ui_now_playing)
    Canvas(
        modifier = modifier
            .size(barsSize)
            .semantics { contentDescription = label },
    ) {
        drawBars(fractions = phases.map { phase -> phase.value.toBarFraction() }, color = color)
    }
}

/**
 * Each bar sweeps a phase instead of a height, and starts partway into its cycle, so the very first
 * frame already reads as an equalizer — 0.6, 1.0 and 0.8 of the height — instead of three bars at the
 * floor, which look like an ellipsis. A frame is drawn before any animation has run (and a screenshot
 * captures exactly that one), so the stagger has to live in the initial value, not in a start offset.
 *
 * The phases come back as states, not values, so only the drawing reads them: read here, every frame
 * of the animation would recompose the icon for as long as the song plays.
 *
 * @return the phase of each bar, left to right.
 */
@Composable
private fun barPhases(): List<State<Float>> {
    val transition = rememberInfiniteTransition(label = "nowPlayingBars")
    return barDurationsMillis.zip(barStartPhases) { durationMillis, startPhase ->
        transition.animateFloat(
            initialValue = startPhase,
            targetValue = startPhase + 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = durationMillis * CYCLES_PER_SWEEP, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "nowPlayingBar$durationMillis",
        )
    }
}

/**
 * The height of a bar at this phase: from the lowest to the tallest in the first half of a cycle and
 * back down in the second, the same linear bounce a reversing animation between the two draws.
 *
 * @return the bar's height, as a fraction of the icon.
 */
private fun Float.toBarFraction(): Float {
    val position = this % 1f
    val rise = if (position < HALF_CYCLE) position / HALF_CYCLE else (1f - position) / HALF_CYCLE
    return LOWEST_BAR_FRACTION + (TALLEST_BAR_FRACTION - LOWEST_BAR_FRACTION) * rise
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
