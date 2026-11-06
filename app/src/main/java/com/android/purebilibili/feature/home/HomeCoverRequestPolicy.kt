package com.android.purebilibili.feature.home

import com.android.purebilibili.core.util.FormatUtils
import com.android.purebilibili.data.model.response.VideoItem
import com.android.purebilibili.feature.home.components.cards.resolveVideoCardCoverCacheKey
import kotlin.math.ceil

internal data class HomeCoverRequestSpec(
    val widthPx: Int,
    val heightPx: Int,
) {
    val cacheKeySuffix: String = "${widthPx}x$heightPx"

    fun resolveUrl(url: String?): String =
        FormatUtils.buildSizedImageUrl(url, width = widthPx, height = heightPx)
}

/** Shared identity for the visible cover and its ahead-of-scroll preload. */
internal data class HomeCoverImageSource(val url: String, val cacheKey: String)

internal fun resolveHomeCoverImageSource(
    video: VideoItem,
    useLowQualityCover: Boolean,
    requestSpec: HomeCoverRequestSpec? = null,
): HomeCoverImageSource = HomeCoverImageSource(
    url = requestSpec?.resolveUrl(video.pic)
        ?: FormatUtils.resolveVideoCoverUrl(video.pic, useLowQuality = useLowQualityCover),
    cacheKey = resolveVideoCardCoverCacheKey(video, useLowQualityCover, requestSpec),
)

internal fun resolveHomeCoverRequestSpec(
    cardWidthDp: Float,
    density: Float,
    useLowQualityCover: Boolean,
): HomeCoverRequestSpec {
    if (useLowQualityCover) {
        return HomeCoverRequestSpec(widthPx = 240, heightPx = 150)
    }

    // 按卡片真实像素宽取档。原先这里乘 1.25 做超采样，在 620dpi 的双列布局下
    // 把 573px 的显示宽度顶到 960 档，而 B 站的 @Ww_Hh 是等比缩放，
    // 解码像素因此多出 2.25 倍，96MB 缓存能装的封面从上百张掉到几十张，
    // 滑动时反复重新解码。请求尺寸贴近显示尺寸即可，1.25 倍的清晰度收益看不出来。
    val displayWidthPx = ceil(cardWidthDp.coerceAtLeast(0f) * density.coerceAtLeast(0f)).toInt()
    val widthPx = if (displayWidthPx <= 0) {
        HOME_COVER_DEFAULT_WIDTH_PX
    } else {
        HOME_COVER_WIDTH_TIERS.firstOrNull { it >= displayWidthPx }
            ?: HOME_COVER_WIDTH_TIERS.last()
    }
    return HomeCoverRequestSpec(
        widthPx = widthPx,
        heightPx = widthPx * 10 / 16,
    )
}

internal const val HOME_COVER_DEFAULT_WIDTH_PX = 640

private val HOME_COVER_WIDTH_TIERS = intArrayOf(320, 480, 640, 960, 1280)
