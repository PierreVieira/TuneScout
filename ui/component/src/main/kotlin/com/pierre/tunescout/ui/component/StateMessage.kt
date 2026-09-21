package com.pierre.tunescout.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.theme.TuneScoutSpacing

/**
 * What a screen says in place of its content: nothing here yet, nothing found, something failed.
 *
 * @param isAnnounced whether a screen reader reads the message the moment it appears. It is for a
 * message that answers something the user just did — a search that found nothing, or failed — and
 * not for one that is simply what the screen opens on, which focus reaches on its own. An announced
 * message is one node, so the title and the description are read as the one sentence they are.
 */
@Composable
fun StateMessage(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    isAnnounced: Boolean = false,
    onRetry: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = TuneScoutSpacing.large, vertical = TuneScoutSpacing.extraLarge)
            .semantics(mergeDescendants = isAnnounced) { if (isAnnounced) liveRegion = LiveRegionMode.Polite },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(TuneScoutSpacing.small),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = TuneScoutColors.textPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )
        if (description != null) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = TuneScoutColors.textSecondary,
                textAlign = TextAlign.Center,
            )
        }
        if (onRetry != null) {
            TextButton(
                onClick = onRetry,
                colors = ButtonDefaults.textButtonColors(contentColor = TuneScoutColors.textPrimary),
            ) {
                Text(text = stringResource(R.string.ui_retry))
            }
        }
    }
}
