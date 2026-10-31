package com.android.purebilibili.feature.home.components

import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import com.android.purebilibili.core.ui.motion.AppMotionEasing
import com.android.purebilibili.core.ui.motion.iosMorphTween

internal fun <T> bottomBarDockWidthMotionSpec(): TweenSpec<T> = iosMorphTween(260)

internal fun <T> bottomBarChromeHeightMotionSpec(): TweenSpec<T> = iosMorphTween(220)

internal fun <T> bottomBarSearchGapMotionSpec(): TweenSpec<T> = iosMorphTween(240)

internal fun <T> bottomBarSearchMorphSpring(): SpringSpec<T> = spring(
    dampingRatio = 0.8f,
    stiffness = 380f,
)

/**
 * COUI 17 整栏收拢成首页球的融合球弹簧。
 *
 * 对齐 COUI 原生 melt ball 的 response 0.55 / bounce 0.35 手感（略软一点），
 * 让"缩小 → 融进球"走完整段轨迹，而不是在交叉淡出窗口里一闪而过。
 * stiffness 从 240 降到 150：约 0.62s 走完，肉眼能看清胶囊一路收成正圆。
 */
internal fun <T> bottomBarDockMeltSpring(): SpringSpec<T> = spring(
    dampingRatio = 0.88f,
    stiffness = 150f,
)

internal fun <T> bottomBarContentVisibilityMotionSpec(): TweenSpec<T> = tween(
    durationMillis = 180,
    easing = AppMotionEasing.M1Standard,
)

internal fun bottomBarClickPulseMotionSpec(): SpringSpec<Float> = spring(
    dampingRatio = 0.55f,
    stiffness = 700f,
    visibilityThreshold = 0.0001f,
)

internal fun <T> bottomBarTapReleaseMotionSpec(): TweenSpec<T> = tween(
    durationMillis = 240,
    easing = AppMotionEasing.M1Standard,
)

internal fun <T> bottomBarSearchHoldMotionSpec(): SpringSpec<T> = spring(
    dampingRatio = 0.62f,
    stiffness = 560f,
)
