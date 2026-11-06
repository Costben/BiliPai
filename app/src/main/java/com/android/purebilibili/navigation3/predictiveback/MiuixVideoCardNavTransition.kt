package com.android.purebilibili.navigation3.predictiveback

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.structuralEqualityPolicy
import kotlin.math.PI
import kotlin.math.sin
import com.android.purebilibili.core.ui.transition.VideoCardSourceLayout
import com.android.purebilibili.core.ui.transition.VideoHeroMotionSpec
import com.android.purebilibili.core.ui.transition.VideoHeroMotionTokens
import com.android.purebilibili.core.ui.transition.VideoCardTransitionSettleState
import com.android.purebilibili.core.ui.transition.resolveVideoCardNativeBodyPresence
import com.android.purebilibili.core.ui.transition.resolveVideoHeroMotionSpec
import com.android.purebilibili.core.ui.transition.resolveVideoHeroLandingScale
import top.yukonga.miuix.kmp.nav.transition.NavMotion
import top.yukonga.miuix.kmp.nav.transition.NavRole
import top.yukonga.miuix.kmp.nav.transition.NavSettle
import top.yukonga.miuix.kmp.nav.transition.NavSettleSpec
import top.yukonga.miuix.kmp.nav.transition.NavTransition
import top.yukonga.miuix.kmp.nav.transition.NavTransitionScope
import top.yukonga.miuix.kmp.nav.transition.NavSettlePhase

internal enum class MiuixVideoCardContentScale {
    /** Home dual-column / stacked cards: media fills card width, pinned to top. */
    FillWidthTop,
    /** Fullscreen, side-by-side, or square-ish morphs: cover scale about center. */
    CropCenter,
}

/**
 * Pick entry content compensation from the frozen source layout.
 * Side-by-side related/home single-column rows avoid FillWidthTop's vertical stretch
 * (which reads as "whole page crushed into a thin strip").
 */
internal fun resolveMiuixVideoCardContentScaleForSourceLayout(
    sourceLayout: VideoCardSourceLayout,
    fullscreen: Boolean = false,
): MiuixVideoCardContentScale {
    if (fullscreen) return MiuixVideoCardContentScale.CropCenter
    // FillWidthTop for both STACKED and SIDE_BY_SIDE so inverse-scale landing
    // (1/sourceScale) + outer non-uniform scale maps to the frozen card bounds.
    // CropCenter shifts top-left landing anchors and turns horizontal cards into black strips.
    return when (sourceLayout) {
        VideoCardSourceLayout.SIDE_BY_SIDE,
        VideoCardSourceLayout.STACKED,
        VideoCardSourceLayout.COVER_ONLY,
        -> MiuixVideoCardContentScale.FillWidthTop
    }
}

internal data class MiuixVideoCardContentCompensation(
    val scaleX: Float,
    val scaleY: Float,
    val transformOrigin: TransformOrigin,
)

internal data class MiuixVideoCardClipRadii(
    val radiusX: Float,
    val radiusY: Float,
)

internal data class MiuixVideoCardGestureTransform(
    val translationX: Float,
    val translationY: Float,
    val rotationZ: Float,
    val transformOrigin: TransformOrigin,
    val liftScale: Float = 1f,
    val cameraDistance: Float = 8f,
    val shadowElevationDp: Float = 0f,
    val rotationX: Float = 0f,
    val rotationY: Float = 0f,
)

internal const val MIUIX_VIDEO_CARD_GESTURE_LIFT_SCALE = 0.02f
internal const val MIUIX_VIDEO_CARD_GESTURE_CAMERA_DISTANCE_DP = 8f
internal const val MIUIX_VIDEO_CARD_GESTURE_CAMERA_PULL_DP = 1.0f
internal const val MIUIX_VIDEO_CARD_GESTURE_SHADOW_DP = 12f
internal const val MIUIX_VIDEO_CARD_FLOATING_CORNER_DP = 28f

/** Covered-entry treatment, owned by the Miuix navigation boundary, never by page children. */
internal fun resolveMiuixVideoCardCoveredScale(depth: Float, reducedMotion: Boolean): Float =
    if (reducedMotion) 1f else 1f - 0.03f * depth.coerceIn(0f, 1f)

/** 屏幕上的圆角随来源页后退同步建立，恢复时归零，不在首个缩放帧跳到整角。 */
internal fun resolveMiuixVideoCardCoveredCornerPx(
    depth: Float,
    reducedMotion: Boolean,
    density: Float,
): Float {
    if (reducedMotion) return 0f
    val scale = resolveMiuixVideoCardCoveredScale(depth, reducedMotion)
    return 24f * density * depth.coerceIn(0f, 1f) / scale
}

/** 两份轮廓交替复用，保证 layer 能识别圆角更新，无需逐帧分配 Shape。 */
internal class MiuixVideoCardCoveredShapeCache {
    private val shapes = arrayOf(MiuixVideoCardClipShape(), MiuixVideoCardClipShape())
    private var index = 0

    fun resolve(radiusPx: Float): Shape {
        if (radiusPx <= 0f) return androidx.compose.ui.graphics.RectangleShape
        val current = shapes[index]
        if (current.radiusX == radiusPx) return current
        index = 1 - index
        return shapes[index].also {
            it.radiusX = radiusPx
            it.radiusY = radiusPx
        }
    }
}

/** Video-to-video transitions extend the black player/window canvas into the scale gap. */
internal fun resolveMiuixVideoCardBackingColor(sourceRoute: String?, surface: Color): Color =
    if (sourceRoute?.substringBefore('?')?.startsWith("video/") == true) Color.Black else surface

internal fun resolveMiuixVideoCardGesturePoseWeight(morphProgress: Float): Float {
    val pull = (1f - morphProgress.coerceIn(0f, 1f)).coerceIn(0f, 1f)
    return sin(PI.toFloat() * pull)
}

/**
 * 点击卡片进场时的三维对称变换：与返回手势构成镜像闭环。
 * 倾角根据卡片距屏幕中线的横向相对偏角连续线性加权（向中线平滑衰减归零），避免平板多列及中线卡片突兀晃动。
 * 随 morph (0->1) 升起至中段达到峰值，并在落地全屏时平滑归零。
 */
