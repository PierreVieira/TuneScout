package com.pierre.tunescout.feature.player.presentation.component

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.theme.TuneScoutSpacing

@Composable
internal fun SongHeading(
    title: String,
    artistName: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(TuneScoutSpacing.extraSmall),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.displayMedium,
            color = TuneScoutColors.textPrimary,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .basicMarquee(),
        )
        Text(
            text = artistName,
            style = MaterialTheme.typography.bodyLarge,
            color = TuneScoutColors.white70,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
