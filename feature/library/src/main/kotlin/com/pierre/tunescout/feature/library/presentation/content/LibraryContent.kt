package com.pierre.tunescout.feature.library.presentation.content

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pierre.tunescout.feature.library.R
import com.pierre.tunescout.feature.library.domain.model.LibraryGridColumns
import com.pierre.tunescout.feature.library.domain.model.LibraryViewMode
import com.pierre.tunescout.feature.library.presentation.component.LibraryArtworkSize
import com.pierre.tunescout.feature.library.presentation.component.LibraryFilterChipRow
import com.pierre.tunescout.feature.library.presentation.component.LibraryGridColumnsButton
import com.pierre.tunescout.feature.library.presentation.component.LibraryItemCell
import com.pierre.tunescout.feature.library.presentation.component.LibraryItemWidths
import com.pierre.tunescout.feature.library.presentation.component.LibraryViewModeToggle
import com.pierre.tunescout.feature.library.presentation.model.LibraryItemUiModel
import com.pierre.tunescout.feature.library.presentation.model.LibraryUiEvent
import com.pierre.tunescout.feature.library.presentation.model.LibraryUiState
import com.pierre.tunescout.feature.library.presentation.model.getName
import com.pierre.tunescout.ui.component.StateMessage
import com.pierre.tunescout.ui.component.TopBarAction
import com.pierre.tunescout.ui.component.TuneScoutIcons
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.theme.TuneScoutSpacing
import com.pierre.tunescout.ui.utils.scroll.hideableTopBar
import com.pierre.tunescout.ui.utils.scroll.hidesBarsOnScroll
import com.pierre.tunescout.ui.utils.semantics.screenPane

private val gridColumnSpacing = TuneScoutSpacing.medium
private val fabSize = 56.dp
private val fabListBottomPadding = fabSize + TuneScoutSpacing.screen * 2
private const val LIST_COLUMNS = 1
private const val LIST_FRACTION = 0f
private const val GRID_FRACTION = 1f

/**
 * The create-playlist action is a FAB on a single pane, where the list has the whole width to spare.
 * On a wide window the library sits in a narrower list pane beside a detail, so the action moves into
 * the top bar next to search instead of floating over the list.
 *
 * @param isTwoPane whether the window lays a pane beside the tabs, narrowing the library's own pane.
 */
@Composable
fun LibraryContent(
    uiState: LibraryUiState,
    isTwoPane: Boolean,
    onEvent: (LibraryUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .screenPane(stringResource(R.string.library_title))
            .safeDrawingPadding()
            .hidesBarsOnScroll(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
        ) {
            Header(
                uiState = uiState,
                isTwoPane = isTwoPane,
                onEvent = onEvent,
                modifier = Modifier.hideableTopBar(),
            )
            Box(
                modifier = Modifier.padding(horizontal = TuneScoutSpacing.screen),
                contentAlignment = Alignment.TopCenter,
            ) {
                when {
                    uiState.isDownloadedEmpty -> StateMessage(
                        title = stringResource(R.string.library_downloaded_empty_title),
                        description = stringResource(R.string.library_downloaded_empty_description),
                        isAnnounced = true,
                    )

                    uiState.isAlbumsEmpty -> StateMessage(
                        title = stringResource(R.string.library_albums_empty_title),
                        description = stringResource(R.string.library_albums_empty_description),
                        isAnnounced = true,
                    )

                    else -> LibraryItemGrid(uiState = uiState, isTwoPane = isTwoPane, onEvent = onEvent)
                }
            }
        }
        if (!isTwoPane) {
            CreatePlaylistButton(
                onClick = { onEvent(LibraryUiEvent.OnCreatePlaylistClicked) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(TuneScoutSpacing.screen),
            )
        }
    }
}

@Composable
private fun CreatePlaylistButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = TuneScoutColors.accentContainer,
        contentColor = TuneScoutColors.textPrimary,
        modifier = modifier,
    ) {
        Icon(
            imageVector = TuneScoutIcons.add,
            contentDescription = stringResource(R.string.library_create_playlist),
        )
    }
}

