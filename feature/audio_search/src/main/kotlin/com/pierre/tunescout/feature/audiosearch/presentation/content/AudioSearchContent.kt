package com.pierre.tunescout.feature.audiosearch.presentation.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.feature.audiosearch.R
import com.pierre.tunescout.feature.audiosearch.domain.model.SpeechFailure
import com.pierre.tunescout.feature.audiosearch.presentation.component.VoicePulseIcon
import com.pierre.tunescout.feature.audiosearch.presentation.model.AudioSearchUiEvent
import com.pierre.tunescout.feature.audiosearch.presentation.model.AudioSearchUiState
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.theme.TuneScoutSpacing

private const val TRANSCRIPT_MAX_LINES = 3
private val bottomPadding = 32.dp
private val transcriptMinHeight = 64.dp

@Composable
fun AudioSearchContent(
    uiState: AudioSearchUiState,
    onEvent: (AudioSearchUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = TuneScoutSpacing.screen)
            .padding(bottom = bottomPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(TuneScoutSpacing.large),
    ) {
        when (uiState) {
            is AudioSearchUiState.Listening -> ListeningContent(uiState)
            is AudioSearchUiState.Failed -> FailedContent(uiState, onEvent)
        }
    }
}

@Composable
private fun ListeningContent(uiState: AudioSearchUiState.Listening) {
    Text(
        text = stringResource(R.string.audio_search_listening_title),
        style = MaterialTheme.typography.titleMedium,
        color = TuneScoutColors.textPrimary,
        modifier = Modifier.semantics { heading() },
    )
    TranscriptText(transcript = uiState.transcript)
    VoicePulseIcon(level = uiState.level, isActive = true)
}

/**
 * The hint gives way to the words as they arrive, in the same box: the sheet keeps its height
 * instead of jumping on the first word.
 *
 * It is not a live region on purpose. The microphone is open while this is on screen, so a screen
 * reader speaking each word back would be heard as the next one; the search the words end in
 * announces its own outcome instead.
 */
@Composable
private fun TranscriptText(transcript: String) {
    val hasTranscript = transcript.isNotEmpty()
    Text(
        text = if (hasTranscript) transcript else stringResource(R.string.audio_search_listening_hint),
        style = if (hasTranscript) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.bodyLarge,
        color = if (hasTranscript) TuneScoutColors.textPrimary else TuneScoutColors.textSecondary,
        textAlign = TextAlign.Center,
        maxLines = TRANSCRIPT_MAX_LINES,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = transcriptMinHeight),
    )
}

@Composable
private fun FailedContent(
    uiState: AudioSearchUiState.Failed,
    onEvent: (AudioSearchUiEvent) -> Unit,
) {
    Text(
        text = stringResource(R.string.audio_search_failed_title),
        style = MaterialTheme.typography.titleMedium,
        color = TuneScoutColors.textPrimary,
        modifier = Modifier.semantics { heading() },
    )
    Text(
        text = failureMessage(uiState.failure),
        style = MaterialTheme.typography.bodyLarge,
        color = TuneScoutColors.textSecondary,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = transcriptMinHeight)
            .semantics { liveRegion = LiveRegionMode.Assertive },
    )
    VoicePulseIcon(level = 0f, isActive = false)
    Button(onClick = { onEvent(AudioSearchUiEvent.OnRetryClicked) }) {
        Text(text = stringResource(R.string.audio_search_retry))
    }
}

@Composable
private fun failureMessage(failure: SpeechFailure): String = stringResource(
    when (failure) {
        SpeechFailure.NothingHeard -> R.string.audio_search_failed_nothing_heard
        SpeechFailure.NoConnection -> R.string.audio_search_failed_no_connection
        SpeechFailure.Unknown -> R.string.audio_search_failed_unknown
    },
)
