package com.android.purebilibili.feature.home.components.cards

import com.android.purebilibili.core.ui.transition.VideoCardTransitionBackgroundPhase
import com.android.purebilibili.core.ui.transition.normalizeSharedElementSourceRoute
import com.android.purebilibili.core.ui.transition.resolveVideoCardReturnListCoverContract

internal data class VideoCardScrollLiteVisualPolicy(
    val coverShadowElevationDp: Float,
    val showCoverGradientMask: Boolean,
    val showHistoryProgressBar: Boolean,
    val showCompactStatsOnCover: Boolean,
    val showSecondaryStatsRow: Boolean
)

internal fun resolveVideoCardScrollLiteVisualPolicy(
    scrollLiteModeEnabled: Boolean,
    compactStatsOnCover: Boolean
): VideoCardScrollLiteVisualPolicy {
    if (scrollLiteModeEnabled) {
        return VideoCardScrollLiteVisualPolicy(
            coverShadowElevationDp = 0f,
            showCoverGradientMask = compactStatsOnCover,
            showHistoryProgressBar = false,
            showCompactStatsOnCover = compactStatsOnCover,
            showSecondaryStatsRow = !compactStatsOnCover
        )
    }

    return VideoCardScrollLiteVisualPolicy(
        coverShadowElevationDp = 0f,
        // 仅贴封面统计需要暗渐变；信息移到封面外时保持原图亮度。
        showCoverGradientMask = compactStatsOnCover,
        showHistoryProgressBar = true,
        showCompactStatsOnCover = compactStatsOnCover,
        showSecondaryStatsRow = !compactStatsOnCover
    )
}

/**
 * 列表封面是否允许 Coil crossfade。
 * 契约收口到 [resolveVideoCardReturnListCoverContract]（[VideoCardReturnTimeline]）。
 */
internal fun shouldEnableVideoCardCoverCrossfade(
    isScrollInProgress: Boolean,
    isReturningFromDetail: Boolean,
    useCoverSharedBounds: Boolean,
    isSharedReturnTarget: Boolean
): Boolean = resolveVideoCardReturnListCoverContract(
    isSharedReturnTarget = isSharedReturnTarget,
    isScrollInProgress = isScrollInProgress,
    isReturningFromDetail = isReturningFromDetail,
    useCoverSharedBounds = useCoverSharedBounds,
).enableCoilCrossfade

/**
 * shared 返回目标卡是否应钉住封面 URL/缓存键。
 * 契约收口到 [resolveVideoCardReturnListCoverContract]。
 */
internal fun shouldPinVideoCardCoverForSharedReturn(
    isSharedReturnTarget: Boolean,
): Boolean = resolveVideoCardReturnListCoverContract(
    isSharedReturnTarget = isSharedReturnTarget,
    isScrollInProgress = false,
    isReturningFromDetail = false,
    useCoverSharedBounds = true,
).pinCoverSource

/**
 * Miuix predictive return may enter its settle while the legacy clock still reports HELD.
 * A moving shared-bounds target in any non-opening phase is therefore also a return context.
 */
internal fun isVideoCardFlyingReturnContext(
    isReturningFromDetail: Boolean,
    isVideoCardReturnGestureInProgress: Boolean,
    transitionBackgroundPhase: VideoCardTransitionBackgroundPhase,
    isSharedTransitionActive: Boolean,
    transitionBackgroundProgress: Float,
): Boolean {
    val nonOpeningMorphActive =
        transitionBackgroundPhase != VideoCardTransitionBackgroundPhase.OPENING &&
            (isSharedTransitionActive ||
                transitionBackgroundProgress.coerceIn(0f, 1f) < 0.999f)
    return isReturningFromDetail ||
        isVideoCardReturnGestureInProgress ||
        transitionBackgroundPhase == VideoCardTransitionBackgroundPhase.RETURNING ||
        nonOpeningMorphActive
}

/**
 * 列表真卡**封面**在 Miuix 飞行 entry 期间的可见 alpha。
 *
 * 恒为 **1**：真卡封面在开合全程原位在场。
 *
 * 覆盖它的有两层，各自负责自己的透明度：
 * - 飞行 entry 的实时画面（不透明）—— 落位前一直压在封面槽上；
 * - 飞行 entry 的整页底板 —— 按
 *   [com.android.purebilibili.core.ui.transition.resolveVideoCardDetailContentAlpha] 淡出。
 *
 * 这里再叠一层 alpha 只会让「卡片从页面下面透出来」变成两层淡出相乘，
 * 中段整张卡接近不可见，落位瞬间才补回来。
 */