internal fun resolveMiuixVideoCardClickTransform(
    morphProgress: Float,
    widthPx: Float,
    heightPx: Float,
    sourceBounds: Rect,
): MiuixVideoCardGestureTransform {
    val morph = morphProgress.coerceIn(0f, 1f)
    val poseWeight = sin(PI.toFloat() * morph)
    val cardCenterX = (sourceBounds.left + sourceBounds.right) / 2f
    val screenCenterX = widthPx.coerceAtLeast(1f) / 2f
    // 归一化横向相对偏角：屏幕正中为 0，最左为 -1.0，最右为 +1.0
    val horizontalOffset = if (screenCenterX > 1f) {
        ((cardCenterX - screenCenterX) / screenCenterX).coerceIn(-1f, 1f)
    } else {
        0f
    }
    val screenCenterY = heightPx.coerceAtLeast(1f) / 2f
    val cardCenterY = (sourceBounds.top + sourceBounds.bottom) / 2f
    // 归一化纵向相对偏角：屏幕正中为 0，最上为 -1.0，最下为 +1.0
    val verticalOffset = if (screenCenterY > 1f) {
        ((cardCenterY - screenCenterY) / screenCenterY).coerceIn(-1f, 1f)
    } else {
        0f
    }

    return MiuixVideoCardGestureTransform(
        translationX = 0f,
        translationY = 0f,
        // 向屏幕内侧微倾：右侧卡片为负角（逆时针向内微倾），左侧卡片为正角（顺时针向内微倾）
        // 手机双列偏角约为 ±0.5，对应优雅自然的 ±1.6° 倾角；中列卡片偏角为 0，平正如初
        rotationZ = -horizontalOffset * 3.2f * poseWeight,
        // 三维拟物微透视：卡片飞起时根据受力点象限产生微小俯仰与侧倾（±1°~1.5°），落地全屏时平滑归零
        rotationX = -verticalOffset * 1.6f * poseWeight,
        rotationY = horizontalOffset * 1.4f * poseWeight,
        transformOrigin = TransformOrigin(
            pivotFractionX = (0.5f - 0.2f * horizontalOffset).coerceIn(0.2f, 0.8f),
            pivotFractionY = (0.5f - 0.2f * verticalOffset).coerceIn(0.2f, 0.8f),
        ),
        liftScale = 1f - 0.025f * poseWeight,
        cameraDistance = MIUIX_VIDEO_CARD_GESTURE_CAMERA_DISTANCE_DP - 2.5f * poseWeight,
        shadowElevationDp = MIUIX_VIDEO_CARD_GESTURE_SHADOW_DP * poseWeight,
    )
}

internal fun resolveMiuixVideoCardLinearCornerPx(
    sourceCornerPx: Float,
    fullscreenCornerPx: Float,
    morphProgress: Float,
    sourceLayout: VideoCardSourceLayout = VideoCardSourceLayout.COVER_ONLY,
    currentShortSidePx: Float = Float.POSITIVE_INFINITY,
): Float {
    val morph = morphProgress.coerceIn(0f, 1f)
    val source = sourceCornerPx.coerceAtLeast(0f)
    val target = fullscreenCornerPx.coerceAtLeast(0f)
    if (sourceLayout == VideoCardSourceLayout.SIDE_BY_SIDE && source == 0f && target == 0f) {
        return 0f
    }
    if (sourceLayout == VideoCardSourceLayout.SIDE_BY_SIDE && source == 0f) {
        return (target * morph).coerceAtMost(currentShortSidePx.coerceAtLeast(0f) / 2f)
    }
    val radius = source + (target - source) * morph
    return radius.coerceAtMost(currentShortSidePx.coerceAtLeast(0f) / 2f)
}

/**
 * 开合的缓动：起步快、收得软，和系统卡片展开一致（Cobo / c001apk 用的是同一条）。
 *
 * 装配在宿主那份帧时钟上（它在时间上是线性的），所以整段展开不会挤在头一百毫秒里。
 */
internal val MIUIX_VIDEO_CARD_MORPH_EASING = CubicBezierEasing(0.3f, 0f, 0.1f, 1f)

internal fun resolveMiuixVideoCardGestureCornerPx(
    sourceCornerPx: Float,
    morphProgress: Float,
    floatingCornerPx: Float,
    fullscreenCornerPx: Float = 0f,
    sourceLayout: VideoCardSourceLayout = VideoCardSourceLayout.COVER_ONLY,
    currentShortSidePx: Float = Float.POSITIVE_INFINITY,
): Float {
    val morph = morphProgress.coerceIn(0f, 1f)
    val source = sourceCornerPx.coerceAtLeast(0f)
    val target = fullscreenCornerPx.coerceAtLeast(0f)
    // 透明横卡没有整卡圆角，封面在独立媒体层裁剪。外壳若鼓起设备圆角，
    // 会只切掉贴着整行左边缘的封面两角，右侧封面圆角却不变。
    if (sourceLayout == VideoCardSourceLayout.SIDE_BY_SIDE && source == 0f && target == 0f) {
        return 0f
    }
    // 带圆角底板的卡片仍使用设备悬浮圆角。
    // 端点和中段斜率均归零，返回同一深度取得同一半径，不跳变或额外套一层设备裁切。
    val floating = floatingCornerPx.coerceAtLeast(0f)
    val u = (if (morph <= .5f) morph * 2f else (morph - .5f) * 2f).coerceIn(0f, 1f)
    val fraction = u * u * (3f - 2f * u)
    val radius = if (morph <= .5f) source + (floating - source) * fraction
        else floating + (target - floating) * fraction
    return radius
        .coerceAtMost(currentShortSidePx.coerceAtLeast(0f) / 2f)
}

/** Map a card-local gesture pivot into the fullscreen entry's transform origin. */
internal fun resolveMiuixVideoCardGestureVisualOrigin(
    sourceBounds: Rect,
    layoutWidth: Float,
    layoutHeight: Float,
    morph: Float,
    localOrigin: TransformOrigin,
    preserveViewport: Boolean = false,
): TransformOrigin {
    val m = morph.coerceIn(0f, 1f)
    val width = layoutWidth.coerceAtLeast(1f)
    val height = layoutHeight.coerceAtLeast(1f)
    val visualLeft = sourceBounds.left * (1f - m)
    val visualTop = sourceBounds.top * (1f - m)
    val visualWidth = sourceBounds.width + (width - sourceBounds.width) * m
    val visualHeight = if (preserveViewport) {
        height * resolveMiuixVideoCardOuterScaleY(sourceBounds.width / width, sourceBounds.height / height, m, 1f)
    } else sourceBounds.height + (height - sourceBounds.height) * m
    return TransformOrigin(
        pivotFractionX = ((visualLeft + visualWidth * localOrigin.pivotFractionX) / width)
            .coerceIn(0f, 1f),
        pivotFractionY = ((visualTop + visualHeight * localOrigin.pivotFractionY) / height)
            .coerceIn(0f, 1f),
    )
}

