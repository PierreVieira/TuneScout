package com.pierre.tunescout.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.theme.TuneScoutSpacing

private val noticeCornerRadius = 12.dp
private val noticeIconSize = 18.dp

/**
 * A line above the content saying why what is below it may not be the latest — no connection, a
 * refresh that failed. It never replaces the content: the point is that the cached rows stay on
 * screen and the reason for their age is stated once, quietly.
 *
 * The row is announced as one sentence and as a live region, so a screen reader reads the reason
 * when it appears instead of when focus happens to land on it.
 */
@Composable
fun NoticeBar(
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = TuneScoutSpacing.screen, vertical = TuneScoutSpacing.small)
            .clip(RoundedCornerShape(noticeCornerRadius))
            .background(TuneScoutColors.surfaceSubtle)
            .padding(horizontal = TuneScoutSpacing.medium, vertical = TuneScoutSpacing.small)
            .clearAndSetSemantics {
                contentDescription = text
                liveRegion = LiveRegionMode.Polite
            },
        horizontalArrangement = Arrangement.spacedBy(TuneScoutSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = TuneScoutIcons.offline,
            contentDescription = null,
            tint = TuneScoutColors.textSecondary,
            modifier = Modifier.size(noticeIconSize),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = TuneScoutColors.textSecondary,
        )
    }
}
