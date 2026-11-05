package com.android.purebilibili.core.ui.transition

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/** 屏幕圆角兜底值；取不到物理圆角时按 ColorOS 常见的 26dp 画。 */
internal const val VIDEO_CARD_MORPH_FALLBACK_SCREEN_CORNER_DP = 26f

/**
 * 飞卡在 overlay 里的裁切轮廓：圆角在「来源卡圆角」与「屏幕物理圆角」之间随 bounds 高度插值。
 *
 * 固定半径的 `RoundedCornerShape` 在整段 morph 里是同一个值：起手时元素还是整屏，
 * 十几 dp 的圆角铺在 1240px 画面上和直角看不出区别，用户看到的就是四个直角往里收；
 * 缩到卡片大小时同一个半径又偏大。COUI 的返回是「屏幕圆角 → 卡片圆角」一路收，
 * 这里按动画 bounds 的高度还原同一条曲线。
 */
@OptIn(ExperimentalSharedTransitionApi::class)
internal class VideoCardMorphCornerClip(
    private val sourceCornerDpProvider: () -> Float,
    private val screenCornerPxProvider: () -> Float,
    private val sourceHeightPxProvider: () -> Float,
    private val screenHeightPxProvider: () -> Float,
) : SharedTransitionScope.OverlayClip {

    private val path = Path()

    override fun getClipPath(
        sharedContentState: SharedTransitionScope.SharedContentState,
        bounds: Rect,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Path {
        val sourceCornerPx = with(density) {
            sourceCornerDpProvider().coerceAtLeast(0f).dp.toPx()
        }
        val screenCornerPx = screenCornerPxProvider()
            .takeIf { it > 0.5f }
            ?: with(density) { VIDEO_CARD_MORPH_FALLBACK_SCREEN_CORNER_DP.dp.toPx() }
        val sourceHeightPx = sourceHeightPxProvider()
        val screenHeightPx = screenHeightPxProvider()
        val travel = screenHeightPx - sourceHeightPx
        val progress = if (
            travel > 1f &&
            bounds.height.isFinite() &&
            sourceHeightPx > 1f
        ) {
            ((bounds.height - sourceHeightPx) / travel).coerceIn(0f, 1f)
        } else {
            0f
        }
        val radius = (sourceCornerPx + (screenCornerPx - sourceCornerPx) * progress)
            .coerceIn(0f, bounds.minDimension / 2f)
        path.reset()
        if (radius <= 0.5f) {
            path.addRect(bounds)
        } else {
            path.addRoundRect(
                RoundRect(
                    left = bounds.left,
                    top = bounds.top,
                    right = bounds.right,
                    bottom = bounds.bottom,
                    cornerRadius = CornerRadius(radius, radius),
                ),
            )
        }
        return path
    }
}
