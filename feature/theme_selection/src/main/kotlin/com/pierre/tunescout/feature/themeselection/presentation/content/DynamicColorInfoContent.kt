package com.pierre.tunescout.feature.themeselection.presentation.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.feature.themeselection.R
import com.pierre.tunescout.feature.themeselection.presentation.model.DynamicColorInfoUiEvent
import com.pierre.tunescout.feature.themeselection.presentation.model.DynamicColorInfoUiState
import com.pierre.tunescout.ui.component.TuneScoutIcons
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.theme.TuneScoutSpacing

private val dialogCornerRadius = 16.dp
private val dialogMaxWidth = 560.dp
private val headerIconSize = 32.dp
private val toggleRowMinHeight = 48.dp

@Composable
fun DynamicColorInfoContent(
    uiState: DynamicColorInfoUiState,
    onEvent: (DynamicColorInfoUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = dialogMaxWidth),
        shape = RoundedCornerShape(dialogCornerRadius),
        color = TuneScoutColors.sheet,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = TuneScoutSpacing.large, vertical = TuneScoutSpacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(TuneScoutSpacing.small + TuneScoutSpacing.extraSmall),
        ) {
            Icon(
                imageVector = TuneScoutIcons.info,
                contentDescription = null,
                tint = TuneScoutColors.textPrimary,
                modifier = Modifier.size(headerIconSize),
            )
            Text(
                text = stringResource(R.string.theme_selection_dynamic_color),
                style = MaterialTheme.typography.titleLarge,
                color = TuneScoutColors.textPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { heading() },
            )
            ToggleRow(
                isEnabled = uiState,
                onToggle = { isEnabled -> onEvent(DynamicColorInfoUiEvent.OnDynamicColorToggled(isEnabled)) },
            )
            Text(
                text = stringResource(R.string.theme_selection_dynamic_color_explanation),
                style = MaterialTheme.typography.bodyMedium,
                color = TuneScoutColors.textPrimary,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = stringResource(R.string.theme_selection_dynamic_color_availability),
                style = MaterialTheme.typography.bodySmall,
                color = TuneScoutColors.textSecondary,
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = { onEvent(DynamicColorInfoUiEvent.OnGotItClicked) },
                modifier = Modifier.padding(top = TuneScoutSpacing.extraSmall),
            ) {
                Text(text = stringResource(R.string.theme_selection_got_it))
            }
        }
    }
}

@Composable
private fun ToggleRow(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = TuneScoutSpacing.extraSmall)
            .heightIn(min = toggleRowMinHeight)
            .toggleable(value = isEnabled, role = Role.Switch, onValueChange = onToggle),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(R.string.theme_selection_dynamic_color_toggle),
            style = MaterialTheme.typography.bodyLarge,
            color = TuneScoutColors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        Switch(
            checked = isEnabled,
            onCheckedChange = null,
        )
    }
}