@Composable
private fun Header(
    uiState: LibraryUiState,
    isTwoPane: Boolean,
    onEvent: (LibraryUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        TitleRow(isTwoPane = isTwoPane, onEvent = onEvent)
        LibraryFilterChipRow(
            filters = uiState.visibleFilters,
            selected = uiState.filters,
            onFilterClick = { filter -> onEvent(LibraryUiEvent.OnFilterClicked(filter)) },
            onClearClick = { onEvent(LibraryUiEvent.OnClearFiltersClicked) },
            horizontalPadding = TuneScoutSpacing.large,
            modifier = Modifier.padding(vertical = TuneScoutSpacing.small),
        )
        SectionBar(viewMode = uiState.viewMode, gridColumns = uiState.gridColumns, onEvent = onEvent)
    }
}

@Composable
private fun TitleRow(
    isTwoPane: Boolean,
    onEvent: (LibraryUiEvent) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = TuneScoutSpacing.screen, start = TuneScoutSpacing.large, end = TuneScoutSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.library_title),
            style = MaterialTheme.typography.headlineMedium,
            color = TuneScoutColors.textPrimary,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
        )
        if (isTwoPane) {
            TopBarAction(
                icon = TuneScoutIcons.add,
                contentDescription = stringResource(R.string.library_create_playlist),
                onClick = { onEvent(LibraryUiEvent.OnCreatePlaylistClicked) },
            )
        }
        TopBarAction(
            icon = TuneScoutIcons.search,
            contentDescription = stringResource(R.string.library_open_search),
            onClick = { onEvent(LibraryUiEvent.OnSearchClicked) },
        )
    }
}

@Composable
private fun SectionBar(
    viewMode: LibraryViewMode,
    gridColumns: LibraryGridColumns,
    onEvent: (LibraryUiEvent) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = TuneScoutSpacing.large),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.library_recents),
            style = MaterialTheme.typography.bodySmall,
            color = TuneScoutColors.textSecondary,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
        )
        GridColumnsAction(
            isVisible = viewMode == LibraryViewMode.GRID,
            columns = gridColumns,
            onClick = { onEvent(LibraryUiEvent.OnGridColumnsClicked) },
        )
        LibraryViewModeToggle(
            viewMode = viewMode,
            onViewModeClick = { mode -> onEvent(LibraryUiEvent.OnViewModeSelected(mode)) },
        )
    }
}

/**
 * The size of the grid is only offered while there is a grid: a list has one column and nothing to
 * size. The button slides out from beside the toggle as the grid is picked and back behind it as the
 * list is, so the list's bar stays as bare as it was.
 */
@Composable
private fun RowScope.GridColumnsAction(
    isVisible: Boolean,
    columns: LibraryGridColumns,
    onClick: () -> Unit,
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + expandHorizontally(),
        exit = fadeOut() + shrinkHorizontally(),
    ) {
        LibraryGridColumnsButton(columns = columns, onClick = onClick)
    }
}

/**
 * The list and the grid are one grid, of one column or of as many as the user picked, so a change of view mode
 * keeps every item in the composition and moves it instead of replacing it: each cell morphs between
 * its row and its cell shape at the pace of [rememberGridFraction] while `animateItem` slides it to
 * its new place, and the scroll position carries over.
 */
