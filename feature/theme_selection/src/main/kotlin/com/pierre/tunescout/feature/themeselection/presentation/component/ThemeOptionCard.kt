package com.pierre.tunescout.feature.themeselection.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.feature.themeselection.presentation.model.ThemeOptionUiModel
import com.pierre.tunescout.ui.component.TuneScoutIcons
import com.pierre.tunescout.ui.theme.Theme
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.theme.TuneScoutSpacing

private val cardCornerRadius = 14.dp
private val selectedBorderWidth = 2.dp
private val borderWidth = 1.dp
private val badgeSize = 18.dp
private val badgeIconSize = 13.dp
private val badgePadding = 5.dp
private val labelIconSize = 16.dp
private val labelSpacing = 5.dp
private const val SELECTED_CONTAINER_ALPHA = 0.12f

@Composable
internal fun ThemeOptionCard(
    model: ThemeOptionUiModel,
    isDynamicColorEnabled: Boolean,
    onClick: (Theme) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isSelected = model.isSelected
    val accent = TuneScoutColors.textPrimary
    Card(
        onClick = { onClick(model.theme) },
        shape = RoundedCornerShape(cardCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) accent.copy(alpha = SELECTED_CONTAINER_ALPHA) else Color.Transparent,
        ),
        border = BorderStroke(
            width = if (isSelected) selectedBorderWidth else borderWidth,
            color = if (isSelected) accent else TuneScoutColors.elementSubtle,
        ),
        modifier = modifier.semantics { selected = isSelected },
    ) {
        Column(
            modifier = Modifier
                .padding(TuneScoutSpacing.small)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(TuneScoutSpacing.small),
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                ThemePreviewCard(
                    theme = model.theme,
                    isDynamicColorEnabled = isDynamicColorEnabled,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (isSelected) {
                    SelectedBadge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(badgePadding),
                    )
                }
            }
            OptionLabel(model = model, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun OptionLabel(
    model: ThemeOptionUiModel,
    modifier: Modifier = Modifier,
) {
    val title = stringResource(model.titleRes)
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(
            space = labelSpacing,
            alignment = Alignment.CenterHorizontally,
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = model.icon,
            contentDescription = null,
            tint = TuneScoutColors.textPrimary,
            modifier = Modifier.size(labelIconSize),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = TuneScoutColors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SelectedBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(badgeSize)
            .clip(CircleShape)
            .background(TuneScoutColors.textPrimary),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = TuneScoutIcons.check,
            contentDescription = null,
            tint = TuneScoutColors.background,
            modifier = Modifier.size(badgeIconSize),
        )
    }
}