/** Top entry depth is 0 at rest and moves toward -1 while returning. */
internal fun resolveMiuixVideoCardDepthProgress(relativeDepth: Float): Float =
    topProgress(relativeDepth)

/**
 * Manual predictive-back drag may land the card fully. Reserving a visible portion
 * for the committed settle made full-finger landing impossible and replayed a second
 * flight after release; with cap 1f a full drag leaves the commit tween nothing to
 * animate (remaining = 0), while partial releases still settle smoothly.
 */
internal const val MIUIX_VIDEO_CARD_GESTURE_MAX_RETURN = 1f
// Reserve only the final 1% for release. The shared content timeline still reaches its
// cover/chrome endpoint (98%) while held, while the free pose remains away from the source slot.
internal const val MIUIX_VIDEO_CARD_FOLLOW_MAX_RETURN = 0.99f

internal fun resolveMiuixVideoCardSeekReturn(
    startReturn: Float,
    gestureProgress: Float,
    maxReturn: Float = MIUIX_VIDEO_CARD_GESTURE_MAX_RETURN,
): Float = (startReturn + gestureProgress.coerceIn(0f, 1f) * maxReturn)
    // An interrupted push can already be nearer the card than the preview limit. Never
    // jump it back toward fullscreen merely to manufacture more return distance.
    .coerceIn(0f, maxOf(startReturn, maxReturn).coerceIn(0f, 1f))

internal fun resolveMiuixVideoCardSettleReturn(
    rawReturn: Float,
    releaseRawReturn: Float,
    releaseVisualReturn: Float,
    committing: Boolean,
): Float {
    val raw = rawReturn.coerceIn(0f, 1f)
    val releaseRaw = releaseRawReturn.coerceIn(0f, 1f)
    val releaseVisual = releaseVisualReturn.coerceIn(0f, 1f)
    return if (committing) {
        val remaining = 1f - releaseRaw
        if (remaining <= 0f) 1f else {
            releaseVisual + (1f - releaseVisual) *
                ((raw - releaseRaw) / remaining).coerceIn(0f, 1f)
        }
    } else {
        if (releaseRaw <= 0f) 0f else {
            releaseVisual * (raw / releaseRaw).coerceIn(0f, 1f)
        }
    }
}

internal fun resolveMiuixVideoCardOuterScale(sourceScale: Float, depth: Float, landingScale: Float): Float {
    val source = sourceScale.coerceIn(0.05f, 1f)
    return source + (1f - source) * depth.coerceIn(0f, 1f) - source * (1f - landingScale)
}

/** Contract the native viewport while its body fades, rather than in a separate second step. */
internal fun resolveMiuixVideoCardOuterScaleY(
    sourceScaleX: Float,
    sourceScaleY: Float,
    depth: Float,
    landingScale: Float,
): Float {
    val x = resolveMiuixVideoCardOuterScale(sourceScaleX, depth, landingScale)
    // Once body content yields, the clip must fit the uniformly scaled source card.
    // Interpolating full-page height independently leaves a tall empty shell even
    // when the native card pixels have already taken over.
    val cardY = x * sourceScaleY.coerceIn(0.05f, 1f) / sourceScaleX.coerceIn(0.05f, 1f)
    val retainedViewport = resolveVideoCardNativeBodyPresence(depth)
    return cardY + (x - cardY) * retainedViewport
}

internal data class MiuixVideoCardInverseScale(
    val scaleX: Float,
    val scaleY: Float,
)

/**
 * Inverse of the current outer morph after FillWidthTop compensation.
 *
 * The outer clip and FillWidthTop compensation already produce a uniform width-based
 * scale. Reconstruct source pixels at that SAME scale on both axes. Fitting source height
 * independently to the clip stretches glyphs and inserts space below the cover.
 */
internal fun resolveMiuixVideoCardInverseScale(
    sourceScaleX: Float,
    outerScaleX: Float,
    outerScaleY: Float,
): MiuixVideoCardInverseScale {
    val compensation = resolveMiuixVideoCardContentCompensation(
        outerScaleX = outerScaleX,
        outerScaleY = outerScaleY,
        contentScale = MiuixVideoCardContentScale.FillWidthTop,
    )
    val sourcePixelScale = outerScaleX / sourceScaleX.coerceAtLeast(0.01f)
    return MiuixVideoCardInverseScale(
        scaleX = sourcePixelScale / (outerScaleX * compensation.scaleX).coerceAtLeast(0.01f),
        scaleY = sourcePixelScale / (outerScaleY * compensation.scaleY).coerceAtLeast(0.01f),
    )
}

internal fun resolveMiuixVideoCardInverseScaleForDepth(
    sourceScaleX: Float,
    sourceScaleY: Float,
    depth: Float,
    autoReturning: Boolean = false,
): MiuixVideoCardInverseScale {
    val morph = depth.coerceIn(0f, 1f)
    val landingScale = resolveVideoHeroLandingScale(morph, autoReturning)
    return resolveMiuixVideoCardInverseScale(
        sourceScaleX = sourceScaleX,
        outerScaleX = resolveMiuixVideoCardOuterScale(sourceScaleX, morph, landingScale),
        outerScaleY = resolveMiuixVideoCardOuterScaleY(sourceScaleX, sourceScaleY, morph, landingScale),
    )
}

/**
 * Keeps the corner circular in screen space while the outer card layer scales non-uniformly.
 * A regular RoundedCornerShape is scaled together with the layer and becomes too small on the
 * compressed axis, which exposes the retained source card near the end of a card return.
 */
