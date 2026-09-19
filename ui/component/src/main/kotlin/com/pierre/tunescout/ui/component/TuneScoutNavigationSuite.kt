package com.pierre.tunescout.ui.component

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.pierre.tunescout.ui.theme.TuneScoutColors
import com.pierre.tunescout.ui.utils.window.TuneScoutWindowSize

data class TuneScoutNavigationItem(
    val icon: ImageVector,
    val label: String,
    val isSelected: Boolean,
    val onClick: () -> Unit,
)

/**
 * A bar in an upright phone, a rail as soon as the window has width to spare, and nothing at all on
 * the screens that are not tabs.
 *
 * Hiding is [NavigationSuiteType.None] rather than a branch around the scaffold: swapping the
 * composable that wraps the content would rebuild the `NavDisplay` inside it, and the back stack
 * would go with it.
 *
 * The type is passed explicitly because the Material default puts a bar back on a compact height,
 * which is exactly the phone turned sideways this is meant to give a rail.
 */
@Composable
fun TuneScoutNavigationSuite(
    items: List<TuneScoutNavigationItem>,
    isVisible: Boolean,
    windowSize: TuneScoutWindowSize,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val navigationSuiteType = getNavigationSuiteType(isVisible = isVisible, windowSize = windowSize)
    NavigationSuiteScaffold(
        navigationItems = {
            items.forEach { navigationItem ->
                NavigationSuiteItem(
                    selected = navigationItem.isSelected,
                    onClick = navigationItem.onClick,
                    icon = {
                        Icon(
                            imageVector = navigationItem.icon,
                            contentDescription = null,
                        )
                    },
                    label = { Text(text = navigationItem.label) },
                    navigationSuiteType = navigationSuiteType,
                )
            }
        },
        modifier = modifier,
        navigationSuiteType = navigationSuiteType,
        navigationSuiteColors = NavigationSuiteDefaults.colors(
            shortNavigationBarContainerColor = TuneScoutColors.background,
            shortNavigationBarContentColor = TuneScoutColors.textPrimary,
            navigationBarContainerColor = TuneScoutColors.background,
            navigationBarContentColor = TuneScoutColors.textPrimary,
            navigationRailContainerColor = TuneScoutColors.background,
            navigationRailContentColor = TuneScoutColors.textPrimary,
        ),
        containerColor = TuneScoutColors.background,
        contentColor = TuneScoutColors.textPrimary,
        content = content,
    )
}

private fun getNavigationSuiteType(
    isVisible: Boolean,
    windowSize: TuneScoutWindowSize,
): NavigationSuiteType = when {
    !isVisible -> NavigationSuiteType.None
    windowSize.hasNavigationRail -> NavigationSuiteType.NavigationRail
    else -> NavigationSuiteType.NavigationBar
}
