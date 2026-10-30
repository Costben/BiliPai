package com.android.purebilibili.navigation

import com.android.purebilibili.core.store.HomeSettings
import com.android.purebilibili.core.store.SettingsManager

internal fun resolveBottomBarVisibleForMode(
    mode: SettingsManager.BottomBarVisibilityMode,
    scrollVisible: Boolean,
): Boolean = when (mode) {
    SettingsManager.BottomBarVisibilityMode.ALWAYS_HIDDEN -> false
    SettingsManager.BottomBarVisibilityMode.ALWAYS_VISIBLE -> true
    SettingsManager.BottomBarVisibilityMode.SCROLL_HIDE -> scrollVisible
}

/** 整栏下滑隐藏优先于 Dock 收缩；只调整渲染参数，不改写用户保存值。 */
internal fun resolveBottomBarScrollSettings(
    homeSettings: HomeSettings,
    mode: SettingsManager.BottomBarVisibilityMode,
): HomeSettings = if (mode == SettingsManager.BottomBarVisibilityMode.SCROLL_HIDE) {
    homeSettings.copy(bottomBarScrollCompactEnabled = false, linkedDockMergeOnScrollEnabled = false)
} else {
    homeSettings
}

internal data class AppNavigationAppearance(
    val cardTransitionEnabled: Boolean,
    val bottomBarBlurEnabled: Boolean,
    val bottomBarLabelMode: Int,
    val bottomBarFloating: Boolean
)

internal fun resolveEffectiveNavigationBottomBarBlur(
    homeSettings: HomeSettings,
): Boolean = homeSettings.isBottomBarBlurEnabled

internal fun resolveAppNavigationAppearance(
    homeSettings: HomeSettings,
): AppNavigationAppearance {
    return AppNavigationAppearance(
        cardTransitionEnabled = homeSettings.cardTransitionEnabled,
        bottomBarBlurEnabled = resolveEffectiveNavigationBottomBarBlur(
            homeSettings = homeSettings,
        ),
        bottomBarLabelMode = homeSettings.bottomBarLabelMode,
        bottomBarFloating = homeSettings.isBottomBarFloating
    )
}

/** COUI 17 的悬浮底栏常驻显示：下滑隐藏在渲染时改为始终显示，永久隐藏保持不变，不改写用户保存值。 */
internal fun resolveEffectiveBottomBarVisibilityMode(
    mode: SettingsManager.BottomBarVisibilityMode,
    uiStyle: com.android.purebilibili.core.theme.AppUiStyle,
): SettingsManager.BottomBarVisibilityMode =
    if (uiStyle == com.android.purebilibili.core.theme.AppUiStyle.COUI17 &&
        mode == SettingsManager.BottomBarVisibilityMode.SCROLL_HIDE
    ) {
        SettingsManager.BottomBarVisibilityMode.ALWAYS_VISIBLE
    } else {
        mode
    }
