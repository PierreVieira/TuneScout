package com.pierre.tunescout.feature.themeselection.presentation.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.feature.themeselection.R
import com.pierre.tunescout.feature.themeselection.presentation.component.ThemeOptionCard
import com.pierre.tunescout.feature.themeselection.presentation.model.ThemeSelectionUiEvent
import com.pierre.tunescout.feature.themeselection.presentation.model.ThemeSelectionUiState
import com.pierre.tunescout.ui.component.TuneScoutIcons
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.theme.TuneScoutSpacing

private val optionSpacing = 10.dp
private val bottomPadding = 32.dp
private val infoButtonSize = 48.dp
private val infoIconSize = 20.dp

@Composable
fun ThemeSelectionContent(
    uiState: ThemeSelectionUiState,
    onEvent: (ThemeSelectionUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = TuneScoutSpacing.screen)
            .padding(bottom = bottomPadding),
        verticalArrangement = Arrangement.spacedBy(TuneScoutSpacing.medium),
    ) {
        Text(
            text = stringResource(R.string.theme_selection_title),
            style = MaterialTheme.typography.titleMedium,
            color = TuneScoutColors.textPrimary,
            modifier = Modifier.semantics { heading() },
        )
        Row(
            modifier = Modifier.selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(optionSpacing),
        ) {
            uiState.options.forEach { option ->
                ThemeOptionCard(
                    model = option,
                    isDynamicColorEnabled = uiState.isDynamicColorEnabled == true,
                    onClick = { theme -> onEvent(ThemeSelectionUiEvent.OnThemeClicked(theme)) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        uiState.isDynamicColorEnabled?.let { isEnabled ->
            HorizontalDivider(color = TuneScoutColors.elementSubtle)
            DynamicColorRow(
                isEnabled = isEnabled,
                onToggle = { newValue -> onEvent(ThemeSelectionUiEvent.OnDynamicColorToggled(newValue)) },
                onInfoClick = { onEvent(ThemeSelectionUiEvent.OnDynamicColorInfoClicked) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun DynamicColorRow(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.toggleable(value = isEnabled, role = Role.Switch, onValueChange = onToggle),
        horizontalArrangement = Arrangement.spacedBy(TuneScoutSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.theme_selection_dynamic_color),
                style = MaterialTheme.typography.bodyLarge,
                color = TuneScoutColors.textPrimary,
            )
            Text(
                text = stringResource(R.string.theme_selection_dynamic_color_description),
                style = MaterialTheme.typography.bodySmall,
                color = TuneScoutColors.textSecondary,
            )
        }
        IconButton(
            onClick = onInfoClick,
            modifier = Modifier.size(infoButtonSize),
        ) {
            Icon(
                imageVector = TuneScoutIcons.info,
                contentDescription = stringResource(R.string.theme_selection_dynamic_color_info),
                tint = TuneScoutColors.textSecondary,
                modifier = Modifier.size(infoIconSize),
            )
        }
        Switch(
            checked = isEnabled,
            onCheckedChange = null,
        )
    }
}