internal fun resolveMiuixVideoCardClipRadii(
    sourceCornerPx: Float,
    outerScaleX: Float,
    outerScaleY: Float,
    morphProgress: Float = 0f,
    floatingCornerPx: Float = sourceCornerPx,
    fullscreenCornerPx: Float = 0f,
    sourceLayout: VideoCardSourceLayout = VideoCardSourceLayout.COVER_ONLY,
    currentShortSidePx: Float = Float.POSITIVE_INFINITY,
): MiuixVideoCardClipRadii {
    val physicalRadius = resolveMiuixVideoCardGestureCornerPx(
        sourceCornerPx = sourceCornerPx,
        morphProgress = morphProgress,
        floatingCornerPx = floatingCornerPx,
        fullscreenCornerPx = fullscreenCornerPx,
        sourceLayout = sourceLayout,
        currentShortSidePx = currentShortSidePx,
    )
    return MiuixVideoCardClipRadii(
        radiusX = physicalRadius / outerScaleX.coerceAtLeast(0.01f),
        radiusY = physicalRadius / outerScaleY.coerceAtLeast(0.01f),
    )
}

/**
 * 开合几何：整页按**宽度**等比缩放，再叠一个从来源卡片矩形长到整屏的可见窗口。
 *
 * 与 Cobo / c001apk 的卡片展开同一套做法：
 * - 两轴共用同一个比例（`frameWidth / layoutWidth`），所以画面不会被压扁；
 * - 图层原点在左上角，屏幕坐标 = 页面坐标 × scale + 位移，位移从卡片左上角逐帧走到 (0, 0)；
 * - 可见高度也逐帧从卡片高度走到整屏高度，超出窗口的部分由圆角矩形裁掉。
 *
 * 进度 0 时屏幕上正好是「卡片大小、卡片圆角、页面的顶部那一块」——也就是那张卡；
 * 进度 1 时是原样的整屏。返回就是这条曲线的倒放。
 */
internal data class MiuixVideoCardMorphFrame(
    val scale: Float,
    val translationX: Float,
    val translationY: Float,
    /** 图层坐标系里的可见窗口上边。 */
    val visibleTopPx: Float,
    /** 图层坐标系里的可见高度（屏幕可见高度 / scale）。 */
    val visibleHeightPx: Float,
)

internal fun resolveMiuixVideoCardMorphFrame(
    bounds: Rect,
    layoutWidth: Float,
    layoutHeight: Float,
    morphProgress: Float,
    /**
     * 页面内容相对整屏的顶边内缩（状态栏那一截）。
     *
     * 整屏时视频就贴在屏幕顶边；缩到卡片大小时要把这一截空带挤掉，否则卡片里会多出
     * 一条底色横杠——视频是从卡片的第二条边才开始画的。Cobo / c001apk 的展开同样有
     * 这一步（`translationY = -statusBarTop * (1 - p)`）。
     */
    contentTopInsetPx: Float = 0f,
): MiuixVideoCardMorphFrame {
    val width = layoutWidth.coerceAtLeast(1f)
    val height = layoutHeight.coerceAtLeast(1f)
    val morph = morphProgress.coerceIn(0f, 1f)
    val frameWidth = bounds.width + (width - bounds.width) * morph
    val frameHeight = bounds.height + (height - bounds.height) * morph
    val scale = (frameWidth / width).coerceIn(0.02f, 1f)
    val screenTop = bounds.top * (1f - morph)
    // 顶边对齐：缩到卡片大小时内容先上移 statusBar 那一截，露出来的正是播放器。
    val inset = contentTopInsetPx.coerceAtLeast(0f)
    val shiftPx = inset * (1f - morph)
    return MiuixVideoCardMorphFrame(
        scale = scale,
        translationX = bounds.left * (1f - morph),
        translationY = screenTop - shiftPx * scale,
        visibleTopPx = shiftPx,
        visibleHeightPx = frameHeight / scale,
    )
}

/**
 * 到这一段行程时页面已经铺满，之前它是一点点从卡片上「长」出来的。
 *
 * 页面窗口和真卡在这段时间里重叠：卡片原位照常画着，页面从它上面盖过去。
 * 曲线是线性的——缓动已经加在进度上了，这里再弯一次会让开头顿住。
 */
internal const val MIUIX_VIDEO_CARD_MORPH_SOLID_AT = 0.36f

/** 页面可见度：0 = 只剩底下那张真卡，1 = 页面实心。 */
internal fun resolveMiuixVideoCardMorphAlpha(morphProgress: Float): Float =
    (morphProgress.coerceIn(0f, 1f) / MIUIX_VIDEO_CARD_MORPH_SOLID_AT).coerceIn(0f, 1f)

/**
 * 覆盖页（首页 / 列表）后退时的圆角轮廓：整块图层矩形，按屏幕半径倒推。
 *
 * 与 [MiuixVideoCardMorphShape] 分开：那个是飞行页的可见窗口，这个是垫在
 * 详情底下的静止页面边缘。
 */
internal class MiuixVideoCardClipShape(
    var radiusX: Float = 0f,
    var radiusY: Float = 0f,
) : Shape {
    private var lastWidth = -1f
    private var lastHeight = -1f
    private var lastRadiusX = -1f
    private var lastRadiusY = -1f
    private var cachedOutline: Outline? = null

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val rx = radiusX.coerceIn(0f, size.width / 2f)
        val ry = radiusY.coerceIn(0f, size.height / 2f)
        val current = cachedOutline
        if (current != null && lastWidth == size.width && lastHeight == size.height &&
            lastRadiusX == rx && lastRadiusY == ry
        ) {
            return current
        }
        lastWidth = size.width
        lastHeight = size.height
        lastRadiusX = rx
        lastRadiusY = ry
        val outline = Outline.Rounded(
            RoundRect(
                rect = Rect(0f, 0f, size.width, size.height),
                cornerRadius = CornerRadius(
                    x = rx,
                    y = ry,
                ),
            ),
        )
        cachedOutline = outline
        return outline
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MiuixVideoCardClipShape) return false
        return radiusX == other.radiusX && radiusY == other.radiusY
    }

    override fun hashCode(): Int {
        var result = radiusX.hashCode()
        result = 31 * result + radiusY.hashCode()
        return result
    }
}

/**
 * 飞行页的可见窗口：从 (0, 0) 起、宽整屏、高 [visibleHeightPx] 的圆角矩形。
 *
 * 用 data class：`graphicsLayer` 靠 `equals` 判断轮廓要不要重建，逐帧给一个新实例就行。
 */
