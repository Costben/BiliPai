package com.android.purebilibili.feature.home.components

import kotlin.math.roundToInt

/** Shared alpha handoff for the overlapping glass navigation and compact button. */
internal fun resolveLinkedDockGlassHandoffProgress(mergeProgress: Float): Float {
    val progress = (mergeProgress / 0.34f).coerceIn(0f, 1f)
    return progress * progress * progress
}

enum class LinkedDockPhase { Expanded, Playback, Compact, Search }

/**
 * 收起态「首页球 + 播放条」的落位。
 *
 * [centered] 时整组在 [width] 里对称留白，右缘与展开态播放条重合：
 * 球 + 条 作为一个整体居中，展开过程中条的右端不动，只由左缘向左铺开。
 *
 * 传入的 [expandedAudioX] 是展开态播放条的**左缘**，必须也是展开态导航胶囊的
 * 可见左缘。这样收起态的球左缘与展开态可见左缘落在同一点：展开时左缘一动不动，
 * 胶囊只从球的右边长开，不会先横挪一段再展开。
 * 否则退回旧布局（贴右缘、播放条接在球右侧）。
 */
internal data class LinkedDockCompactGroup(
    /** COUI 胶囊槽位左缘；可见的球还要再内缩一圈 [capsuleInset]。 */
    val capsuleLeft: Int,
    /** 播放条左缘（= 球可见右缘 + gap）。 */
    val audioLeft: Int,
    /** 播放条右缘；为 null 时交给 [resolveLinkedDockGeometry] 按搜索圆钮退位。 */
    val audioRight: Int?,
)

internal fun resolveLinkedDockCompactGroup(
    width: Int,
    expandedAudioX: Int,
    expandedAudioWidth: Int,
    capsuleInset: Int,
    button: Int,
    gap: Int,
    centered: Boolean,
    fallbackCapsuleLeft: Int = 0,
    /**
     * 收起态球的**可见左缘**（相对容器）。
     * 与 [compactAudioRight] 同时传入时改用「左右贴边」落位：球的左缘和播放条的右缘
     * 到屏幕左右边界的距离恒等，底栏因此比整组居中更宽。
     */
    compactBallLeft: Int? = null,
    /** 收起态播放条右缘（相对容器）；与 [compactBallLeft] 成对使用。 */
    compactAudioRight: Int? = null,
    /**
     * 收起态球与播放条之间的间距。
     * 导航胶囊与播放条此时共用一块融合球表面，间距太小两块轮廓会被表面张力焊成一坨；
     * 默认沿用 [gap]，dock 会传入更大的值让静止时是两颗分开的球。
     */
    compactGap: Int = gap,
): LinkedDockCompactGroup {
    if (compactBallLeft != null && compactAudioRight != null) {
        return LinkedDockCompactGroup(
            capsuleLeft = compactBallLeft - capsuleInset,
            audioLeft = compactBallLeft + button + compactGap,
            audioRight = compactAudioRight.coerceIn(0, width),
        )
    }
    if (!centered) {
        val ballLeft = fallbackCapsuleLeft + capsuleInset
        return LinkedDockCompactGroup(
            capsuleLeft = fallbackCapsuleLeft,
            audioLeft = ballLeft + button + compactGap,
            audioRight = null,
        )
    }
    val audioRight = (expandedAudioX + expandedAudioWidth).coerceIn(0, width)
    val capsuleLeft = (width - capsuleInset - audioRight).coerceAtLeast(0)
    return LinkedDockCompactGroup(
        capsuleLeft = capsuleLeft,
        audioLeft = capsuleLeft + capsuleInset + button + compactGap,
        audioRight = audioRight,
    )
}

/** Search owns the first tap on the compact artwork target. */
internal fun shouldExpandPlaybackFromSearch(
    phase: LinkedDockPhase,
    hasAudio: Boolean,
): Boolean = hasAudio && phase == LinkedDockPhase.Search

