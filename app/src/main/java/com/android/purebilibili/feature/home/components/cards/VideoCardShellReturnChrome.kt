package com.android.purebilibili.feature.home.components.cards

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * 源卡信息区在开合期间的视觉。
 *
 * **恒等**：列表真卡在开合全程原位满格。开合已经改成「飞行页整页淡入淡出」，卡片是从
 * 它下面透出来的——可见度完全由飞行页那一层的 alpha 决定，这里不需要任何补偿。
 *
 * 保留函数签名只为不改动十几处调用点；此前它每次都会挂一个变换图层，即使算出来
 * 是 alpha=1、translationY=0，也仍然给每张卡多建一个 RenderNode。整条链路改成直通之后，
 * 列表卡就是普通的一次绘制。
 */
@Composable
@Suppress("UNUSED_PARAMETER")
internal fun Modifier.videoCardShellReturnChromeAlpha(
    enabled: Boolean,
    bvid: String,
    sourceRoute: String?,
    isReturningFromDetail: Boolean = false,
    isQuickReturnFromDetail: Boolean = false,
    followShellMotion: Boolean = false,
    resolveSourceOwnershipAtDraw: Boolean = false,
): Modifier = this

/**
 * 来源卡封面在开合期间的可见 alpha：同样**恒等**，理由见 [videoCardShellReturnChromeAlpha]。
 */
@Composable
@Suppress("UNUSED_PARAMETER")
internal fun Modifier.videoCardShellReturnCoverAlpha(
    enabled: Boolean,
    bvid: String,
    sourceRoute: String?,
    isReturningFromDetail: Boolean = false,
): Modifier = this
