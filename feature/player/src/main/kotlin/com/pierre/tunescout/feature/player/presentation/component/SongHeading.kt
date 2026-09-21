package com.pierre.tunescout.feature.player.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import com.pierre.tunescout.ui.component.SongSharedElement
import com.pierre.tunescout.ui.component.SongSharedKey
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.theme.TuneScoutSpacing
import com.pierre.tunescout.ui.utils.animation.loopingMarquee
import com.pierre.tunescout.ui.utils.animation.sharedTextBounds

@Composable
internal fun SongHeading(
    songId: Long,
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
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .sharedTextBounds(SongSharedKey.createOrNull(songId, SongSharedElement.TITLE))
                .loopingMarquee()
                .semantics { heading() },
        )
        Text(
            text = artistName,
            style = MaterialTheme.typography.bodyLarge,
            color = TuneScoutColors.textEmphasis,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .sharedTextBounds(SongSharedKey.createOrNull(songId, SongSharedElement.ARTIST)),
        )
    }
}
