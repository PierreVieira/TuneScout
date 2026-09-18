package com.quare.tunescout.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.quare.tunescout.ui.theme.TuneScoutColors
import com.quare.tunescout.ui.theme.TuneScoutSpacing

private val topBarHeight = 50.dp
private val actionSize = 48.dp
private val actionIconSize = 24.dp

@Composable
fun TopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(topBarHeight),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = TuneScoutColors.textPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = TuneScoutSpacing.screen + actionSize),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = TuneScoutSpacing.screen),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBackClick != null) {
                TopBarAction(
                    iconRes = TuneScoutIcons.chevronLeft,
                    contentDescription = stringResource(R.string.ui_back),
                    onClick = onBackClick,
                )
            }
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                content = actions,
            )
        }
    }
}

@Composable
fun TopBarAction(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(actionSize),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            tint = TuneScoutColors.textPrimary,
            modifier = Modifier.size(actionIconSize),
        )
    }
}
