package com.android.purebilibili.feature.home.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.ui.AppSpacingTokens

/** 宿主给悬浮 dock 留的底部间距，与浮动底栏既有的下边距规范一致。 */
private val DockHostBottomOffset = AppSpacingTokens.ExtraLarge

/** dock 容器自身的上下内边距，胶囊本体之外仍占位。 */
private val DockContainerVerticalPadding = AppSpacingTokens.Small

/** 最后一条内容与 dock 之间要留出的呼吸间距。 */
private val DockContentGap = AppSpacingTokens.Small

/**
 * 悬浮 dock 在屏幕底部实际占据的高度：导航栏 + 宿主下边距 + 容器内边距 + 胶囊 + 内容间距。
 * 合计与宿主 `rememberAppBottomBarContentPadding` 的 floating 规格等值（96dp + 导航栏），
 * 底栏在滑动中隐藏时也必须按这个高度预留，否则 dock 复现就会压住列表末尾。
 */
internal fun resolveFloatingDockBottomExtent(navigationBarsBottom: Dp): Dp {
    val safeNavigationBarsBottom = navigationBarsBottom.coerceAtLeast(0.dp)
    return safeNavigationBarsBottom +
        DockHostBottomOffset +
        DockContainerVerticalPadding +
        resolveBiliPaiBottomBarDockHeight(searchExpanded = false) +
        DockContentGap
}

/**
 * 列表底部留白 = max(宿主已提供的留白, dock 实际占位)。
 *
 * 宿主留白随 dock 显隐收缩（下滑隐藏模式与侧栏导航），直接跟随会让列表末条在 dock
 * 复现时被盖住，因此这里以 dock 的固定占位作为下限；dock 不会出现的页面（侧栏导航、
 * 常隐模式）继续使用宿主留白。
 */
internal fun resolveDockAwareBottomContentPadding(
    shellPadding: Dp,
    navigationBarsBottom: Dp,
    reserveDock: Boolean,
): Dp {
    if (!reserveDock) return shellPadding
    return maxOf(shellPadding, resolveFloatingDockBottomExtent(navigationBarsBottom))
}

@Composable
internal fun rememberDockAwareBottomContentPadding(
    shellPadding: Dp,
    reserveDock: Boolean,
): Dp {
    val navigationBarsBottom = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()
    return resolveDockAwareBottomContentPadding(
        shellPadding = shellPadding,
        navigationBarsBottom = navigationBarsBottom,
        reserveDock = reserveDock,
    )
}
