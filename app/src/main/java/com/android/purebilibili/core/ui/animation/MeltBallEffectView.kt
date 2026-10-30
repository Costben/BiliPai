package com.android.purebilibili.core.ui.animation

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowInsets
import android.view.animation.PathInterpolator
import kotlin.math.min

/** 收起动画的落点：底栏胶囊，或屏幕侧边。 */
internal enum class MeltBallMergeTarget {
    BOTTOM_BAR,
    SCREEN_EDGE,
}

/** 收起动画覆盖层的统一句柄：粒子与绒球都只需要一个 dispose。 */
internal interface OverlayEffectView {
    fun dispose()
}

/**
 * 把一块内容快照捏成一颗绒球并吸进目标位置：先快速缩成圆球，再沿路径滑入
 * 底栏 / 屏幕侧边，末段加模糊、被拉开一条细颈后淡出。
 *
 * 与 ThanosEffectView 一样挂到 decorView 上，保证覆盖层不被列表或横条裁剪。
 */
internal class MeltBallEffectView private constructor(
    context: Context,
    private val bitmap: Bitmap,
    private val sourceBounds: RectF,
    private val mergeTarget: MeltBallMergeTarget,
    private var onFirstFrame: (() -> Unit)?,
    private var onComplete: (() -> Unit)?,
) : View(context), OverlayEffectView {
    private val density = resources.displayMetrics.density
    private val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
    private val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 0.9f * density
        color = 0x55FFFFFF
    }
    private val destination = RectF()
    private val clipPath = Path()
    private val origin = RectF()
    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 560L
        // COUI 的收起曲线：先快后慢，末段几乎停住，像是被目标吸住。
        interpolator = PathInterpolator(0.22f, 0f, 0f, 1f)
        addUpdateListener {
            progress = it.animatedValue as Float
            applyFuzz()
            invalidate()
        }
        addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                if (disposed) return
                onComplete?.invoke()
                dispose()
            }
        })
    }
    private var progress = 0f
    private var firstFrameDelivered = false
    private var disposed = false
    private var started = false

    init {
        isOpaque = false
        isClickable = false
        isFocusable = false
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (started) return
        started = true
        post { if (!disposed) animator.start() }
    }

    override fun onDraw(canvas: Canvas) {
        if (disposed) return
        val p = progress.coerceIn(0f, 1f)
        val travel = smootherStep(p)
        val morph = smootherStep((p / 0.62f).coerceIn(0f, 1f))

        val targetDiameter = 26f * density
        val width = lerp(sourceBounds.width(), targetDiameter, morph)
        val height = lerp(sourceBounds.height(), targetDiameter, morph)
        val radius = min(width, height) / 2f

        val targetX: Float
        val targetY: Float
        when (mergeTarget) {
            MeltBallMergeTarget.BOTTOM_BAR -> {
                targetX = this.width / 2f
                targetY = this.height - navigationBarInset() - 34f * density
            }
            MeltBallMergeTarget.SCREEN_EDGE -> {
                targetX = this.width + radius * 0.9f
                targetY = sourceBounds.centerY()
            }
        }
        val centerX = lerp(sourceBounds.centerX(), targetX, travel)
        val centerY = lerp(sourceBounds.centerY(), targetY, travel)

        // 末段压扁一点，像撞进目标后被吸走。
        val squash = if (p > 0.82f) 1f - 0.22f * ((p - 0.82f) / 0.18f) else 1f
        val drawWidth = width
        val drawHeight = height * squash
        destination.set(
            centerX - drawWidth / 2f,
            centerY - drawHeight / 2f,
            centerX + drawWidth / 2f,
            centerY + drawHeight / 2f,
        )

        val fade = if (p > 0.8f) ((1f - p) / 0.2f).coerceIn(0f, 1f) else 1f
        val drawAlpha = (fade * 255f).toInt()
        paint.alpha = drawAlpha
        haloPaint.alpha = drawAlpha
        // Paint.setAlpha 会覆盖颜色自带透明度，描边这里按原始 0x55 再乘整体淡出。
        rimPaint.alpha = (0x55 * fade).toInt()

        drawHalo(canvas, centerX, centerY, radius, p)
        drawNeck(canvas, centerX, centerY, radius, targetX, targetY, p)

        val corner = lerp(min(radius * 1.2f, 14f * density), radius, morph)
        clipPath.reset()
        clipPath.addRoundRect(destination, corner, corner, Path.Direction.CW)
        val checkpoint = canvas.save()
        canvas.clipPath(clipPath)
        origin.set(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
        canvas.drawBitmap(bitmap, origin, destination, paint)
        canvas.restoreToCount(checkpoint)

        canvas.drawCircle(centerX, centerY, radius - rimPaint.strokeWidth / 2f, rimPaint)

        if (!firstFrameDelivered) {
            firstFrameDelivered = true
            post { if (!disposed) onFirstFrame?.invoke() }
        }
    }

    private fun drawHalo(canvas: Canvas, centerX: Float, centerY: Float, radius: Float, p: Float) {
        if (radius <= 0f) return
        val haloRadius = radius * (1.55f + 0.35f * p)
        haloPaint.shader = RadialGradient(
            centerX,
            centerY,
            haloRadius,
            intArrayOf(0x33FFFFFF, 0x14FFFFFF, 0x00FFFFFF),
            floatArrayOf(0f, 0.55f, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.drawCircle(centerX, centerY, haloRadius, haloPaint)
        haloPaint.shader = null
    }

    /** 被目标吸住前拉出的一条细颈，模糊后与球体自然融在一起。 */
    private fun drawNeck(
        canvas: Canvas,
        centerX: Float,
        centerY: Float,
        radius: Float,
        targetX: Float,
        targetY: Float,
        p: Float,
    ) {
        if (p < 0.62f || radius <= 0f) return
        val strength = ((p - 0.62f) / 0.38f).coerceIn(0f, 1f)
        val neckX = lerp(centerX, targetX, 0.42f + 0.28f * strength)
        val neckY = lerp(centerY, targetY, 0.42f + 0.28f * strength)
        val neckRadius = radius * (0.62f - 0.18f * strength)
        haloPaint.shader = RadialGradient(
            neckX,
            neckY,
            neckRadius,
            intArrayOf(0x40FFFFFF, 0x00FFFFFF),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.drawCircle(neckX, neckY, neckRadius, haloPaint)
        val linear = LinearGradient(
            centerX, centerY, neckX, neckY,
            intArrayOf(0x30FFFFFF, 0x00FFFFFF),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP,
        )
        haloPaint.shader = linear
        canvas.drawRect(
            min(centerX, neckX) - radius * 0.2f,
            min(centerY, neckY) - radius * 0.2f,
            maxOf(centerX, neckX) + radius * 0.2f,
            maxOf(centerY, neckY) + radius * 0.2f,
            haloPaint,
        )
        haloPaint.shader = null
    }

    /** 末段把整层柔化，球体边缘像绒毛一样散开。 */
    private fun applyFuzz() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        val p = progress.coerceIn(0f, 1f)
        val blurDp = if (p < 0.42f) 0f else ((p - 0.42f) / 0.58f) * 9f
        renderEffect = if (blurDp <= 0.05f) {
            null
        } else {
            RenderEffect.createBlurEffect(
                blurDp * density,
                blurDp * density,
                Shader.TileMode.CLAMP,
            )
        }
    }

    private fun navigationBarInset(): Int {
        val insets = rootWindowInsets ?: return 0
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            insets.getInsets(WindowInsets.Type.navigationBars()).bottom
        } else {
            @Suppress("DEPRECATION")
            insets.systemWindowInsets.bottom
        }
    }

    override fun dispose() {
        if (disposed) return
        disposed = true
        animator.cancel()
        onFirstFrame = null
        onComplete = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            renderEffect = null
        }
        (parent as? ViewGroup)?.removeView(this)
        if (!bitmap.isRecycled) bitmap.recycle()
    }

    companion object {
        /** Takes ownership of [bitmap] when a view is returned. */
        fun attach(
            window: Window,
            bitmap: Bitmap,
            windowBounds: RectF,
            mergeTarget: MeltBallMergeTarget,
            onFirstFrame: () -> Unit,
            onComplete: () -> Unit,
        ): MeltBallEffectView? {
            val root = window.decorView as? ViewGroup ?: return null
            if (!root.isAttachedToWindow) return null
            val location = IntArray(2)
            root.getLocationInWindow(location)
            val bounds = RectF(windowBounds).apply {
                offset(-location[0].toFloat(), -location[1].toFloat())
            }
            val view = MeltBallEffectView(
                root.context, bitmap, bounds, mergeTarget, onFirstFrame, onComplete,
            )
            return try {
                root.addView(
                    view,
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    ),
                )
                view
            } catch (error: RuntimeException) {
                Log.w("MeltBallEffect", "Cannot attach melt ball overlay", error)
                view.dispose()
                null
            }
        }
    }
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float = start + (stop - start) * fraction

private fun smootherStep(value: Float): Float {
    val x = value.coerceIn(0f, 1f)
    return x * x * x * (x * (x * 6f - 15f) + 10f)
}
