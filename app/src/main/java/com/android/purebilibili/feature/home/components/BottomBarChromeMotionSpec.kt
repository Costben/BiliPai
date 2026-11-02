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
 * COUI 17 整栏收拢成首页球的弹簧。
 *
 * 原先是 stiffness 150（约 0.62s）：起步 100ms 只走三成，加上横条宽度变化很小，
 * 展开/收起会被读成"停在中间一帧再滑上去"。收窄到约 0.3s 后，
 * 胶囊和播放条在同一段连续形变里完成，不再有拖尾。
 */
internal fun <T> bottomBarDockMeltSpring(): SpringSpec<T> = spring(
    dampingRatio = 0.86f,
    stiffness = 320f,
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