@Suppress("UNUSED_PARAMETER")
internal fun resolveHomeCardStationaryRevealAlpha(
    isReturnContext: Boolean,
    preferWholeCardReturn: Boolean,
    transitionBackgroundPhase: VideoCardTransitionBackgroundPhase,
    isVideoCardReturnGestureInProgress: Boolean,
    isSharedTransitionActive: Boolean,
    transitionBackgroundProgress: Float,
): Float = 1f

/**
 * 来源卡封面在开合期间的可见 alpha。
 *
 * 与 [resolveHomeCardStationaryRevealAlpha] 同一契约：真卡封面原位满格，
 * 可见度完全由飞行 entry 的实时画面与整页底板决定。
 */
internal fun resolveHomeCardReturnSourceVisualAlpha(
    useCardContainerSharedBounds: Boolean,
    isSharedMorphSourceCard: Boolean,
    isReturningFromDetail: Boolean,
    transitionBackgroundPhase: VideoCardTransitionBackgroundPhase,
    isVideoCardReturnGestureInProgress: Boolean,
    isSharedTransitionActive: Boolean = false,
    transitionBackgroundProgress: Float,
    preferWholeCardReturn: Boolean = false,
): Float {
    if (!useCardContainerSharedBounds || !isSharedMorphSourceCard) return 1f
    val isReturnContext = isVideoCardFlyingReturnContext(
        isReturningFromDetail = isReturningFromDetail,
        isVideoCardReturnGestureInProgress = isVideoCardReturnGestureInProgress,
        transitionBackgroundPhase = transitionBackgroundPhase,
        isSharedTransitionActive = isSharedTransitionActive,
        transitionBackgroundProgress = transitionBackgroundProgress,
    )
    return resolveHomeCardStationaryRevealAlpha(
        isReturnContext = isReturnContext,
        preferWholeCardReturn = preferWholeCardReturn,
        transitionBackgroundPhase = transitionBackgroundPhase,
        isVideoCardReturnGestureInProgress = isVideoCardReturnGestureInProgress,
        isSharedTransitionActive = isSharedTransitionActive,
        transitionBackgroundProgress = transitionBackgroundProgress,
    )
}

internal data class HorizontalCardChromeMotionFrame(
    val alpha: Float,
    /** 0=原位，1=向详情方向完成短距离跟随。 */
    val translationProgress: Float,
)

/**
 * 横卡 chrome 的透明度与短距离位移。
 *
 * 恒为「原位、满格」：横卡信息区不再自带走位/淡出时间轴，开合的交叉淡入淡出统一交给
 * 飞行详情页一侧（见 [resolveHomeCardChromeAlphaDuringShellReturnMorph]）。
 * 横卡与竖卡因此共用同一条观感，不再出现文字先跑一步再被页面吃掉。
 */
@Suppress("UNUSED_PARAMETER")
internal fun resolveHorizontalCardChromeMotionFrame(
    useCardContainerSharedBounds: Boolean,
    isSharedMorphSourceCard: Boolean,
    isReturningFromDetail: Boolean = false,
    transitionBackgroundPhase: VideoCardTransitionBackgroundPhase =
        VideoCardTransitionBackgroundPhase.IDLE,
    isVideoCardReturnGestureInProgress: Boolean = false,
    isSharedTransitionActive: Boolean = false,
    transitionBackgroundProgress: Float = 0f,
    isQuickReturnFromDetail: Boolean = false,
    preferWholeCardReturn: Boolean = false,
): HorizontalCardChromeMotionFrame = HorizontalCardChromeMotionFrame(
    alpha = 1f,
    translationProgress = 0f,
)

/**
 * 返回 shell morph 期间源卡 **chrome**（标题/UP/信息区）的 alpha。
 *
 * 恒为 **1**：列表真卡在开合全程原位满格。
 *
 * 交叉淡入淡出只由**飞行详情页**一侧完成 —— 整页底板与正文按
 * [com.android.purebilibili.core.ui.transition.resolveVideoCardDetailContentAlpha] 从 1 淡到 0，
 * 卡片是从它下面「透出来」的。若这里再乘一条互补 alpha，卡片可见度会变成 `(1 - a)²`，
 * 飞行中段整张卡几乎消失、落位才「补」回来，正是「只有封面在换、标题没有交换」的观感来源。
 */