/** Accumulate one direction before changing chrome; tiny reversals must not cause flicker. */
internal fun accumulateDockScroll(previous: Float, delta: Float): Float =
    if (previous * delta < 0f) delta else previous + delta

/** Retained playback owns the dock until its exit animation has released the slot. */
internal fun shouldUseScrollCompactNavigation(
    enabled: Boolean,
    hasAudio: Boolean,
    retainingAudio: Boolean,
): Boolean = enabled && !hasAudio && !retainingAudio

/** Positive offset browses down; a full opposite-direction threshold expands again. */
internal fun resolveScrollCompactIntent(accumulatedPx: Float, thresholdPx: Float): Boolean? = when {
    accumulatedPx >= thresholdPx -> true
    accumulatedPx <= -thresholdPx -> false
    else -> null
}

internal fun resolveLinkedDockRestingPhase(
    collapseRequested: Boolean,
    hasAudio: Boolean,
): LinkedDockPhase = when {
    !collapseRequested -> LinkedDockPhase.Expanded
    hasAudio -> LinkedDockPhase.Playback
    // 没有播放条时保持完整导航，不再收成播放条联动的小球。
    else -> LinkedDockPhase.Expanded
}

fun resolveLinkedDockPhaseOnAudioChange(
    currentPhase: LinkedDockPhase,
    hasAudio: Boolean,
): LinkedDockPhase = when {
    !hasAudio && (currentPhase == LinkedDockPhase.Playback || currentPhase == LinkedDockPhase.Compact) ->
        LinkedDockPhase.Expanded
    hasAudio && currentPhase == LinkedDockPhase.Compact -> LinkedDockPhase.Playback
    else -> currentPhase
}

fun resolveLinkedDockInitialPhase(
    currentItem: BottomNavItem,
    collapseRequested: Boolean,
    hasAudio: Boolean,
    savedPhase: LinkedDockPhase? = null,
): LinkedDockPhase {
    if (savedPhase != null) {
        return resolveLinkedDockPhaseOnAudioChange(savedPhase, hasAudio)
    }
    return if (currentItem == BottomNavItem.HOME) {
        LinkedDockPhase.Expanded
    } else {
        resolveLinkedDockRestingPhase(collapseRequested, hasAudio)
    }
}

fun shouldEnableLinkedDockBackHandler(
    phase: LinkedDockPhase,
    isTopLevelDestination: Boolean,
): Boolean = isTopLevelDestination && phase == LinkedDockPhase.Search

fun resolveLinkedDockPhaseOnSearchDismiss(
    hasAudio: Boolean,
    previousPhase: LinkedDockPhase = if (hasAudio) LinkedDockPhase.Playback else LinkedDockPhase.Expanded,
): LinkedDockPhase = resolveLinkedDockPhaseOnAudioChange(previousPhase, hasAudio)

/** 离开搜索展开态（收起/退回其它相位）时清空底栏搜索词，避免下次展开残留。 */
internal fun shouldResetLinkedDockSearchQuery(
    phase: LinkedDockPhase,
): Boolean = phase != LinkedDockPhase.Search

internal data class LinkedDockGeometry(
    val searchWidth: Int,
    val audioWidth: Int,
    val audioX: Int,
    val audioY: Int,
    val top: Int,
    val height: Int,
)

internal fun resolveLinkedDockNavigationX(
    maximumWidth: Int,
    navigationWidth: Int,
    button: Int,
    gap: Int,
    searchEnabled: Boolean,
): Int {
    val safeMaximumWidth = maximumWidth.coerceAtLeast(0)
    val searchReservation = if (searchEnabled) button.coerceAtLeast(0) + gap.coerceAtLeast(0) else 0
    val clusterWidth = (navigationWidth.coerceAtLeast(0) + searchReservation)
        .coerceAtMost(safeMaximumWidth)
    return ((safeMaximumWidth - clusterWidth) / 2).coerceAtLeast(0)
}

