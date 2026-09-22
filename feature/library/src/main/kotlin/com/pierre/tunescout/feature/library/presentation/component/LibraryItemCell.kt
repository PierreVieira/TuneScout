package com.pierre.tunescout.feature.library.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.pierre.tunescout.feature.library.R
import com.pierre.tunescout.feature.library.presentation.model.LibraryItemUiModel
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.theme.TuneScoutSpacing
import kotlin.math.abs
import kotlin.math.roundToInt

private val cellCornerShape = RoundedCornerShape(8.dp)
private const val LIST_FRACTION = 0f
private const val GRID_FRACTION = 1f

/**
 * The two widths a library item rests at, in pixels: [list] as a row of the list, [cell] as a cell
 * of the grid. The item interpolates between the two shapes on its own, because the width the grid
 * gives it jumps from one to the other the moment the columns change, before the transition has
 * moved at all.
 *
 * @property list the width of a row, the whole width of the list.
 * @property cell the width of a cell, one column of the grid.
 */
internal data class LibraryItemWidths(
    val list: Int,
    val cell: Int,
)

/**
 * One library item, drawn as a row of the list at a [gridFraction] of 0, as a cell of the grid at 1,
 * and in between as the one becoming the other: the cover grows from the row's thumbnail to the
 * cell's full width while the name and the subtitle move from beside it to under it.
 *
 * The texts fade out and back in through the move. Halfway, a name is neither beside the cover nor
 * under it but over its corner, and the width it is ellipsized to changes every frame; a fade shows
 * neither. [gridFraction] is read when the item is measured, so a frame of the transition lays the
 * item out again without recomposing it.
 *
 * The item is only clipped to its rounded corners at rest. The grid hands it its new width the
 * moment the columns change, while its content is still the shape of the old one, and a clip would
 * cut a row's name at the edge of the cell it is about to become.
 *
 * @param widths the widths the item rests at, in the list and in the grid.
 * @param artworkSize the cover the item asks for, that of the view mode it is going to.
 * @param gridFraction how far the item is from the row (0) to the cell (1).
 * @param isDense whether the cell is one of the narrow ones of the densest grid, where the name is set a
 * size smaller so a word more of it fits under the cover.
 */
@Composable
internal fun LibraryItemCell(
    item: LibraryItemUiModel,
    name: String,
    widths: LibraryItemWidths,
    artworkSize: LibraryArtworkSize,
    gridFraction: () -> Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDownloaded: Boolean = false,
    isDense: Boolean = false,
) {
    val textModifier = Modifier.fadingThrough(gridFraction)
    Layout(
        content = {
            LibraryItemArtwork(item = item, size = artworkSize)
            Text(
                text = name,
                style = if (isDense) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                color = TuneScoutColors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = textModifier,
            )
            LibraryItemSubtitleLine(item = item, isDownloaded = isDownloaded, modifier = textModifier)
        },
        modifier = modifier
            .graphicsLayer {
                shape = cellCornerShape
                clip = isAtRest(gridFraction())
            }.clickable(onClickLabel = stringResource(R.string.library_open_item), onClick = onClick),
        measurePolicy = remember(widths, gridFraction) { LibraryItemCellMeasurePolicy(widths, gridFraction) },
    )
}

private fun isAtRest(gridFraction: Float): Boolean = gridFraction <= LIST_FRACTION || gridFraction >= GRID_FRACTION

/**
 * Fades the texts out and back in through the move: fully drawn at either end, gone halfway.
 *
 * A `graphicsLayer` would do it, but it draws the text through a layer of its own even at rest, and
 * that rasterizes the glyphs differently from the row they used to be. The layer is only taken while
 * the text is translucent, so the list and the grid draw exactly as they did.
 *
 * @return this modifier, drawing the text at the alpha [gridFraction] gives it.
 */
private fun Modifier.fadingThrough(gridFraction: () -> Float): Modifier = drawWithCache {
    val paint = Paint()
    val bounds = Rect(offset = Offset.Zero, size = size)
    onDrawWithContent {
        val alpha = abs(GRID_FRACTION - 2 * gridFraction())
        if (alpha >= GRID_FRACTION) {
            drawContent()
        } else {
            drawTranslucent(paint = paint, bounds = bounds, alpha = alpha)
        }
    }
}

