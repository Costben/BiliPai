package com.android.purebilibili.feature.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.android.purebilibili.R
import com.android.purebilibili.core.util.HapticType
import io.github.suqi8.coui.kmp.basic.BottomFloatingAccessoryNavigationBar
import io.github.suqi8.coui.kmp.basic.BottomFloatingNavigationBar
import io.github.suqi8.coui.kmp.basic.BottomFloatingNavigationBarDefaults
import io.github.suqi8.coui.kmp.basic.BottomFloatingNavigationBarItem
import io.github.suqi8.coui.kmp.basic.BottomFloatingRedDot
import io.github.suqi8.coui.kmp.basic.Icon
import io.github.suqi8.coui.kmp.blur.Backdrop
import io.github.suqi8.coui.kmp.theme.COUITheme

/**
 * COUI 17 底栏的 1 + 3 + 1 排布：首尾两项是融出胶囊的圆形附件，其余条目留在胶囊内。
 * 不足三项时没有中间段可留，退回为单个胶囊。
 */
internal data class CouiAccessoryBottomBarSlots(
    val start: BottomNavItem?,
    val capsule: List<BottomNavItem>,
    val end: BottomNavItem?,
)

internal fun resolveCouiAccessoryBottomBarSlots(
    visibleItems: List<BottomNavItem>,
): CouiAccessoryBottomBarSlots = if (visibleItems.size < 3) {
    CouiAccessoryBottomBarSlots(start = null, capsule = visibleItems, end = null)
} else {
    CouiAccessoryBottomBarSlots(
        start = visibleItems.first(),
        capsule = visibleItems.subList(1, visibleItems.size - 1),
        end = visibleItems.last(),
    )
}

internal fun resolveCouiBottomBarRedDot(item: BottomNavItem, unreadCount: Int): BottomFloatingRedDot? {
    if (!shouldShowBottomBarDynamicReminderBadge(item, unreadCount)) return null
    return if (unreadCount == BOTTOM_BAR_DYNAMIC_BADGE_DOT) {
        BottomFloatingRedDot.Dot
    } else {
        BottomFloatingRedDot.Count(unreadCount)
    }
}

/**
 * ColorOS 17 悬浮底栏：COUI 的附件导航栏常驻显示，玻璃材质采样 [backdrop]。
 * 底部留白按 COUI 的导航模式规则计算，替代系统导航栏 inset。
 */
@Composable
internal fun CouiAccessoryBottomBar(
    currentItem: BottomNavItem,
    onItemClick: (BottomNavItem) -> Unit,
    visibleItems: List<BottomNavItem>,
    itemLabels: Map<String, String>,
    dynamicUnreadCount: Int,
    iconStyle: SharedFloatingBottomBarIconStyle,
    backdrop: Backdrop?,
    haptic: (HapticType) -> Unit,
    modifier: Modifier = Modifier,
    onToggleSidebar: (() -> Unit)? = null,
) {
    val slots = remember(visibleItems) { resolveCouiAccessoryBottomBarSlots(visibleItems) }
    val barModifier = modifier.padding(bottom = BottomFloatingNavigationBarDefaults.navigationBarPadding())
    val tap: (BottomNavItem) -> Unit = { item ->
        performMaterialBottomBarTap(haptic = haptic, onClick = { onItemClick(item) })
    }
    val capsuleContent: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {
        slots.capsule.forEach { item ->
            BottomFloatingNavigationBarItem(
                selected = currentItem == item,
                onClick = { tap(item) },
                icon = resolveSharedBottomBarIcon(item, false, iconStyle),
                selectedIcon = resolveSharedBottomBarIcon(item, true, iconStyle),
                label = resolveBottomNavItemLabel(item, itemLabels),
                redDot = resolveCouiBottomBarRedDot(item, dynamicUnreadCount),
            )
        }
        if (onToggleSidebar != null) {
            BottomFloatingNavigationBarItem(
                selected = false,
                onClick = { performMaterialBottomBarTap(haptic = haptic, onClick = onToggleSidebar) },
                icon = resolveSharedBottomBarSidebarIcon(iconStyle),
                label = stringResource(R.string.sidebar_toggle),
            )
        }
    }
    val start = slots.start
    val end = slots.end
    if (start == null || end == null) {
        BottomFloatingNavigationBar(
            modifier = barModifier,
            backdrop = backdrop,
            content = capsuleContent,
        )
        return
    }
    BottomFloatingAccessoryNavigationBar(
        modifier = barModifier,
        backdrop = backdrop,
        startAccessory = {
            CouiBottomBarAccessory(
                item = start,
                selected = currentItem == start,
                label = resolveBottomNavItemLabel(start, itemLabels),
                iconStyle = iconStyle,
                redDot = resolveCouiBottomBarRedDot(start, dynamicUnreadCount),
                onClick = { tap(start) },
            )
        },
        endAccessory = {
            CouiBottomBarAccessory(
                item = end,
                selected = currentItem == end,
                label = resolveBottomNavItemLabel(end, itemLabels),
                iconStyle = iconStyle,
                redDot = resolveCouiBottomBarRedDot(end, dynamicUnreadCount),
                onClick = { tap(end) },
            )
        },
        content = capsuleContent,
    )
}

/** 圆形附件只放图标；圆形本体由融合球材质绘制，这里不画背景。配色与胶囊条目同一套 COUI 规则。 */
@Composable
private fun BoxScope.CouiBottomBarAccessory(
    item: BottomNavItem,
    selected: Boolean,
    label: String,
    iconStyle: SharedFloatingBottomBarIconStyle,
    redDot: BottomFloatingRedDot?,
    onClick: () -> Unit,
) {
    val colorScheme = COUITheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Tab,
                onClickLabel = label,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box {
            Icon(
                imageVector = resolveSharedBottomBarIcon(item, selected, iconStyle),
                contentDescription = label,
                modifier = Modifier.size(BottomFloatingNavigationBarDefaults.IconSize),
                tint = if (selected) colorScheme.onSurface else colorScheme.labelPrimaryVariant,
            )
            if (redDot != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 2.dp, y = (-2).dp)
                        .size(BottomFloatingNavigationBarDefaults.RedDotDiameter)
                        .background(colorScheme.error, CircleShape),
                )
            }
        }
    }
}