internal fun resolveLinkedDockSearchX(
    maximumWidth: Int,
    navigationWidth: Int,
    searchWidth: Int,
    button: Int,
    gap: Int,
    mergeProgress: Float,
    searchProgress: Float,
): Int {
    val startX = resolveLinkedDockNavigationX(
        maximumWidth = maximumWidth,
        navigationWidth = navigationWidth,
        button = button,
        gap = gap,
        searchEnabled = true,
    ) + navigationWidth.coerceAtLeast(0) + gap.coerceAtLeast(0)
    val endX = (maximumWidth - searchWidth).coerceAtLeast(0)
    val expansionProgress = maxOf(mergeProgress, searchProgress).coerceIn(0f, 1f)
    return (startX + (endX - startX) * expansionProgress)
        .roundToInt()
}

internal fun resolveLinkedDockGeometry(
    width: Int,
    button: Int,
    barHeight: Int,
    gap: Int,
    hasAudio: Boolean,
    searchEnabled: Boolean,
    mergeProgress: Float,
    searchProgress: Float,
    verticalGap: Int = gap,
    presenceProgress: Float = 1f,
    /** 展开态小横条的宽度；默认铺满容器，dock 传入底栏整簇宽度以对齐长度。 */
    expandedAudioWidth: Int = width,
    /** 展开态小横条的左缘；dock 传入底栏整簇的起点 X 以对齐两行胶囊。 */
    expandedAudioX: Int = 0,
    /**
     * 收拢态小横条的左缘；默认接在首页球右侧。
     * COUI 原生胶囊左右各带 16dp 内边距，球心因此右移，dock 必须传入球的实际右缘，
     * 否则播放条会压住半个首页球。
     */
    compactAudioLeft: Int = button + gap,
    /**
     * 收拢态小横条的右缘；默认右侧留出搜索圆钮后贴齐容器。
     * 传入展开态小横条的右缘时，收拢 → 展开过程中条的右端完全不动，
     * 只由左缘向左铺开，看起来是"从球那边长出来"而不是整条横移。
     */
    compactAudioRight: Int? = null,
): LinkedDockGeometry {
    val merge = mergeProgress.coerceIn(0f, 1f)
    val search = searchProgress.coerceIn(0f, 1f)
    val presence = presenceProgress.coerceIn(0f, 1f)
    val top = ((if (hasAudio) barHeight + verticalGap else 0) * (1f - merge)).roundToInt()
    val searchWidth = if (!searchEnabled) 0 else (
        button + (width - button * (if (hasAudio) 3 else 2) - gap * (if (hasAudio) 2 else 1)) * search
    ).roundToInt().coerceAtLeast(button).coerceAtMost((width - button).coerceAtLeast(0))
    // Both playback and search retain separate capsule surfaces.
    val playbackGap = gap
    val compactRight = (compactAudioRight
        ?: (width - searchWidth - playbackGap * (if (searchEnabled) 1 else 0)))
        .coerceIn(0, width)
    val compactLeft = compactAudioLeft.coerceIn(0, compactRight)
    val compactAudioWidth = (compactRight - compactLeft).coerceAtLeast(0)
    val expandedAudio = expandedAudioWidth.coerceIn(0, width)
    val targetAudioWidth = if (hasAudio) {
        (expandedAudio + (compactAudioWidth - expandedAudio) * merge).roundToInt()
    } else {
        0
    }
    // Presence 收放以右缘为锚：出现时胶囊从右缘向左生长，消失时向右收起，
    // 避免旧实现的零宽硬切；右缘始终落在完整胶囊的右边界上。
    val audioWidth = (targetAudioWidth * presence).roundToInt()
    val audioX = (expandedAudioX +
        (compactLeft - expandedAudioX) * merge).roundToInt() +
        targetAudioWidth - audioWidth
    return LinkedDockGeometry(
        searchWidth = searchWidth,
        audioWidth = audioWidth,
        audioX = audioX,
        audioY = 0,
        top = top,
        height = top + barHeight,
    )
}
