package com.pierre.tunescout.ui.component

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.utils.network.LocalIsOffline

/**
 * What is drawn in place of an artwork that is not on screen. An artwork the device could not load
 * while it is offline says so, instead of wearing the same note as a song that simply has no cover:
 * the image is one connection away, not missing.
 *
 * @param hasFailed whether the artwork was asked for and did not arrive.
 */
@Composable
fun ArtworkPlaceholderIcon(
    hasFailed: Boolean,
    modifier: Modifier = Modifier,
) {
    val isOffline = hasFailed && LocalIsOffline.current
    Icon(
        imageVector = if (isOffline) TuneScoutIcons.offline else TuneScoutIcons.musicList,
        contentDescription = stringResource(R.string.ui_artwork_unavailable_offline).takeIf { isOffline },
        tint = TuneScoutColors.elementPlaceholder,
        modifier = modifier,
    )
}
