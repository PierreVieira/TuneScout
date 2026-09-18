package com.pierre.tunescout.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.theme.TuneScoutSpacing

private val rowCornerRadius = 8.dp
private val artworkCornerRadius = 8.dp
private val moreButtonSize = 36.dp
private val moreIconSize = 20.dp

@Composable
fun SongRow(
    title: String,
    subtitle: String,
    artworkUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    artworkSize: Dp = 52.dp,
    isHighlighted: Boolean = false,
    onMoreClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(rowCornerRadius))
            .clickable(onClick = onClick)
            .padding(vertical = TuneScoutSpacing.small),
        horizontalArrangement = Arrangement.spacedBy(TuneScoutSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(TuneScoutSpacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Artwork(
                url = artworkUrl,
                contentDescription = null,
                cornerRadius = artworkCornerRadius,
                modifier = Modifier.size(artworkSize),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(TuneScoutSpacing.extraSmall),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TuneScoutColors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isHighlighted) TuneScoutColors.white70 else TuneScoutColors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (onMoreClick != null) {
            IconButton(
                onClick = onMoreClick,
                modifier = Modifier.size(moreButtonSize),
            ) {
                Icon(
                    painter = painterResource(TuneScoutIcons.moreMenu),
                    contentDescription = stringResource(R.string.ui_more_options),
                    tint = TuneScoutColors.elementMuted,
                    modifier = Modifier.size(moreIconSize),
                )
            }
        }
    }
}
