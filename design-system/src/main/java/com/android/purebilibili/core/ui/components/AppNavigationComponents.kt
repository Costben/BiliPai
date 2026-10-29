package com.android.purebilibili.core.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import io.github.suqi8.coui.kmp.basic.Badge as CouiBadge
import io.github.suqi8.coui.kmp.basic.BadgeDefaults as CouiBadgeDefaults
import io.github.suqi8.coui.kmp.basic.NavigationBar as CouiNavigationBar
import io.github.suqi8.coui.kmp.basic.NavigationBarDefaults as CouiNavigationBarDefaults
import io.github.suqi8.coui.kmp.basic.NavigationBarDisplayMode as CouiNavigationBarDisplayMode
import io.github.suqi8.coui.kmp.basic.NavigationBarItem as CouiNavigationBarItem
import io.github.suqi8.coui.kmp.basic.NavigationRail as CouiNavigationRail
import io.github.suqi8.coui.kmp.basic.NavigationRailDefaults as CouiNavigationRailDefaults
import io.github.suqi8.coui.kmp.basic.NavigationRailItem as CouiNavigationRailItem
import io.github.suqi8.coui.kmp.basic.NavigationRailDisplayMode as CouiNavigationRailDisplayMode
import io.github.suqi8.coui.kmp.theme.COUITheme

@Composable
fun AppNavigationBar(
    modifier: Modifier = Modifier,
    containerColor: Color = NavigationBarDefaults.containerColor,
    contentColor: Color = contentColorFor(containerColor),
    tonalElevation: Dp = NavigationBarDefaults.Elevation,
    windowInsets: WindowInsets = NavigationBarDefaults.windowInsets,
    content: @Composable RowScope.() -> Unit,
) = NavigationBar(
    modifier = modifier.focusGroup(),
    containerColor = containerColor,
    contentColor = contentColor,
    tonalElevation = tonalElevation,
    windowInsets = windowInsets,
    content = content,
)

@Composable
fun RowScope.AppNavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: (@Composable (() -> Unit))? = null,
    alwaysShowLabel: Boolean = true,
    colors: NavigationBarItemColors = NavigationBarItemDefaults.colors(),
    interactionSource: MutableInteractionSource? = null,
) {
    val resolvedInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = icon,
        modifier = modifier.appDesktopInteractionVisuals(resolvedInteractionSource, enabled),
        enabled = enabled,
        label = label,
        alwaysShowLabel = alwaysShowLabel,
        colors = colors,
        interactionSource = resolvedInteractionSource,
    )
}

enum class AppPlatformNavigationBarDisplayMode {
    ICON_AND_TEXT,
    ICON_ONLY,
    ICON_WITH_SELECTED_LABEL,
}

private fun AppPlatformNavigationBarDisplayMode.toCouiDisplayMode(): CouiNavigationBarDisplayMode =
    when (this) {
        AppPlatformNavigationBarDisplayMode.ICON_AND_TEXT -> CouiNavigationBarDisplayMode.IconAndText
        AppPlatformNavigationBarDisplayMode.ICON_ONLY -> CouiNavigationBarDisplayMode.IconOnly
        AppPlatformNavigationBarDisplayMode.ICON_WITH_SELECTED_LABEL ->
            CouiNavigationBarDisplayMode.IconWithSelectedLabel
    }

@Composable
fun AppPlatformNavigationBar(
    modifier: Modifier = Modifier,
    color: Color = COUITheme.colorScheme.surface,
    showDivider: Boolean = true,
    defaultWindowInsetsPadding: Boolean = true,
    mode: AppPlatformNavigationBarDisplayMode = AppPlatformNavigationBarDisplayMode.ICON_AND_TEXT,
    content: @Composable RowScope.() -> Unit,
) = CouiNavigationBar(
    modifier = modifier.focusGroup(),
    color = color,
    showDivider = showDivider,
    defaultWindowInsetsPadding = defaultWindowInsetsPadding,
    mode = mode.toCouiDisplayMode(),
    content = content,
)

@Composable
fun RowScope.AppPlatformNavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: Any? = null,
    badge: (@Composable () -> Unit)? = null,
) = CouiNavigationBarItem(
    selected = selected,
    onClick = onClick,
    icon = icon,
    label = label,
    modifier = modifier
        .appDesktopFocusableItemVisuals(enabled)
        .semantics {
            role = Role.Tab
            this.selected = selected
        },
    enabled = enabled,
)

@Composable
fun AppPlatformNavigationBadge(
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    count: Int = 0,
    isDot: Boolean = true,
    content: (@Composable RowScope.() -> Unit)? = null,
) {
    if (content != null) {
        androidx.compose.foundation.layout.Row(modifier = modifier) {
            content()
        }
    } else {
        CouiBadge(
            modifier = modifier,
            count = if (isDot) 0 else count,
        )
    }
}

@Composable
fun AppPlatformNavigationRail(
    expanded: Boolean,
    initiallyExpanded: Boolean = expanded,
    onExpandedChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
    color: Color = COUITheme.colorScheme.surface,
    showDivider: Boolean = true,
    minWidth: Dp = CouiNavigationRailDefaults.MinWidth,
    content: @Composable ColumnScope.() -> Unit,
) {
    CouiNavigationRail(
        modifier = modifier.focusGroup(),
        color = color,
        showDivider = showDivider,
        minWidth = minWidth,
        content = content,
    )
}

@Composable
fun ColumnScope.AppPlatformNavigationRailItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) = CouiNavigationRailItem(
    selected = selected,
    onClick = onClick,
    icon = icon,
    label = label,
    modifier = modifier
        .appDesktopFocusableItemVisuals(enabled)
        .semantics {
            role = Role.Tab
            this.selected = selected
        },
    enabled = enabled,
)