private fun ContentDrawScope.drawTranslucent(
    paint: Paint,
    bounds: Rect,
    alpha: Float,
) {
    paint.alpha = alpha
    drawIntoCanvas { canvas ->
        canvas.saveLayer(bounds, paint)
        drawContent()
        canvas.restore()
    }
}

/**
 * The row's geometry at a fraction of 0, the cell's at 1, and every size and position in between
 * taken proportionally, so the cover, the name and the subtitle move together at whatever pace
 * [gridFraction] is driven at. The row is the one `LibraryItemRow` draws: the cover at its start,
 * the texts centered beside it, a small padding above and below. The cell stacks the cover, the name
 * and the subtitle with a small gap between each and under the last.
 *
 * The row is laid out at [widths]' list width and the cell at its cell width, whatever width the
 * grid is constraining the item to. At rest the two agree, and the cell takes the width of its own
 * column, which may be a pixel narrower than the first.
 *
 * @property widths the widths the item rests at, in the list and in the grid.
 * @property gridFraction how far the item is from the row (0) to the cell (1), read on every measure.
 */
private class LibraryItemCellMeasurePolicy(
    private val widths: LibraryItemWidths,
    private val gridFraction: () -> Float,
) : MeasurePolicy {
    private val rowArtworkSize = 56.dp
    private val rowArtworkGap = TuneScoutSpacing.medium
    private val rowVerticalPadding = TuneScoutSpacing.small
    private val rowTextGap = TuneScoutSpacing.extraSmall
    private val cellGap = TuneScoutSpacing.small

    override fun MeasureScope.measure(
        measurables: List<Measurable>,
        constraints: Constraints,
    ): MeasureResult {
        val fraction = gridFraction().coerceIn(LIST_FRACTION, GRID_FRACTION)
        val rowWidth = if (constraints.hasBoundedWidth) maxOf(widths.list, constraints.maxWidth) else widths.list
        val cellWidth = minOf(widths.cell, constraints.maxWidth)
        val rowArtwork = rowArtworkSize.roundToPx()
        val rowGap = rowArtworkGap.roundToPx()
        val gap = cellGap.roundToPx()
        val (artwork, name, subtitle) = measurables
        val artworkSize = lerp(rowArtwork, cellWidth, fraction)
        val artworkPlaceable = artwork.measure(Constraints.fixed(artworkSize, artworkSize))
        val textWidth = lerp((rowWidth - rowArtwork - rowGap).coerceAtLeast(0), cellWidth, fraction)
        val textConstraints = Constraints(maxWidth = textWidth)
        val namePlaceable = name.measure(textConstraints)
        val subtitlePlaceable = subtitle.measure(textConstraints)
        val textGap = lerp(rowTextGap.roundToPx(), gap, fraction)
        val textHeight = namePlaceable.height + textGap + subtitlePlaceable.height
        val rowHeight = maxOf(rowArtwork, textHeight) + rowVerticalPadding.roundToPx() * 2
        val cellHeight = cellWidth + gap + textHeight + gap
        val textX = lerp(rowArtwork + rowGap, 0, fraction)
        val textY = lerp(centeredOffsetOf(rowHeight, textHeight), cellWidth + gap, fraction)
        val width = constraints.constrainWidth(lerp(rowWidth, cellWidth, fraction))
        return layout(width, lerp(rowHeight, cellHeight, fraction)) {
            artworkPlaceable.placeRelative(x = 0, y = lerp(centeredOffsetOf(rowHeight, rowArtwork), 0, fraction))
            namePlaceable.placeRelative(x = textX, y = textY)
            subtitlePlaceable.placeRelative(x = textX, y = textY + namePlaceable.height + textGap)
        }
    }

    /**
     * Rounded the way `Alignment.CenterVertically` rounds, so the row lands on the pixel the `Row`
     * it replaces put it on.
     *
     * @return the offset that centers something [size] tall in [space].
     */
    private fun centeredOffsetOf(
        space: Int,
        size: Int,
    ): Int = (HALF * (space - size)).roundToInt()

    private companion object {
        const val HALF = 0.5f
    }
}
