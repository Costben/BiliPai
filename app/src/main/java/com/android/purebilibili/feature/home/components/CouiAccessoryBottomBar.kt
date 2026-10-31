package com.android.purebilibili.feature.home.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.android.purebilibili.R
import com.android.purebilibili.core.util.HapticType
import androidx.compose.ui.platform.LocalDensity
import io.github.suqi8.coui.kmp.basic.BottomFloatingNavigationBar
import io.github.suqi8.coui.kmp.basic.BottomFloatingNavigationBarDefaults
import io.github.suqi8.coui.kmp.basic.BottomFloatingNavigationBarItem
import io.github.suqi8.coui.kmp.basic.BottomFloatingRedDot
import io.github.suqi8.coui.kmp.blur.Backdrop
import io.github.suqi8.coui.kmp.utils.rememberGalleryDensity

/**
 * 底栏红点：动态页未读数映射为 COUI 的红点/数字角标。
 */
internal fun resolveCouiBottomBarRedDot(item: BottomNavItem, unreadCount: Int): BottomFloatingRedDot? {
    if (!shouldShowBottomBarDynamicReminderBadge(item, unreadCount)) return null
    return if (unreadCount == BOTTOM_BAR_DYNAMIC_BADGE_DOT) {
        BottomFloatingRedDot.Dot
    } else {
        BottomFloatingRedDot.Count(unreadCount)
    }
}

/**
 * ColorOS 17 悬浮底栏：所有条目留在同一个连续胶囊里，不再拆出首尾独立圆球。
 * 玻璃材质采样 [backdrop]，底部留白按 COUI 的导航模式规则计算。
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
    val galleryDensity = rememberGalleryDensity()
    CompositionLocalProvider(LocalDensity provides galleryDensity) {
        BottomFloatingNavigationBar(
            modifier = modifier
                .padding(bottom = BottomFloatingNavigationBarDefaults.MenuToScreenNaviDistance)
                .requiredHeight(BottomFloatingNavigationBarDefaults.BarHeight),
            backdrop = backdrop,
        ) {
            visibleItems.forEach { item ->
                BottomFloatingNavigationBarItem(
                    selected = currentItem == item,
                    onClick = {
                        performMaterialBottomBarTap(haptic = haptic, onClick = { onItemClick(item) })
                    },
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
    }
}