@Suppress("UNUSED_PARAMETER")
internal fun resolveHomeCardChromeAlphaDuringShellReturnMorph(
    useCardContainerSharedBounds: Boolean,
    isSharedMorphSourceCard: Boolean,
    isReturningFromDetail: Boolean,
    transitionBackgroundPhase: VideoCardTransitionBackgroundPhase =
        VideoCardTransitionBackgroundPhase.IDLE,
    isVideoCardReturnGestureInProgress: Boolean = false,
    isSharedTransitionActive: Boolean = false,
    transitionBackgroundProgress: Float = 0f,
    isQuickReturnFromDetail: Boolean = false,
    preferWholeCardReturn: Boolean = false,
): Float = 1f

/**
 * 兼容旧布尔语义：chrome 尚未完全露出版视为仍在抑制。
 */
internal fun shouldSuppressHomeCardVisualDuringShellReturnMorph(
    useCardContainerSharedBounds: Boolean,
    isSharedMorphSourceCard: Boolean,
    isReturningFromDetail: Boolean,
    transitionBackgroundPhase: VideoCardTransitionBackgroundPhase =
        VideoCardTransitionBackgroundPhase.IDLE,
    isVideoCardReturnGestureInProgress: Boolean = false,
    isSharedTransitionActive: Boolean = false,
    transitionBackgroundProgress: Float = 0f,
    isQuickReturnFromDetail: Boolean = false,
    preferWholeCardReturn: Boolean = false,
): Boolean {
    return resolveHomeCardChromeAlphaDuringShellReturnMorph(
        useCardContainerSharedBounds = useCardContainerSharedBounds,
        isSharedMorphSourceCard = isSharedMorphSourceCard,
        isReturningFromDetail = isReturningFromDetail,
        transitionBackgroundPhase = transitionBackgroundPhase,
        isVideoCardReturnGestureInProgress = isVideoCardReturnGestureInProgress,
        isSharedTransitionActive = isSharedTransitionActive,
        transitionBackgroundProgress = transitionBackgroundProgress,
        isQuickReturnFromDetail = isQuickReturnFromDetail,
        preferWholeCardReturn = preferWholeCardReturn,
    ) < 1f
}

internal fun normalizeVideoCardSourceRouteForSharedKey(sourceRoute: String?): String? {
    return normalizeSharedElementSourceRoute(sourceRoute)
}

internal fun resolveVideoCardSharedReturnTargetKey(
    bvid: String,
    sourceRoute: String?,
): String? {
    val normalizedBvid = bvid.trim()
    val normalizedRoute = normalizeVideoCardSourceRouteForSharedKey(sourceRoute) ?: return null
    if (normalizedBvid.isEmpty()) return null
    return "$normalizedRoute:$normalizedBvid"
}

internal fun isVideoCardSharedReturnTarget(
    bvid: String,
    sourceRoute: String?,
    lastClickedVideoSourceKey: String?,
): Boolean {
    val key = resolveVideoCardSharedReturnTargetKey(bvid, sourceRoute) ?: return false
    return key == lastClickedVideoSourceKey
}

/**
 * Disambiguates duplicate videos that are simultaneously visible in large-screen grids.
 * Route + bvid alone is insufficient when a hero card and a feed card show the same video.
 */
internal fun isVideoCardSharedSourceInstanceOwner(
    sourceInstanceId: Long,
    lastClickedSourceInstanceId: Long?,
): Boolean = lastClickedSourceInstanceId == null ||
    sourceInstanceId == lastClickedSourceInstanceId

internal data class StoryVideoCardScrollLiteVisualPolicy(
    val coverShadowElevationDp: Float,
    val showSecondaryStatsRow: Boolean
)

internal fun resolveStoryVideoCardScrollLiteVisualPolicy(
    scrollLiteModeEnabled: Boolean
): StoryVideoCardScrollLiteVisualPolicy {
    return if (scrollLiteModeEnabled) {
        StoryVideoCardScrollLiteVisualPolicy(
            coverShadowElevationDp = 0f,
            showSecondaryStatsRow = true
        )
    } else {
        StoryVideoCardScrollLiteVisualPolicy(
            coverShadowElevationDp = 0f,
            showSecondaryStatsRow = true
        )
    }
}
