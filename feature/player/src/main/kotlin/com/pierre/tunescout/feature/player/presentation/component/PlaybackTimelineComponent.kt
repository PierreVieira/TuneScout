package com.pierre.tunescout.feature.player.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.pierre.tunescout.core.utils.toClockString
import com.pierre.tunescout.feature.player.R
import com.pierre.tunescout.ui.component.SeekBar
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.theme.TuneScoutSpacing
import kotlin.time.Duration

private const val SECONDS_PER_MINUTE = 60

@Composable
internal fun PlaybackTimelineComponent(
    songId: Long,
    progress: Float,
    position: Duration,
    duration: Duration,
    onSeekFinished: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val remaining = duration - position
    val positionText = spokenDurationText(position)
    val remainingText = spokenDurationText(remaining)
    val elapsedDescription = stringResource(R.string.player_elapsed_description, positionText)
    val remainingDescription = stringResource(R.string.player_remaining_description, remainingText)
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(TuneScoutSpacing.extraSmall),
    ) {
        SeekBar(
            progress = progress,
            contentKey = songId,
            onSeekFinished = onSeekFinished,
            positionDescription = stringResource(
                R.string.player_position_description,
                positionText,
                spokenDurationText(duration),
            ),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = position.toClockString(),
                style = MaterialTheme.typography.bodyMedium,
                color = TuneScoutColors.textTertiary,
                modifier = Modifier.semantics { contentDescription = elapsedDescription },
            )
            Text(
                text = stringResource(R.string.player_remaining_time, remaining.toClockString()),
                style = MaterialTheme.typography.bodyMedium,
                color = TuneScoutColors.textTertiary,
                modifier = Modifier.semantics { contentDescription = remainingDescription },
            )
        }
    }
}

/**
 * A clock reads badly aloud — "-0:17" comes out as "minus zero seventeen" — so a screen reader gets
 * the same time in words, and a time under a minute leaves the minutes out.
 *
 * @return [duration] as it is said: "1 minute 23 seconds".
 */
@Composable
private fun spokenDurationText(duration: Duration): String {
    val totalSeconds = duration.inWholeSeconds.coerceAtLeast(0).toInt()
    val minutes = totalSeconds / SECONDS_PER_MINUTE
    val seconds = totalSeconds % SECONDS_PER_MINUTE
    val secondsText = pluralStringResource(R.plurals.player_seconds, seconds, seconds)
    val minutesText = pluralStringResource(R.plurals.player_minutes, minutes, minutes)
    return when {
        minutes == 0 -> secondsText
        seconds == 0 -> minutesText
        else -> stringResource(R.string.player_minutes_and_seconds, minutesText, secondsText)
    }
}
