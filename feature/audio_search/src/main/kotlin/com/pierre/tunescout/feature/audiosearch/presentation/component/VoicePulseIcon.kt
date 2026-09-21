package com.pierre.tunescout.feature.audiosearch.presentation.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.ui.component.TuneScoutIcons
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.utils.animation.rememberReduceMotion

private const val BREATH_DURATION_MILLIS = 900
private const val BREATH_SCALE = 0.08f
private const val INNER_HALO_REACH = 0.45f
private const val OUTER_HALO_REACH = 0.9f
private const val INNER_HALO_ALPHA = 0.28f
private const val OUTER_HALO_ALPHA = 0.14f
private val pulseSize = 168.dp
private val coreSize = 80.dp
private val iconSize = 36.dp

/**
 * The microphone with two halos that swell with the voice. While nothing is said the halos still
 * breathe a little, so the sheet reads as listening rather than as stuck. With motion reduced they
 * only answer the voice: that movement is feedback, the breathing is decoration.
 *
 * @param level how loud the voice is right now, from 0 (silence) to 1.
 * @param isActive whether the microphone is open; a closed one is drawn muted and still.
 */
@Composable
internal fun VoicePulseIcon(
    level: Float,
    isActive: Boolean,
    modifier: Modifier = Modifier,
) {
    val voice by animateFloatAsState(
        targetValue = if (isActive) level else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "voiceLevel",
    )
    val breath = if (isActive && !rememberReduceMotion()) breath() else remember { mutableFloatStateOf(0f) }
    val coreColor = if (isActive) TuneScoutColors.accent else TuneScoutColors.elementMuted
    Box(
        modifier = modifier.size(pulseSize),
        contentAlignment = Alignment.Center,
    ) {
        HaloBox(
            color = coreColor.copy(alpha = OUTER_HALO_ALPHA),
            scale = { 1f + breath.value + voice * OUTER_HALO_REACH },
        )
        HaloBox(
            color = coreColor.copy(alpha = INNER_HALO_ALPHA),
            scale = { 1f + breath.value + voice * INNER_HALO_REACH },
        )
        Box(
            modifier = Modifier
                .size(coreSize)
                .background(coreColor, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = TuneScoutIcons.microphone,
                contentDescription = null,
                tint = TuneScoutColors.background,
                modifier = Modifier.size(iconSize),
            )
        }
    }
}

/**
 * The scale is read inside `graphicsLayer`, so a level that changes many times a second redraws the
 * halo without recomposing it.
 */
@Composable
private fun HaloBox(
    color: Color,
    scale: () -> Float,
) {
    Box(
        modifier = Modifier
            .size(coreSize)
            .graphicsLayer {
                scaleX = scale()
                scaleY = scale()
            }.background(color, CircleShape),
    )
}

@Composable
private fun breath(): State<Float> = rememberInfiniteTransition(label = "voiceBreath").animateFloat(
    initialValue = 0f,
    targetValue = BREATH_SCALE,
    animationSpec = infiniteRepeatable(
        animation = tween(durationMillis = BREATH_DURATION_MILLIS),
        repeatMode = RepeatMode.Reverse,
    ),
    label = "voiceBreathScale",
)
