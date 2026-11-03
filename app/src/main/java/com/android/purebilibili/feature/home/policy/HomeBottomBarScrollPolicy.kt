package com.android.purebilibili.feature.home.policy

internal data class HomeBottomBarScrollState(
    val firstVisibleItem: Int,
    val scrollOffset: Int
)

internal data class HomeBottomBarScrollUpdate(
    val state: HomeBottomBarScrollState,
    val visibilityIntent: BottomBarVisibilityIntent?
)

internal fun reduceHomeBottomBarListScroll(
    previousState: HomeBottomBarScrollState,
    firstVisibleItem: Int,
    scrollOffset: Int,
    isVideoNavigating: Boolean,
    contentInteractionRestored: Boolean = false,
    topRevealThresholdPx: Int = 100,
    offsetHysteresisPx: Int = 200,
    isAtScrollEnd: Boolean = false,
): HomeBottomBarScrollUpdate {
    val nextState = HomeBottomBarScrollState(
        firstVisibleItem = firstVisibleItem,
        scrollOffset = scrollOffset
    )
    if (isVideoNavigating && !contentInteractionRestored) {
        return HomeBottomBarScrollUpdate(
            state = nextState,
            visibilityIntent = null
        )
    }

    val intent = when {
        // 触底即恢复：列表底部已按 dock 高度预留，dock 复现不会盖住最后一条内容，
        // 继续隐藏反而让用户在末尾失去导航入口。
        isAtScrollEnd -> BottomBarVisibilityIntent.SHOW
        firstVisibleItem == 0 && scrollOffset < topRevealThresholdPx -> BottomBarVisibilityIntent.SHOW
        firstVisibleItem > previousState.firstVisibleItem -> BottomBarVisibilityIntent.HIDE
        firstVisibleItem < previousState.firstVisibleItem -> BottomBarVisibilityIntent.SHOW
        scrollOffset > previousState.scrollOffset + offsetHysteresisPx -> BottomBarVisibilityIntent.HIDE
        scrollOffset < previousState.scrollOffset - offsetHysteresisPx -> BottomBarVisibilityIntent.SHOW
        else -> null
    }

    return HomeBottomBarScrollUpdate(
        state = nextState,
        visibilityIntent = intent
    )
}

internal fun resolveBottomBarChromeScrollOffset(
    firstVisibleItem: Int,
    scrollOffset: Int,
    offscreenOffset: Float = 1000f
): Float {
    return if (firstVisibleItem == 0) {
        scrollOffset.coerceAtLeast(0).toFloat()
    } else {
        offscreenOffset
    }
}