internal data class MiuixVideoCardMorphShape(
    private val visibleTopPx: Float,
    private val visibleHeightPx: Float,
    private val radiusPx: Float,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val top = visibleTopPx.coerceIn(0f, size.height)
        val bottom = (visibleTopPx + visibleHeightPx).coerceIn(top, size.height)
        val shortSide = minOf(size.width, bottom - top).coerceAtLeast(0f)
        val radius = radiusPx.coerceIn(0f, shortSide / 2f)
        return Outline.Rounded(
            RoundRect(
                rect = Rect(0f, top, size.width, bottom),
                cornerRadius = CornerRadius(x = radius, y = radius),
            ),
        )
    }
}

internal fun resolveMiuixVideoCardContentCompensation(
    outerScaleX: Float,
    outerScaleY: Float,
    contentScale: MiuixVideoCardContentScale,
): MiuixVideoCardContentCompensation {
    val safeOuterScaleX = outerScaleX.coerceAtLeast(0.01f)
    val safeOuterScaleY = outerScaleY.coerceAtLeast(0.01f)
    val uniformScale = when (contentScale) {
        MiuixVideoCardContentScale.FillWidthTop -> safeOuterScaleX
        MiuixVideoCardContentScale.CropCenter -> maxOf(safeOuterScaleX, safeOuterScaleY)
    }
    return MiuixVideoCardContentCompensation(
        scaleX = uniformScale / safeOuterScaleX,
        scaleY = uniformScale / safeOuterScaleY,
        transformOrigin = when (contentScale) {
            MiuixVideoCardContentScale.FillWidthTop -> TransformOrigin(0.5f, 0f)
            MiuixVideoCardContentScale.CropCenter -> TransformOrigin.Center
        },
    )
}

