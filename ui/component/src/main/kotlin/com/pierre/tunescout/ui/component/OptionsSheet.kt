package com.pierre.tunescout.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.theme.TuneScoutSpacing

private val menuItemHeight = 56.dp
private val menuIconSize = 24.dp
private val bottomPadding = 32.dp

@Composable
fun OptionsSheet(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    options: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = bottomPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HeaderText(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(horizontal = TuneScoutSpacing.large)
                .semantics { heading() },
        )
        HeaderText(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(
                top = TuneScoutSpacing.small,
                start = TuneScoutSpacing.large,
                end = TuneScoutSpacing.large,
            ),
        )
        Column(
            modifier = Modifier.padding(top = TuneScoutSpacing.large),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = options,
        )
    }
}

@Composable
fun OptionRow(
    icon: ImageVector,
    label: String,
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = menuItemHeight)
            .clickable(enabled = isEnabled, onClick = onClick)
            .padding(horizontal = TuneScoutSpacing.large + TuneScoutSpacing.small),
        horizontalArrangement = Arrangement.spacedBy(TuneScoutSpacing.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TuneScoutColors.textPrimary,
            modifier = Modifier.size(menuIconSize),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = TuneScoutColors.textPrimary,
        )
    }
}

@Composable
private fun HeaderText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = style,
        color = TuneScoutColors.textPrimary,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}
