package com.quare.tunescout.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.quare.tunescout.ui.theme.TuneScoutColors
import com.quare.tunescout.ui.theme.TuneScoutSpacing

private val indicatorSize = 28.dp
private val strokeWidth = 3.dp

@Composable
fun LoadingIndicator(modifier: Modifier = Modifier) {
    val description = stringResource(R.string.ui_loading)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(TuneScoutSpacing.large)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color = TuneScoutColors.textPrimary,
            strokeWidth = strokeWidth,
            modifier = Modifier.size(indicatorSize),
        )
    }
}
