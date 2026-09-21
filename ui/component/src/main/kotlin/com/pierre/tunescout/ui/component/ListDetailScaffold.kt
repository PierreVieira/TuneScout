package com.pierre.tunescout.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.ui.theme.LocalTuneScoutColorPalette
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.theme.TuneScoutSpacing

private val detailPaneCornerRadius = 16.dp

/**
 * The list the user picks from and, beside it, what they picked. The detail sits on a raised card, the
 * way the design lays the queue beside the player, so the two panes read as one choice and its result
 * rather than as two screens that happen to share the window.
 *
 * Each pane is only padded for the window edges it touches: the list stops short of the side the
 * detail covers, and the detail takes no padding for the side the list covers. The card itself is
 * padded for the system bars, so the detail inside it lays out as if it had a window of its own.
 *
 * The card is [TuneScoutColors.sheet], and it is the detail's background too: rows that paint the
 * background behind themselves, so a swipe can uncover what is under them, paint the card instead of
 * cutting black stripes through it.
 *
 * @param listPane the pane on the start side.
 * @param detailPane the pane on the end side, on the card.
 * @param modifier the modifier applied to the row holding both panes.
 */
@Composable
fun ListDetailScaffold(
    listPane: @Composable () -> Unit,
    detailPane: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .consumeWindowInsets(WindowInsets.safeDrawing.only(WindowInsetsSides.End)),
            propagateMinConstraints = true,
        ) {
            listPane()
        }
        DetailPaneCard(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            content = detailPane,
        )
    }
}

@Composable
private fun DetailPaneCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val palette = LocalTuneScoutColorPalette.current
    Box(
        modifier = modifier
            .consumeWindowInsets(WindowInsets.safeDrawing.only(WindowInsetsSides.Start))
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.End + WindowInsetsSides.Bottom),
            ).padding(
                top = TuneScoutSpacing.small,
                end = TuneScoutSpacing.small,
                bottom = TuneScoutSpacing.small,
            ).clip(RoundedCornerShape(detailPaneCornerRadius))
            .background(palette.sheet),
        propagateMinConstraints = true,
    ) {
        CompositionLocalProvider(LocalTuneScoutColorPalette provides palette.copy(background = palette.sheet)) {
            content()
        }
    }
}