/** Deferred bridge to the top video entry's live Miuix driver. */
internal class MiuixVideoCardTransitionProgress(
    private val gestureTranslationEnabled: Boolean = false,
    private val gesturePoseEnabled: Boolean = false,
) {
    val gestureFollow = VideoCardGestureFollowState()
    private val maxGestureReturn get() = if (gestureTranslationEnabled) {
        MIUIX_VIDEO_CARD_FOLLOW_MAX_RETURN
    } else MIUIX_VIDEO_CARD_GESTURE_MAX_RETURN
    private var topScope: NavTransitionScope? by mutableStateOf(null)
    // These predicates are also called from composition and card draw modifiers. Only
    // boundary changes may invalidate those callers; geometry still reads the live depth.
    private val gestureInProgress by derivedStateOf(structuralEqualityPolicy()) {
        topScope?.let {
            !isCoveredOrRevealedParent(it) && it.gesture != null && it.settle == null
        } == true
    }
    private val settleState by derivedStateOf(structuralEqualityPolicy()) { resolveSettleState() }
    private var gestureStartReturn: Float? = null
    private var lastSeekRawReturn = 0f
    private var lastSeekVisualReturn = 0f
    private var activeSettle: NavSettle? = null
    private var settleRawReturn = 0f
    private var settleVisualReturn = 0f
    private var lastVisualReturn = 0f
    private var followSequence: Long? = null
    private val neutralFollowPose = VideoCardFollowPose()
    private var followAnchorPose = neutralFollowPose
    private var lastFollowPose = neutralFollowPose
    private var releaseFollowPose = neutralFollowPose
    private var followSettle: NavSettle? = null
    // 仅保留自动阶段已经应用的对象；新抓取时才转换锚点，不增加逐帧姿态分配。
    private var automaticTransform: MiuixVideoCardGestureTransform? = null
    private var automaticOrigin = TransformOrigin.Center
    var contentTopInsetPx: Float = 0f

    fun recordAutomaticPose(transform: MiuixVideoCardGestureTransform, origin: TransformOrigin) {
        automaticTransform = transform
        automaticOrigin = origin
    }

    /**
     * 帧内记忆：详情 entry 的 `transformEntry` 会在同一帧里挂多层 graphicsLayer，
     * 每层都会读一次 `visualDepth`。而 `visualDepth` 不是纯读函数——它会推进
     * `gestureStartReturn` / `activeSettle` / `settleRawReturn` / `lastVisualReturn`
     * 这套状态机。同帧重复调用等于把状态机推进多次，手势与落位判定互相污染，
     * 且每帧放大成 N 次读取 + N 次 follow pose 分配，直接表现为转场掉帧。
     *
     * 这里以「scope/settle 身份 + 手势进度 + 深度/角色 + 帧序号」为键做帧内记忆：
     * - `NavTransitionScope` 与 `NavSettle` 都是每帧稳定（跨帧同引用或状态迁移时换新）
     *   的对象，用身份比较即可区分「同一帧的同一次采样」与「状态真的变了」。
     * - [localFrameSequence] 由调用方每帧递增，保证跨帧一定失效，不会把上一帧结果留用。
     */
    private var lastDepthScope: NavTransitionScope? = null
    private var lastDepthSettle: NavSettle? = null
    private var lastDepthGestureProgress: Float? = null
    private var lastDepthRelativeDepth: Float = Float.NaN
    private var lastDepthRole: NavRole? = null
    private var lastDepthFrame: Long = Long.MIN_VALUE
    private var lastDepthValue: Float = 0f

    private fun isCoveredOrRevealedParent(scope: NavTransitionScope): Boolean =
        scope.relativeDepth >= 0f && scope.role != NavRole.Outgoing

    /**
     * 当前帧序号。由宿主每帧递增（见 `advanceTransitionFrame`）。
     * 未接入宿主时保持 0，帧内记忆仍生效、跨帧自然失效（键会随深度、角色或手势变化）。
     */
    private var localFrameSequence: Long = 0L

    internal fun advanceTransitionFrame() {
        localFrameSequence += 1L
    }

    /** 带帧内记忆的 [visualDepth]：同一帧同一 scope 只推进一次状态机。 */
    fun visualDepthMemoized(scope: NavTransitionScope): Float {
        if (
            lastDepthFrame == localFrameSequence &&
            lastDepthScope === scope &&
            lastDepthSettle === scope.settle &&
            lastDepthGestureProgress == scope.gesture?.progress &&
            lastDepthRelativeDepth == scope.relativeDepth &&
            lastDepthRole == scope.role
        ) {
            return lastDepthValue
        }
        val value = visualDepth(scope)
        lastDepthScope = scope
        lastDepthSettle = scope.settle
        lastDepthGestureProgress = scope.gesture?.progress
        lastDepthRelativeDepth = scope.relativeDepth
        lastDepthRole = scope.role
        lastDepthFrame = localFrameSequence
        lastDepthValue = value
        return value
    }

    /** One mapping for the flying shell, its contents, and the retained source page. */
    fun visualDepth(scope: NavTransitionScope): Float {
        // Gesture/settle belongs to the whole NavDisplay, including covered entries. Returning
        // from BGM (or another child) reveals this video at positive depth; it does not close
        // the video into its original feed card. Keep it fullscreen through the landing frame.
        if (isCoveredOrRevealedParent(scope)) {
            gestureStartReturn = null
            activeSettle = null
            lastVisualReturn = 0f
            settleVisualReturn = 0f
            return 1f
        }
        val rawDepth = resolveMiuixVideoCardDepthProgress(scope.relativeDepth)
        val rawReturn = 1f - rawDepth
        val settle = scope.settle
        val gesture = scope.gesture
        val visualReturn = when {
            gesture != null && settle == null && scope.role != NavRole.Outgoing -> {
                if (activeSettle != null) {
                    // A new grab interrupts a restoring spring at its visible position.
                    gestureStartReturn = lastVisualReturn
                    activeSettle = null
                }
                val start = gestureStartReturn ?: (rawReturn - gesture.progress).also {
                    gestureStartReturn = it
                }
                resolveMiuixVideoCardSeekReturn(start, gesture.progress, maxGestureReturn).also {
                    lastSeekRawReturn = rawReturn
                    lastSeekVisualReturn = it
                }
            }
            gesture != null && settle != null &&
                (settle.phase == NavSettlePhase.Commit || settle.phase == NavSettlePhase.Cancel) -> {
                if (activeSettle !== settle) {
                    activeSettle = settle
                    settleRawReturn = if (gestureStartReturn != null) lastSeekRawReturn else rawReturn
                    settleVisualReturn = if (gestureStartReturn != null) lastSeekVisualReturn else {
                        resolveMiuixVideoCardSeekReturn(
                            rawReturn - gesture.progress,
                            gesture.progress,
                            maxGestureReturn,
                        )
                    }
                }
                resolveMiuixVideoCardSettleReturn(
                    rawReturn = rawReturn,
                    releaseRawReturn = settleRawReturn,
                    releaseVisualReturn = settleVisualReturn,
                    committing = settle.phase == NavSettlePhase.Commit,
                )
            }
            else -> rawReturn.also {
                if (gesture == null) {
                    gestureStartReturn = null
                    activeSettle = null
                }
            }
        }
        lastVisualReturn = visualReturn
        return (1f - visualReturn).coerceIn(0f, 1f)
    }

    /** Called only in the outer graphics layer; release pose follows the shared depth driver. */
    fun followPose(
        scope: NavTransitionScope,
        bounds: Rect,
        width: Float,
        height: Float,
        arcAmplitudePx: Float,
        motionSpec: VideoHeroMotionSpec,
        preserveViewport: Boolean = false,
    ): VideoCardFollowPose? {
        val firstGestureFrame = scope.relativeDepth == 0f && scope.role == NavRole.Top &&
            scope.gesture?.progress == 0f &&
            (scope.settle == null || scope.settle?.phase == NavSettlePhase.Cancel)
        if ((!gestureTranslationEnabled && !gesturePoseEnabled) || scope.gesture == null ||
            (isCoveredOrRevealedParent(scope) && !firstGestureFrame)
        ) {
            followSequence = null
            followSettle = null
            // 自动转场每帧也会经过这里，复用不可变的初始姿态。
            lastFollowPose = neutralFollowPose
            if (scope.gesture != null || !gesturePoseEnabled || isCoveredOrRevealedParent(scope)) {
                automaticTransform = null
            }
            return null
        }
        // 事件坐标晚于导航手势到达时，继续显示上一帧姿态，不先闪回单位变换。
        val sample = gestureFollow.localSample() ?: return automaticTransform?.let {
            automaticFollowPose(it)
        } ?: lastFollowPose
        // 同帧内复用 transformEntry 已经算过的 depth，避免此处再推进一次状态机。
        val morph = visualDepthMemoized(scope)
        val settle = scope.settle
        if (settle == null) {
            if (followSequence != sample.sequence) {
                followAnchorPose = automaticTransform?.let(::automaticFollowPose) ?: lastFollowPose
                automaticTransform = null
                followSequence = sample.sequence
            }
            followSettle = null
            val rawPose = resolveVideoCardFollowPose(
                sample, bounds, width, height, morph,
                anchorMorph = 1f - (gestureStartReturn ?: (1f - morph)),
                arcAmplitudePx = arcAmplitudePx,
                anchorPose = followAnchorPose,
                gestureProgress = scope.gesture?.progress ?: 0f,
                preserveViewport = preserveViewport,
            )
            val fixedOrigin = if (followAnchorPose.hasSpatialTransform) followAnchorPose.transformOrigin
            else resolveMiuixVideoCardGestureVisualOrigin(
                bounds, width, height, morph,
                androidx.compose.ui.graphics.TransformOrigin(
                    if (sample.initialTouch.x < width / 2f) .16f else .84f,
                    (sample.touch.y / height).coerceIn(.12f, .88f),
                ),
                preserveViewport = preserveViewport,
            )
            lastFollowPose = resolveVideoCardGestureChannels(
                rawPose, gestureTranslationEnabled, gesturePoseEnabled, fixedOrigin,
            )
        } else {
            if (followSettle !== settle) {
                followSettle = settle
                releaseFollowPose = lastFollowPose
            }
            val visualReturn = 1f - morph
            val weight = if (settle.phase == NavSettlePhase.Cancel) {
                if (settleVisualReturn <= 0f) {
                    // A vertical-only move can have zero back progress. Its pose still needs
                    // the cancel tween even though the depth driver's start and end are equal.
                    1f - motionSpec.cancelSpatialSpec.transform(
                        (settle.elapsedMillis / motionSpec.cancelDurationMillis.coerceAtLeast(1))
                            .coerceIn(0f, 1f),
                    )
                } else visualReturn / settleVisualReturn
            } else {
                if (settleVisualReturn >= 1f) 0f else (1f - visualReturn) / (1f - settleVisualReturn)
            }
            lastFollowPose = releaseFollowPose
                .settle(weight)
                // 冲量从零建立再归零，不在释放首帧直接叠加旋转。
                .withReleaseImpulse(settle.releaseVelocity, weight)
        }
        return lastFollowPose
    }

    private fun automaticFollowPose(transform: MiuixVideoCardGestureTransform) = VideoCardFollowPose(
        translation = androidx.compose.ui.geometry.Offset(transform.translationX, transform.translationY),
        rotationZ = transform.rotationZ,
        transformOrigin = automaticOrigin,
        rotationX = transform.rotationX,
        rotationY = transform.rotationY,
        liftScale = transform.liftScale,
        cameraDistance = transform.cameraDistance,
    )

    fun bind(scope: NavTransitionScope) {
        when (scope.role) {
            NavRole.Incoming,
            NavRole.Outgoing,
            -> topScope = scope
            // At rest, pop supplies a new scope whose isRemoving flag is true while depth
            // is still zero. Keeping the old Top scope would classify the subsequent negative
            // depth as Incoming for the whole return. Do retain a moving scope when the lower
            // page becomes Top at landing, until its outgoing driver has reported Idle.
            NavRole.Top -> if (topScope == null ||
                topScope?.role == NavRole.Top || topScope?.role == NavRole.Covered
            ) {
                topScope = scope
            }
            NavRole.Covered -> Unit
        }
    }

    /** Observe the ordinary slide too, so related source metadata survives its entire pop. */
    fun observe(transition: NavTransition): NavTransition = object : NavTransition by transition {
        override fun Modifier.transformEntry(scope: NavTransitionScope): Modifier {
            bind(scope)
            return with(transition) { this@transformEntry.transformEntry(scope) }
        }
    }

    fun clear() {
        lastDepthScope = null
        lastDepthSettle = null
        lastDepthGestureProgress = null
        lastDepthRelativeDepth = Float.NaN
        lastDepthRole = null
        lastDepthFrame = Long.MIN_VALUE
        lastDepthValue = 0f
        topScope = null
        gestureStartReturn = null
        activeSettle = null
        followSequence = null
        followSettle = null
        lastFollowPose = neutralFollowPose
        automaticTransform = null
        automaticOrigin = TransformOrigin.Center
    }

    fun depthOr(fallback: Float): Float = topScope
        ?.let(::visualDepthMemoized)
        ?: fallback.coerceIn(0f, 1f)

    // Miuix retains gesture metadata while settling. It is NOT still direct manipulation.
    fun isGestureInProgress(): Boolean = gestureInProgress

    fun depthOrNull(): Float? = topScope?.let(::visualDepthMemoized)

    fun releaseVelocity(): Float = topScope?.settle?.releaseVelocity ?: 0f

    fun settleStateOrNull(): VideoCardTransitionSettleState? = settleState

    private fun resolveSettleState(): VideoCardTransitionSettleState? = topScope?.let { scope ->
        when {
            isCoveredOrRevealedParent(scope) -> VideoCardTransitionSettleState.Held
            scope.settle?.phase == NavSettlePhase.Cancel -> VideoCardTransitionSettleState.CancelRestore
            isGestureInProgress() -> VideoCardTransitionSettleState.InteractiveSeek
            scope.role == NavRole.Outgoing && scope.relativeDepth <= -1f -> VideoCardTransitionSettleState.Idle
            scope.settle?.phase == NavSettlePhase.Commit || scope.role == NavRole.Outgoing ->
                VideoCardTransitionSettleState.AutoReturn
            scope.role == NavRole.Incoming -> VideoCardTransitionSettleState.AutoEnter
            else -> VideoCardTransitionSettleState.Held
        }
    }

    /**
     * Host layout width used by outer morph (`bounds.width / layoutSize.width`).
     * Landing inverse scale must use the same width or land size drifts from the list card.
     */
    fun layoutWidthOr(fallback: Float): Float {
        val w = topScope?.layoutSize?.width?.toFloat() ?: return fallback.coerceAtLeast(1f)
        return w.coerceAtLeast(1f)
    }

    /**
     * Host layout height used by outer morph (`bounds.height / layoutSize.height`).
     * Inverse Y must use this or stacked cover/info drift off the frozen card.
     */
    fun layoutHeightOr(fallback: Float): Float {
        val h = topScope?.layoutSize?.height?.toFloat() ?: return fallback.coerceAtLeast(1f)
        return h.coerceAtLeast(1f)
    }

    /**
     * 预测返回手势进度（0=开始 → 1=完全提交），无手势时为 null。
     * 供预测返回背景模糊（predictiveBackBackgroundEffect）随手势与落地动画平滑消退，避免松手瞬间断档闪烁。
     */
    fun gestureBackProgress(): Float? = topScope?.let { scope ->
        if (!isCoveredOrRevealedParent(scope) && (scope.gesture != null || scope.settle != null)) {
            // 采样同帧已算过的 depth；这是显示路径，不得再推进状态机。
            val morph = visualDepthMemoized(scope)
            (1f - morph).coerceIn(0f, 1f)
        } else {
            null
        }
    }
}

