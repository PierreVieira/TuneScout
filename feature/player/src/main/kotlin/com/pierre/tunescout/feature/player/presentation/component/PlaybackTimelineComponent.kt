package com.pierre.tunescout.feature.player.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.pierre.tunescout.core.utils.toClockString
import com.pierre.tunescout.feature.player.R
import com.pierre.tunescout.ui.component.SeekBar
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.theme.TuneScoutSpacing
import kotlin.time.Duration

@Composable
internal fun PlaybackTimelineComponent(
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
                color = TuneScoutColors.textTertiary,
            )
            Text(
                text = stringResource(R.string.player_remaining_time, (duration - position).toClockString()),
                style = MaterialTheme.typography.bodyMedium,
                color = TuneScoutColors.textTertiary,
            )
        }
    }
}
