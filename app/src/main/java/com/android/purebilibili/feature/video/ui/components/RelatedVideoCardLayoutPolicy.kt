package com.android.purebilibili.feature.video.ui.components

import androidx.compose.runtime.compositionLocalOf
import com.android.purebilibili.core.store.RelatedVideoCardStyle

/** The narrow half-opened pane cannot leave enough room for title and stats beside a cover. */
internal const val RELATED_VIDEO_HORIZONTAL_MIN_WIDTH_DP = 320

/** 紧凑横卡只要还能并排放下一张封面和两行标题就继续横排。 */
internal const val RELATED_VIDEO_COMPACT_MIN_WIDTH_DP = 200

/** 紧凑横卡按这个高度算封面宽度，比默认横卡矮一档，行高由文字区决定。 */
internal const val RELATED_VIDEO_COMPACT_COVER_HEIGHT_DP = 64f

internal enum class RelatedVideoCardLayout {
    HORIZONTAL,
    STACKED,
}

/** 详情页已读到的行形态，供真实推荐行和占位骨架共用。 */
internal val LocalRelatedVideoCardStyle = compositionLocalOf { RelatedVideoCardStyle.COMPACT }

internal fun resolveRelatedVideoCardLayout(
    availableWidthDp: Int,
    style: RelatedVideoCardStyle = RelatedVideoCardStyle.COMPACT,
): RelatedVideoCardLayout {
    val minWidthDp = when (style) {
        RelatedVideoCardStyle.COMPACT -> RELATED_VIDEO_COMPACT_MIN_WIDTH_DP
        RelatedVideoCardStyle.LARGE -> RELATED_VIDEO_HORIZONTAL_MIN_WIDTH_DP
    }
    return if (availableWidthDp < minWidthDp) {
        RelatedVideoCardLayout.STACKED
    } else {
        RelatedVideoCardLayout.HORIZONTAL
    }
}

internal fun resolveRelatedVideoListCardLayout(
    viewportWidthPx: Int,
    horizontalContentPaddingPx: Int,
    cardHorizontalPaddingPx: Int,
    density: Float,
    style: RelatedVideoCardStyle = RelatedVideoCardStyle.COMPACT,
): RelatedVideoCardLayout {
    val cardWidthPx = (viewportWidthPx - horizontalContentPaddingPx - 2 * cardHorizontalPaddingPx)
        .coerceAtLeast(0)
    return resolveRelatedVideoCardLayout((cardWidthPx / density).toInt(), style)
}