internal fun resolveVideoHeroNavMotion(
    spec: VideoHeroMotionSpec,
    returning: Boolean,
    gestureTranslationEnabled: Boolean = false,
): NavMotion = NavMotion(
    // A gesture can stop at raw depth 0.999. Give the reserved final card flight its own
    // duration even when the navigation driver's remaining distance is below spring tolerance.
    commit = NavSettleSpec.Tween(
        durationMillis = spec.returnDurationMillis,
        easing = spec.returnSpatialSpec,
        matchReleaseVelocity = !spec.reducedMotion,
    ),
    cancel = if (gestureTranslationEnabled) NavSettleSpec.Tween(
        durationMillis = spec.cancelDurationMillis,
        easing = spec.cancelSpatialSpec,
        matchReleaseVelocity = !spec.reducedMotion,
    ) else NavSettleSpec.Spring(
        dampingRatio = VideoHeroMotionTokens.SPRING_DAMPING,
        stiffness = spec.cancelStiffness,
    ),
    programmatic = NavSettleSpec.Tween(
        durationMillis = if (returning) spec.returnDurationMillis else spec.enterDurationMillis,
        easing = if (returning) spec.returnSpatialSpec else spec.enterSpatialSpec,
    ),
)

/**
 * Video-card morph authored directly against Miuix's shared navigation driver.
 *
 * The video entry is transformed from the click-time card rectangle to the navigation host. The
 * same [NavTransitionScope.relativeDepth] drives push, programmatic pop, predictive back, commit,
 * and cancellation, so there is no AndroidX Navigation3 or AnimatedVisibility compatibility path.
 */
