package com.quare.tunescout.ui.component

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
import androidx.compose.ui.text.style.TextAlign
import com.quare.tunescout.ui.theme.TuneScoutColors
import com.quare.tunescout.ui.theme.TuneScoutSpacing

@Composable
fun StateMessage(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    onRetry: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = TuneScoutSpacing.large, vertical = TuneScoutSpacing.extraLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(TuneScoutSpacing.small),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = TuneScoutColors.textPrimary,
            textAlign = TextAlign.Center,
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
