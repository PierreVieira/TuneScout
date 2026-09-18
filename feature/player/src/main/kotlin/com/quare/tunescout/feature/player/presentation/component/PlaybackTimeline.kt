package com.quare.tunescout.feature.player.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.quare.tunescout.core.utils.toClockString
import com.quare.tunescout.feature.player.R
import com.quare.tunescout.ui.component.SeekBar
import com.quare.tunescout.ui.theme.TuneScoutColors
import com.quare.tunescout.ui.theme.TuneScoutSpacing
import kotlin.time.Duration

@Composable
internal fun PlaybackTimeline(
    progress: Float,
    position: Duration,
    duration: Duration,
    onSeekFinished: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(TuneScoutSpacing.extraSmall),
    ) {
        SeekBar(
            progress = progress,
            onSeekFinished = onSeekFinished,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = position.toClockString(),
                style = MaterialTheme.typography.bodyMedium,
                color = TuneScoutColors.white60,
            )
            Text(
                text = stringResource(R.string.player_remaining_time, (duration - position).toClockString()),
                style = MaterialTheme.typography.bodyMedium,
                color = TuneScoutColors.white60,
            )
        }
    }
}
