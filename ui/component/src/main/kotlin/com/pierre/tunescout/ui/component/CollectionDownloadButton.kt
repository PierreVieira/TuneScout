package com.pierre.tunescout.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.ui.theme.TuneScoutColors
import kotlin.math.roundToInt

private val targetSize = 48.dp
private val iconSize = 28.dp
private val progressSize = 28.dp
private val progressStrokeWidth = 2.dp
private val progressIconSize = 16.dp

/**
 * Downloading a whole collection — an album, a playlist, the liked songs — is a switch, the way
 * Spotify draws it: an outlined arrow while it is off, a ring filling up while the songs arrive, and
 * the filled accent arrow once they are all on the device. A tap turns it off at any point.
 *
 * The label stays "Download" and the role carries whether it is on, like [ShuffleButton]; the state
 * says how far it has got.
 *
 * @param progress how much of the collection is on the device: null while nobody asked for it, and
 * 1 once every song has arrived.
 * @param totalCount how many songs the collection holds, which the state reads out with how many of
 * them have arrived while the ring fills.
 */
@Composable
fun CollectionDownloadButton(
    progress: Float?,
    totalCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isRequested = progress != null
    val state = when {
        progress == null -> stringResource(R.string.ui_collection_state_not_downloaded)

        progress < 1f -> stringResource(
            R.string.ui_collection_state_downloading,
            (progress * totalCount).roundToInt(),
            totalCount,
        )

        else -> stringResource(R.string.ui_collection_state_downloaded)
    }
    Box(
        modifier = modifier
            .size(targetSize)
            .clip(CircleShape)
            .toggleable(value = isRequested, role = Role.Switch, onValueChange = { onClick() })
            .semantics { stateDescription = state },
        contentAlignment = Alignment.Center,
    ) {
        when {
            progress == null -> Icon(
                imageVector = TuneScoutIcons.download,
                contentDescription = stringResource(R.string.ui_download),
                tint = TuneScoutColors.textSecondary,
                modifier = Modifier.size(iconSize),
            )

            progress < 1f -> {
                CircularProgressIndicator(
                    progress = { progress },
                    color = TuneScoutColors.accent,
                    trackColor = TuneScoutColors.textTertiary,
                    strokeWidth = progressStrokeWidth,
                    modifier = Modifier.size(progressSize),
                )
                Icon(
                    imageVector = TuneScoutIcons.downloading,
                    contentDescription = stringResource(R.string.ui_download),
                    tint = TuneScoutColors.accent,
                    modifier = Modifier.size(progressIconSize),
                )
            }

            else -> Icon(
                imageVector = TuneScoutIcons.downloaded,
                contentDescription = stringResource(R.string.ui_download),
                tint = TuneScoutColors.accent,
                modifier = Modifier.size(iconSize),
            )
        }
    }
}
