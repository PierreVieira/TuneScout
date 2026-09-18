package com.quare.tunescout.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.quare.tunescout.ui.theme.TuneScoutColors

private val placeholderIconSize = 48.dp

@Composable
fun Artwork(
    url: String,
    contentDescription: String?,
    cornerRadius: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(cornerRadius))
            .background(TuneScoutColors.white10),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(TuneScoutIcons.musicList),
            contentDescription = null,
            tint = TuneScoutColors.elementPlaceholder,
            modifier = Modifier.size(placeholderIconSize),
        )
        AsyncImage(
            model = url,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier.aspectRatio(1f),
        )
    }
}