@Composable
private fun LibraryItemGrid(
    uiState: LibraryUiState,
    isTwoPane: Boolean,
    onEvent: (LibraryUiEvent) -> Unit,
) {
    val gridFraction = rememberGridFraction(uiState.viewMode)
    val artworkSize = LibraryArtworkSize.of(uiState.viewMode)
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val columns = remember(uiState.viewMode, uiState.gridColumns) {
            columnsOf(viewMode = uiState.viewMode, gridColumns = uiState.gridColumns)
        }
        val widths = rememberItemWidths(availableWidth = maxWidth, gridColumns = uiState.gridColumns)
        LazyVerticalGrid(
            columns = columns,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = TuneScoutSpacing.small,
                bottom = if (isTwoPane) TuneScoutSpacing.extraLarge else fabListBottomPadding,
            ),
            horizontalArrangement = Arrangement.spacedBy(gridColumnSpacing),
            verticalArrangement = remember(gridFraction) { LineSpacingArrangement(gridFraction) },
        ) {
            items(
                items = uiState.filteredItems,
                key = { item -> item.key.toString() },
            ) { item ->
                LibraryItemCell(
                    item = item,
                    name = libraryItemName(item),
                    widths = widths,
                    artworkSize = artworkSize,
                    gridFraction = gridFraction,
                    onClick = { onEvent(LibraryUiEvent.OnItemClicked(item)) },
                    modifier = Modifier.animateItem(),
                    isDownloaded = uiState.isDownloaded(item),
                )
            }
        }
    }
}

/**
 * The widths an item rests at, from the width the grid has: the whole of it as a row, and as a
 * cell the first of the [gridColumns] the grid cuts it into, the way the grid itself cuts it.
 *
 * @return the widths for every item of a grid [availableWidth] wide.
 */
@Composable
private fun rememberItemWidths(
    availableWidth: Dp,
    gridColumns: LibraryGridColumns,
): LibraryItemWidths {
    val density = LocalDensity.current
    return remember(availableWidth, gridColumns, density) {
        val listWidth = with(density) { availableWidth.roundToPx() }
        val spacing = with(density) { gridColumnSpacing.roundToPx() }
        val cellWidths = with(GridCells.Fixed(gridColumns.count)) {
            density.calculateCrossAxisCellSizes(listWidth, spacing)
        }
        LibraryItemWidths(list = listWidth, cell = cellWidths.first())
    }
}

/**
 * How far the items are from the list to the grid, as a read for a measure pass rather than a value:
 * every cell and the space between the lines follow it on each frame, and a value read in the
 * composition would recompose them all on each frame instead.
 *
 * @return a read of the fraction, 0 in the list, 1 in the grid, moving between the two on a change.
 */
@Composable
private fun rememberGridFraction(viewMode: LibraryViewMode): () -> Float {
    val fraction: State<Float> = animateFloatAsState(
        targetValue = if (viewMode == LibraryViewMode.GRID) GRID_FRACTION else LIST_FRACTION,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "libraryGridFraction",
    )
    return remember(fraction) { { fraction.value } }
}

private fun columnsOf(
    viewMode: LibraryViewMode,
    gridColumns: LibraryGridColumns,
): GridCells = when (viewMode) {
    LibraryViewMode.LIST -> GridCells.Fixed(LIST_COLUMNS)
    LibraryViewMode.GRID -> GridCells.Fixed(gridColumns.count)
}

/**
 * The gap between the grid's lines, closed in the list, where each row carries its own padding and
 * a gap on top of it would space them apart. The grid reads [spacing] as it measures, so the gap
 * follows [gridFraction] with the cells, frame by frame, without the grid being recomposed.
 *
 * @property gridFraction how far the items are from the list (0) to the grid (1).
 */
private class LineSpacingArrangement(
    private val gridFraction: () -> Float,
) : Arrangement.Vertical {
    private val gridLineSpacing = TuneScoutSpacing.medium

    override val spacing: Dp
        get() = gridLineSpacing * gridFraction()

    override fun Density.arrange(
        totalSize: Int,
        sizes: IntArray,
        outPositions: IntArray,
    ) {
        val gap = spacing.roundToPx()
        var position = 0
        sizes.forEachIndexed { index, size ->
            outPositions[index] = position
            position += size + gap
        }
    }
}

@Composable
private fun libraryItemName(item: LibraryItemUiModel): String = item.getName(
    favoritesName = stringResource(R.string.library_favorites),
    downloadedSongsName = stringResource(R.string.library_downloaded_songs),
)
