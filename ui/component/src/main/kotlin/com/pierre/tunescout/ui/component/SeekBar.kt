package com.pierre.tunescout.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.ui.theme.TuneScoutColors

private val seekHeight = 24.dp
private val trackHeight = 8.dp
private val trackCornerRadius = 20.dp
private val handleSize = 24.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeekBar(
    progress: Float,
    onSeekFinished: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(progress) }
    val shownProgress = if (isDragging) dragProgress else progress
    val description = stringResource(R.string.ui_seek_bar)
    Slider(
        value = shownProgress,
        onValueChange = { value ->
            isDragging = true
            dragProgress = value
        },
        onValueChangeFinished = {
            isDragging = false
            onSeekFinished(dragProgress)
        },
        modifier = modifier
            .fillMaxWidth()
            .height(seekHeight)
            .semantics { contentDescription = description },
        thumb = { Handle() },
        track = { sliderState -> Track(sliderState) },
    )
}

@Composable
private fun Handle() {
    Box(
        modifier = Modifier
            .size(handleSize)
            .background(TuneScoutColors.textPrimary, CircleShape),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Track(sliderState: SliderState) {
    val fraction = sliderState.value.coerceIn(0f, 1f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(trackHeight)
            .background(TuneScoutColors.white25, RoundedCornerShape(trackCornerRadius)),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction)
                .height(trackHeight)
                .background(TuneScoutColors.white60, RoundedCornerShape(trackCornerRadius)),
        )
    }
}