internal fun miuixVideoCardNavTransition(
    sourceBounds: Rect?,
    sourceCornerDp: Int?,
    durationMillis: Int,
    fallback: NavTransition,
    progress: MiuixVideoCardTransitionProgress,
    contentScale: MiuixVideoCardContentScale = MiuixVideoCardContentScale.FillWidthTop,
    gestureFollowEnabled: Boolean = true,
    gestureTranslationEnabled: Boolean = false,
    heroMotionSpec: VideoHeroMotionSpec = resolveVideoHeroMotionSpec(durationMillis),
    returningProvider: () -> Boolean = { false },
    deviceCornerDp: Dp = 32.dp,
    sourceLayout: VideoCardSourceLayout = VideoCardSourceLayout.COVER_ONLY,
    sourceRoute: String? = null,
    /**
     * 宿主自己那份开合时钟（0 = 停在来源卡片上，1 = 整屏）。
     *
     * 导航驱动的行程从压栈开始算，而详情页首帧要一两百毫秒才画得出来——只跟导航驱动，
     * 用户看到的就是「点一下，然后整屏页面直接出现」。这里取两者较小值：页面还没画出来
     * 时进度停在 0（屏幕上就是那张卡），画出来之后由宿主的帧时钟把行程走完。
     */
    morphProvider: () -> Float = { 1f },
): NavTransition {
    val bounds = sourceBounds?.takeIf { it.width > 1f && it.height > 1f }
        ?: return fallback
    val enterMotion = resolveVideoHeroNavMotion(
        heroMotionSpec, returning = false, gestureTranslationEnabled = gestureTranslationEnabled,
    )
    val returnMotion = resolveVideoHeroNavMotion(
        heroMotionSpec, returning = true, gestureTranslationEnabled = gestureTranslationEnabled,
    )
    val corner = sourceCornerDp?.coerceAtLeast(0) ?: 16

    return object : NavTransition {
        override val opaqueDepth: Float = fallback.opaqueDepth
        override val motion: NavMotion get() = if (returningProvider()) returnMotion else enterMotion

        // Source-page scrim and blur are rendered by the existing depth layer from this same
        // transition's deferred progress. Do not add Miuix's generic dim on top of it.
        override fun scrimFraction(scope: NavTransitionScope): Float = 0f

        override fun Modifier.transformEntry(scope: NavTransitionScope): Modifier {
            progress.bind(scope)
            val floatingCornerPx = with(scope.density) {
                val devicePx = deviceCornerDp.toPx()
                if (devicePx > 0f) devicePx else MIUIX_VIDEO_CARD_FLOATING_CORNER_DP.dp.toPx()
            }
            val gestureModifier = if (gestureTranslationEnabled) {
                Modifier.graphicsLayer {
                    val depth = scope.relativeDepth
                    val gesture = scope.gesture
                    if (depth <= 0f && gesture != null) {
                        val width = scope.layoutSize.width.toFloat().coerceAtLeast(1f)
                        val height = scope.layoutSize.height.toFloat().coerceAtLeast(1f)
                        val pose = progress.followPose(
                            scope, bounds, width, height, 0f, heroMotionSpec,
                            preserveViewport = false,
                        )
                        if (pose != null) {
                            transformOrigin = pose.transformOrigin
                            translationX = pose.translation.x
                            translationY = pose.translation.y
                            rotationZ = pose.rotationZ
                            rotationX = pose.rotationX
                            rotationY = pose.rotationY
                            scaleX = pose.liftScale
                            scaleY = pose.liftScale
                            cameraDistance = pose.cameraDistance
                        }
                    }
                }
            } else {
                Modifier
            }
            // 卡片展开：整页从**被点的那张卡的矩形**长到整屏，返回就是倒放。
            //
            // 屏幕上只有这一层在动：等比缩放 + 位移 + 可见窗口，几何见
            // [resolveMiuixVideoCardMorphFrame]。页面内部（播放器、标题、评论）全部按
            // 整屏布局，跟着这一层一起放大缩小，所以不会出现「播放器先展开、信息区后
            // 跟上」那种两段式，也不会有任何一处被压扁。
            //
            // 被覆盖的首页/列表什么都不做：不缩放、不建层、不改不透明度。底下那张真卡
            // 就在原位画着，页面从它上面长出来时把它盖住——开是长出来，合是缩回去。
            return gestureModifier
                .graphicsLayer {
                    if (scope.relativeDepth > 0f) return@graphicsLayer
                    val width = scope.layoutSize.width.toFloat().coerceAtLeast(1f)
                    val height = scope.layoutSize.height.toFloat().coerceAtLeast(1f)
                    val navDepth = progress.visualDepthMemoized(scope).coerceIn(0f, 1f)
                    // 手势跟手时手指就是时钟；自动开合取两者较小值，免得页面首帧还没画
                    // 出来，导航就已经把行程走完。
                    val raw = if (scope.gesture != null) navDepth else minOf(navDepth, morphProvider())
                    val morph = MIUIX_VIDEO_CARD_MORPH_EASING.transform(raw)
                    val frame = resolveMiuixVideoCardMorphFrame(
                        bounds = bounds,
                        layoutWidth = width,
                        layoutHeight = height,
                        morphProgress = morph,
                        contentTopInsetPx = progress.contentTopInsetPx,
                    )
                    transformOrigin = TransformOrigin(0f, 0f)
                    translationX = frame.translationX
                    translationY = frame.translationY
                    scaleX = frame.scale
                    scaleY = frame.scale
                    alpha = resolveMiuixVideoCardMorphAlpha(morph)
                    shadowElevation = 0f
                    if (morph < 0.999f) {
                        clip = true
                        shape = MiuixVideoCardMorphShape(
                            visibleTopPx = frame.visibleTopPx,
                            visibleHeightPx = frame.visibleHeightPx,
                            radiusPx = resolveMiuixVideoCardLinearCornerPx(
                                sourceCornerPx = corner.dp.toPx(),
                                fullscreenCornerPx = floatingCornerPx,
                                morphProgress = morph,
                                sourceLayout = sourceLayout,
                                currentShortSidePx = frame.visibleHeightPx * frame.scale,
                            ) / frame.scale.coerceAtLeast(0.0001f),
                        )
                    } else {
                        clip = false
                        shape = androidx.compose.ui.graphics.RectangleShape
                    }
                }
                .zIndex(1f)
        }
    }
}
