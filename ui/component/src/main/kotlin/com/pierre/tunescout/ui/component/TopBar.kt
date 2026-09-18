package com.pierre.tunescout.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.theme.TuneScoutSpacing

private val topBarHeight = 48.dp
private val actionSize = 48.dp
private val actionIconSize = 24.dp

@Composable
fun TopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(topBarHeight)
            .padding(horizontal = TuneScoutSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBackClick != null) {
            TopBarAction(
                icon = TuneScoutIcons.arrowBack,
                contentDescription = stringResource(R.string.ui_back),
                onClick = onBackClick,
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = TuneScoutColors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = TuneScoutSpacing.extraSmall),
        )
        actions()
    }
}

@Composable
fun TopBarAction(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(actionSize),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = TuneScoutColors.textPrimary,
            modifier = Modifier.size(actionIconSize),
        )
    }
}
