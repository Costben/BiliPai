package com.android.purebilibili.navigation3.predictiveback

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.Color
import top.yukonga.miuix.kmp.nav.transition.NavTransitions
import com.android.purebilibili.navigation3.resolveRelatedReturnSourceRestoreDecision
import com.android.purebilibili.core.ui.transition.VideoCardTransitionExposure
import com.android.purebilibili.core.ui.transition.VideoCardSourceLayout
import java.io.File
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import com.android.purebilibili.core.ui.transition.resolveVideoHeroMotionSpec
import com.android.purebilibili.core.ui.transition.resolveVideoHeroLandingScale
import com.android.purebilibili.core.ui.transition.VideoCardTransitionSettleState
import top.yukonga.miuix.kmp.nav.transition.NavSettleSpec
import top.yukonga.miuix.kmp.nav.transition.NavTransitionScope
import top.yukonga.miuix.kmp.nav.transition.NavSettle
import top.yukonga.miuix.kmp.nav.transition.NavSettlePhase
import top.yukonga.miuix.kmp.nav.transition.NavGesture
import top.yukonga.miuix.kmp.nav.transition.NavSwipeEdge
import top.yukonga.miuix.kmp.nav.transition.NavRole
import top.yukonga.miuix.kmp.nav.runtime.NavChange
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

class MiuixVideoCardNavTransitionTest {
    @Test
    fun releaseRotationStartsWithoutAJumpAndEndsAtTheLandingPose() {
        val released = VideoCardFollowPose(rotationZ = 2f)
        assertEquals(released, released.withReleaseImpulse(4f, 1f))
        assertEquals(VideoCardFollowPose(), released.settle(0f).withReleaseImpulse(4f, 0f))
        val almostReleased = released.settle(.9999f).withReleaseImpulse(4f, .9999f)
        assertEquals(released.rotationZ, almostReleased.rotationZ, .002f)
        assertTrue(released.settle(.5f).withReleaseImpulse(4f, .5f).rotationZ > released.settle(.5f).rotationZ)
    }

    @Test
    fun nativeViewportContractsWithBodyFadeWithoutASecondHeightStage() {
        for ((sourceX, sourceY) in listOf(.46f to .25f, .92f to .12f, .3f to .08f)) {
            var previousY = sourceY
            for (i in 0..1000) {
                val depth = i / 1000f
                val x = resolveMiuixVideoCardOuterScale(sourceX, depth, 1f)
                val y = resolveMiuixVideoCardOuterScaleY(sourceX, sourceY, depth, 1f)
                val body = com.android.purebilibili.core.ui.transition.resolveVideoCardSecondaryContentVisualFrame(
                    morphDepthProgress = depth,
                    phase = com.android.purebilibili.core.ui.transition.VideoCardTransitionBackgroundPhase.RETURNING,
                    isReturnGestureInProgress = true,
                    motionTier = com.android.purebilibili.core.ui.adaptive.MotionTier.Normal,
                    preserveNativeViewport = true,
                )
                val cardY = x * sourceY / sourceX
                assertTrue(y >= cardY - .0001f)
                assertTrue(y <= x + .0001f)
                // Full content keeps the viewport; as it fades, contraction must already
                // be underway rather than waiting for an empty page to shrink again.
                if (body.alpha >= .9999f) assertEquals(x, y, .0001f)
                if (body.alpha in .1f..0.9f) {
                    assertTrue(y < x)
                    assertTrue(y > cardY)
                }
                assertTrue(y >= previousY - .0001f)
                assertTrue(y <= 1.0001f)
                previousY = y
            }
            assertEquals(sourceY, resolveMiuixVideoCardOuterScaleY(sourceX, sourceY, 0f, 1f), .0001f)
            assertEquals(1f, resolveMiuixVideoCardOuterScaleY(sourceX, sourceY, 1f, 1f), .0001f)
        }
    }

    @Test
    fun relatedVideoBackingDoesNotExposeWhiteThemeSurfaceAtTheWindowEdges() {
        assertEquals(Color.Black, resolveMiuixVideoCardBackingColor("video/BV123", Color.White))
        assertEquals(Color.Black, resolveMiuixVideoCardBackingColor("video/BV123?cid=10", Color.White))
        assertEquals(Color.White, resolveMiuixVideoCardBackingColor("home", Color.White))
        assertEquals(Color.White, resolveMiuixVideoCardBackingColor(null, Color.White))
    }


    @Test
    fun coveredPageDepthIsClampedReversibleAndDisabledForReducedMotion() {
        assertEquals(1f, resolveMiuixVideoCardCoveredScale(-1f, false))
        assertEquals(1f, resolveMiuixVideoCardCoveredScale(0f, false))
        assertEquals(0.985f, resolveMiuixVideoCardCoveredScale(0.5f, false), 0.0001f)
        assertEquals(0.97f, resolveMiuixVideoCardCoveredScale(1f, false), 0.0001f)
        assertEquals(0.97f, resolveMiuixVideoCardCoveredScale(2f, false), 0.0001f)
        // An interrupted return reuses its current depth; cancel restores the same path.
        for (depth in listOf(1f, 0.6f, 0.3f, 0.6f, 1f)) {
            assertEquals(1f - 0.03f * depth, resolveMiuixVideoCardCoveredScale(depth, false), 0.0001f)
            assertEquals(1f, resolveMiuixVideoCardCoveredScale(depth, true))
        }
    }

    @Test
    fun coveredBackgroundCornersGrowWithScaleAndReturnWithoutAFirstFrameJump() {
        for (density in listOf(1f, 1.5f, 3f)) {
            assertEquals(0f, resolveMiuixVideoCardCoveredCornerPx(0f, false, density))
            assertTrue(resolveMiuixVideoCardCoveredCornerPx(.001f, false, density) < .1f)
            for (depth in listOf(0f, .25f, .6f, 1f, .6f, .25f, 0f)) {
                val scale = resolveMiuixVideoCardCoveredScale(depth, false)
                val radius = resolveMiuixVideoCardCoveredCornerPx(depth, false, density)
                // 验证屏幕坐标圆角；冻结 Host 和真实来源页共用同一几何函数。
                assertEquals(24f * density * depth, radius * scale, .0001f)
                assertEquals(0f, resolveMiuixVideoCardCoveredCornerPx(depth, true, density))
            }
        }
        val cache = MiuixVideoCardCoveredShapeCache()
        val first = cache.resolve(4f)
        assertTrue(first === cache.resolve(4f))
        val second = cache.resolve(8f)
        assertTrue(first !== second)
        assertTrue(first === cache.resolve(12f))
    }
    @Test
    fun ordinaryReturnTracksDepthAndRoleChangesWithinOneFrame() {
        val scope = object : NavTransitionScope {
            override var relativeDepth by mutableFloatStateOf(-.4f)
            override var role by mutableStateOf(NavRole.Outgoing)
            override val change = NavChange.Pop
            override val layoutSize = IntSize(1080, 2400)
            override val layoutDirection = LayoutDirection.Ltr
            override val density = Density(3f)
            override val gesture: NavGesture? = null
            override val settle: NavSettle? = null
        }
        val progress = MiuixVideoCardTransitionProgress()
        progress.bind(scope)
        assertEquals(.6f, progress.depthOrNull()!!, absoluteTolerance = .001f)
        assertEquals(.6f, progress.depthOrNull()!!, absoluteTolerance = .001f)
        scope.relativeDepth = -.8f
        assertEquals(.2f, progress.depthOrNull()!!, absoluteTolerance = .001f)
        scope.relativeDepth = 0f
        assertEquals(1f, progress.depthOrNull())
        scope.role = NavRole.Top
        assertEquals(1f, progress.depthOrNull())
        progress.clear()
        assertEquals(null, progress.depthOrNull())
        scope.role = NavRole.Outgoing
        scope.relativeDepth = -.3f
        progress.bind(scope)
        assertEquals(.7f, progress.depthOrNull()!!, absoluteTolerance = .001f)
    }

    @Test
    fun childPredictiveBackKeepsCoveredVideoFullscreenThroughCommitAndCancel() {
        val scope = object : NavTransitionScope {
            override var relativeDepth by mutableFloatStateOf(0f)
            override val role get() = if (relativeDepth > 0f) NavRole.Covered else NavRole.Top
            override val change = NavChange.Pop
            override val layoutSize = IntSize(1080, 2400)
            override val layoutDirection = LayoutDirection.Ltr
            override val density = Density(3f)
            override var gesture: NavGesture? by mutableStateOf(null)
            override var settle: NavSettle? by mutableStateOf(null)
        }
        val progress = MiuixVideoCardTransitionProgress()
        // The retained video scope is already bound before opening BGM.
        progress.bind(scope)
        for (releasePhase in listOf(NavSettlePhase.Commit, NavSettlePhase.Cancel)) {
            scope.settle = null
            for (fraction in listOf(0f, .2f, .7f, .999f)) {
                scope.relativeDepth = 1f - fraction
                scope.gesture = NavGesture(fraction, NavSwipeEdge.Left, 500f)
                assertEquals(1f, progress.depthOrNull())
                assertEquals(VideoCardTransitionSettleState.Held, progress.settleStateOrNull())
                assertEquals(false, progress.isGestureInProgress())
                assertEquals(null, progress.gestureBackProgress())
            }
            scope.settle = object : NavSettle {
                override val phase = releasePhase
                override val releaseVelocity = 0f
                override val elapsedMillis = 0f
            }
            // Commit reveals the parent; cancel keeps it covered. Neither is a card return.
            scope.relativeDepth = if (releasePhase == NavSettlePhase.Commit) 0f else 1f
            assertEquals(1f, progress.depthOrNull())
            assertEquals(VideoCardTransitionSettleState.Held, progress.settleStateOrNull())
            assertEquals(null, progress.gestureBackProgress())
        }
    }

    @Test
    fun settledEntryRebindsRemovingScopeBeforeReturnStarts() {
        var depth by mutableFloatStateOf(-.5f)
        fun scope(removing: Boolean, lowerPage: Boolean = false) = object : NavTransitionScope {
            override val relativeDepth get() = if (lowerPage) depth + 1f else depth
            override val role get() = when {
                relativeDepth > 0f -> NavRole.Covered
                relativeDepth == 0f -> NavRole.Top
                removing -> NavRole.Outgoing
                else -> NavRole.Incoming
            }
            override val change = if (removing) NavChange.Pop else NavChange.Push
            override val layoutSize = IntSize(1080, 2400)
            override val layoutDirection = LayoutDirection.Ltr
            override val density = Density(3f)
            override val gesture: NavGesture? = null
            override val settle: NavSettle? = null
        }
        val progress = MiuixVideoCardTransitionProgress()
        progress.bind(scope(removing = false))
        assertEquals(VideoCardTransitionSettleState.AutoEnter, progress.settleStateOrNull())
        depth = 0f
        assertEquals(VideoCardTransitionSettleState.Held, progress.settleStateOrNull())

        // Miuix captures isRemoving in a fresh scope before its driver leaves depth zero.
        progress.bind(scope(removing = true))
        depth = -.4f
        progress.bind(scope(removing = false, lowerPage = true))
        assertEquals(VideoCardTransitionSettleState.AutoReturn, progress.settleStateOrNull())
        assertEquals(.6f, progress.depthOrNull())

        // The source reaches Top in the final frame; it must not steal exit completion.
        depth = -1f
        progress.bind(scope(removing = false, lowerPage = true))
        assertEquals(VideoCardTransitionSettleState.Idle, progress.settleStateOrNull())
        assertEquals(0f, progress.depthOrNull())
    }

    @Test
    fun heroNavMotionReservesTimeForCommittedCardLanding() {
        val spec = resolveVideoHeroMotionSpec(360)
        val entering = resolveVideoHeroNavMotion(spec, false)
        val returning = resolveVideoHeroNavMotion(spec, true)
        assertEquals(360, (entering.programmatic as NavSettleSpec.Tween).durationMillis)
        assertEquals(299, (returning.programmatic as NavSettleSpec.Tween).durationMillis)
        assertEquals(spec.returnSpatialSpec, (returning.programmatic as NavSettleSpec.Tween).easing)
        assertEquals(spec.enterSpatialSpec, (entering.programmatic as NavSettleSpec.Tween).easing)
        assertEquals(spec.returnDurationMillis, (returning.commit as NavSettleSpec.Tween).durationMillis)
        assertTrue((returning.commit as NavSettleSpec.Tween).matchReleaseVelocity)
        assertEquals(spec.cancelStiffness, (returning.cancel as NavSettleSpec.Spring).stiffness)
        val follow = resolveVideoHeroNavMotion(spec, true, gestureTranslationEnabled = true)
        assertTrue((follow.cancel as NavSettleSpec.Tween).matchReleaseVelocity)
        assertEquals(spec.cancelSpatialSpec, (follow.cancel as NavSettleSpec.Tween).easing)
        val reduced = resolveVideoHeroNavMotion(resolveVideoHeroMotionSpec(360, reducedMotion = true), true)
        assertTrue(!(reduced.commit as NavSettleSpec.Tween).matchReleaseVelocity)
        assertTrue(!(returning.programmatic as NavSettleSpec.Tween).matchReleaseVelocity)
    }

    @Test
    fun fullGestureLandsManuallyAndCommitHasNoSecondFlight() {
        val scope = object : NavTransitionScope {
            override var relativeDepth by mutableFloatStateOf(0f)
            override var role by mutableStateOf(NavRole.Top)
            override val change = NavChange.Pop
            override val layoutSize = IntSize(1080, 2400)
            override val layoutDirection = LayoutDirection.Ltr
            override val density = Density(3f)
            override var gesture: NavGesture? by mutableStateOf(NavGesture(0f, NavSwipeEdge.Left, 500f))
            override var settle: NavSettle? by mutableStateOf(null)
        }
        val progress = MiuixVideoCardTransitionProgress()
        progress.bind(scope)
        assertEquals(1f, progress.depthOrNull())
        scope.relativeDepth = -.999f
        scope.gesture = NavGesture(.999f, NavSwipeEdge.Left, 500f)
        // Manual drag reaches (almost) full landing; the last sliver waits for the driver.
        assertEquals(.001f, progress.depthOrNull()!!, absoluteTolerance = .002f)
        scope.settle = object : NavSettle {
            override val phase = NavSettlePhase.Commit
            override val releaseVelocity = 0f
            override val elapsedMillis = 0f
        }
        scope.role = NavRole.Outgoing
        // Commit at a fully-dragged release has no remaining distance: no second flight.
        assertEquals(.001f, progress.depthOrNull()!!, absoluteTolerance = .002f)
        scope.relativeDepth = -.9995f
        assertEquals(.0005f, progress.depthOrNull()!!, absoluteTolerance = .002f)
        scope.relativeDepth = -1f
        assertEquals(0f, progress.depthOrNull())
    }

    @Test
    fun interruptingEntryRetainsItsCurrentPositionAndMapsGestureOneToOne() {
        val scope = object : NavTransitionScope {
            override var relativeDepth by mutableFloatStateOf(-.5f)
            override val role = NavRole.Incoming
            override val change = NavChange.Push
            override val layoutSize = IntSize(1080, 2400)
            override val layoutDirection = LayoutDirection.Ltr
            override val density = Density(3f)
            override var gesture: NavGesture? by mutableStateOf(NavGesture(0f, NavSwipeEdge.Left, 500f))
            override val settle: NavSettle? = null
        }
        val progress = MiuixVideoCardTransitionProgress()
        progress.bind(scope)
        assertEquals(.5f, progress.depthOrNull())
        scope.relativeDepth = -.9f
        scope.gesture = NavGesture(.4f, NavSwipeEdge.Left, 500f)
        // Interruption keeps the pre-gesture position and gesture progress maps 1:1.
        assertEquals(.1f, progress.depthOrNull()!!, absoluteTolerance = .001f)
        // The first gesture frame must stay at the current position.
        val earlyScope = object : NavTransitionScope by scope {
            override val relativeDepth = -.9f
            override val gesture = NavGesture(0f, NavSwipeEdge.Left, 500f)
        }
        val earlyProgress = MiuixVideoCardTransitionProgress()
        earlyProgress.bind(earlyScope)
        assertEquals(.1f, earlyProgress.depthOrNull()!!, absoluteTolerance = .001f)
    }

    @Test
    fun cancelledGestureReturnsToFullscreenWithoutJumpingAtRelease() {
        val scope = object : NavTransitionScope {
            override var relativeDepth by mutableFloatStateOf(-.6f)
            override val role = NavRole.Incoming
            override val change = NavChange.Pop
            override val layoutSize = IntSize(1080, 2400)
            override val layoutDirection = LayoutDirection.Ltr
            override val density = Density(3f)
            override var gesture: NavGesture? by mutableStateOf(NavGesture(.6f, NavSwipeEdge.Left, 500f))
            override var settle: NavSettle? by mutableStateOf(null)
        }
        val progress = MiuixVideoCardTransitionProgress()
        progress.bind(scope)
        val released = progress.depthOrNull()!!
        scope.settle = object : NavSettle {
            override val phase = NavSettlePhase.Cancel
            override val releaseVelocity = 0f
            override val elapsedMillis = 0f
        }
        assertEquals(released, progress.depthOrNull())
        scope.relativeDepth = -.3f
        assertTrue(progress.depthOrNull()!! > released)
        scope.relativeDepth = 0f
        assertEquals(1f, progress.depthOrNull())
    }

    @Test
    fun sourcePixelInverseScaleIsIndependentOfMorphDepth() {
        // Native chrome can measure once: FillWidthTop cancels the outer morph on BOTH
        // axes, including highly non-uniform horizontal cards and interrupted returns.
        for ((sourceX, sourceY) in listOf(.01f to .01f, .46f to .25f, .92f to .12f, 1f to .08f)) {
            val frozen = resolveMiuixVideoCardInverseScaleForDepth(sourceX, sourceY, 0f)
            for (autoReturning in listOf(false, true)) {
                for (step in 0..100) {
                    val live = resolveMiuixVideoCardInverseScaleForDepth(
                        sourceX, sourceY, step / 100f, autoReturning,
                    )
                    assertEquals(frozen.scaleX, live.scaleX, .0001f)
                    assertEquals(frozen.scaleY, live.scaleY, .0001f)
                    assertEquals(1f / sourceX, live.scaleX, .0001f)
                }
            }
        }
    }

    @Test
    fun reconstructedCardPixelsStayUndistortedAndFitTheShellAtHandoff() {
        for ((sourceX, sourceY) in listOf(.46f to .25f, .92f to .12f, .3f to .08f)) {
            for (autoReturning in listOf(false, true)) {
                for (step in 0..100) {
                    val depth = step / 100f
                    val landing = com.android.purebilibili.core.ui.transition
                        .resolveVideoHeroLandingScale(depth, autoReturning)
                    val x = resolveMiuixVideoCardOuterScale(sourceX, depth, landing)
                    val y = resolveMiuixVideoCardOuterScaleY(sourceX, sourceY, depth, landing)
                    val compensation = resolveMiuixVideoCardContentCompensation(
                        outerScaleX = x,
                        outerScaleY = y,
                        contentScale = MiuixVideoCardContentScale.FillWidthTop,
                    )
                    val inverse = resolveMiuixVideoCardInverseScaleForDepth(
                        sourceX, sourceY, depth, autoReturning,
                    )
                    val pixelX = inverse.scaleX * compensation.scaleX * x
                    val pixelY = inverse.scaleY * compensation.scaleY * y
                    // Compose the actual nested layers: equal screen-space scaling
                    // preserves text glyphs and cover aspect ratio throughout the return.
                    assertEquals(pixelX, pixelY, .0001f)
                    if (depth <= .18f) {
                        // Once source chrome appears, its full card height fills the clip.
                        // No residual detail-page height may remain as a blank band.
                        assertEquals(y, sourceY * pixelY, .0001f)
                    }
                }
            }
        }
    }

    @Test
    fun landingCompressionIsBoundedRelativeToFinalSizeOnBothAxes() {
        for (sourceScale in listOf(.05f, .2f, .8f, 1f)) {
            for (i in 0..1000) {
                val depth = i / 1000f
                val scale = resolveMiuixVideoCardOuterScale(sourceScale, depth,
                    resolveVideoHeroLandingScale(depth, true))
                val baseline = resolveMiuixVideoCardOuterScale(sourceScale, depth, 1f)
                assertTrue(abs(scale - baseline) <= .015f * sourceScale)
            }
            assertEquals(sourceScale, resolveMiuixVideoCardOuterScale(sourceScale, 0f, 1f))
            assertEquals(1f, resolveMiuixVideoCardOuterScale(sourceScale, 1f, 1f))
        }
    }

    @Test
    fun ordinaryRelatedSlideKeepsSourceUntilItsActualExitCompletes() {
        val scope = object : NavTransitionScope {
            override var relativeDepth by mutableFloatStateOf(-.4f)
            override var role by mutableStateOf(NavRole.Outgoing)
            override val change = NavChange.Pop
            override val layoutSize = IntSize(1080, 2400)
            override val layoutDirection = LayoutDirection.Ltr
            override val density = Density(3f)
            override val gesture: NavGesture? = null
            override val settle: NavSettle? = null
        }
        val progress = MiuixVideoCardTransitionProgress()
        val fallback = NavTransitions.MiuixDefault
        val observed = progress.observe(fallback)
        with(observed) { Modifier.transformEntry(scope) }
        assertEquals(fallback.motion, observed.motion)
        assertEquals(fallback.opaqueDepth, observed.opaqueDepth)
        assertEquals(VideoCardTransitionSettleState.AutoReturn, progress.settleStateOrNull())
        val moving = resolveRelatedReturnSourceRestoreDecision(
            restorePending = true,
            transitionObserved = false,
            transitionAnimated = true,
            exposure = VideoCardTransitionExposure.Returning,
        )
        assertEquals(false, moving.shouldRestore)
        scope.relativeDepth = -1f
        assertEquals(VideoCardTransitionSettleState.Idle, progress.settleStateOrNull())
        val finished = resolveRelatedReturnSourceRestoreDecision(
            restorePending = true,
            transitionObserved = moving.transitionObserved,
            transitionAnimated = true,
            exposure = VideoCardTransitionExposure.Idle,
        )
        assertTrue(finished.shouldRestore)
    }

    @Test
    fun retainedGestureMetadataDoesNotMisclassifyCommitOrCancelAsSeek() {
        val scope = object : NavTransitionScope {
            override var relativeDepth by mutableFloatStateOf(-.4f)
            override var role by mutableStateOf(NavRole.Top)
            override val change = NavChange.Pop
            override val layoutSize = IntSize(1080, 2400)
            override val layoutDirection = LayoutDirection.Ltr
            override val density = Density(3f)
            override val gesture = NavGesture(.4f, NavSwipeEdge.Left, 500f)
            override var settle: NavSettle? by mutableStateOf(null)
        }
        val progress = MiuixVideoCardTransitionProgress()
        progress.bind(scope)
        assertTrue(progress.isGestureInProgress())
        assertEquals(.6f, progress.depthOrNull()!!, absoluteTolerance = .001f)
        scope.settle = object : NavSettle {
            override val phase = NavSettlePhase.Cancel
            override val releaseVelocity = 0f
            override val elapsedMillis = 0f
        }
        assertEquals(false, progress.isGestureInProgress())
        assertEquals(VideoCardTransitionSettleState.CancelRestore, progress.settleStateOrNull())
        scope.settle = object : NavSettle {
            override val phase = NavSettlePhase.Commit
            override val releaseVelocity = 2f
            override val elapsedMillis = 0f
        }
        scope.role = NavRole.Outgoing
        assertEquals(VideoCardTransitionSettleState.AutoReturn, progress.settleStateOrNull())
        scope.relativeDepth = -1f
        assertEquals(VideoCardTransitionSettleState.Idle, progress.settleStateOrNull())

        // The same live presentation subsequently drives a settings navigation. A released
        // video scope must not turn that unrelated transition back into video depth/blur.
        progress.clear()
        scope.relativeDepth = -.2f
        scope.role = NavRole.Incoming
        assertEquals(null, progress.depthOrNull())
        assertEquals(null, progress.settleStateOrNull())
        assertEquals(null, progress.gestureBackProgress())
        assertEquals(false, progress.isGestureInProgress())
        assertEquals(0f, progress.depthOr(0f))

        progress.bind(scope)
        assertEquals(.8f, progress.depthOrNull())
    }
    @Test
    fun thePageGrowsOutOfTheSourceCardWithOneUniformScale() {
        val source = File(
            "src/main/java/com/android/purebilibili/navigation3/predictiveback/MiuixVideoCardNavTransition.kt"
        ).readText()
        val transform = source.substringAfter("override fun Modifier.transformEntry")

        // 几何只有一条：整页从卡片矩形长到整屏，等比缩放 + 位移 + 可见窗口。
        assertTrue(transform.contains("resolveMiuixVideoCardMorphFrame("))
        assertTrue(transform.contains("scaleX = frame.scale"))
        assertTrue(transform.contains("scaleY = frame.scale"))
        assertTrue(transform.contains("translationX = frame.translationX"))
        assertTrue(transform.contains("translationY = frame.translationY"))
        assertTrue(transform.contains("MiuixVideoCardMorphShape("))
        assertTrue(transform.contains("transformOrigin = TransformOrigin(0f, 0f)"))

        // 两轴必须同一个比例。任何一处单独按 X / Y 缩放，画面都会被压扁或拉长。
        assertFalse(transform.contains("outerScaleX"))
        assertFalse(transform.contains("outerScaleY"))
        assertFalse(transform.contains("uniformScale / outerScaleX"))
        assertFalse(transform.contains("uniformScale / outerScaleY"))
        assertFalse(transform.contains("visibleHeightFraction"))
        assertFalse(transform.contains("resolveMiuixVideoCardCoveredScale("))
        assertFalse(transform.contains("coveredShapeCache"))

        // 被覆盖的首页/列表完全不动：不缩放、不建层、不改不透明度。
        assertTrue(transform.contains("if (scope.relativeDepth > 0f) return@graphicsLayer"))
        // 整条 modifier 链上只剩一层 graphicsLayer。
        val chain = transform.substringAfter("return gestureModifier")
        assertEquals(1, chain.split(".graphicsLayer {").size - 1)
        assertEquals(true, transform.contains("shadowElevation = 0f"))

        // 导航驱动不是唯一时钟：页面首帧还没画出来时，行程停在卡片上。
        assertTrue(transform.contains("minOf(navDepth, morphProvider())"))
        // 手指就是时钟：手势跟手时以导航深度为准。
        assertTrue(transform.contains("if (scope.gesture != null) navDepth else"))
        // 缓动装配在宿主那份线性时钟上。
        assertTrue(source.contains("MIUIX_VIDEO_CARD_MORPH_EASING = CubicBezierEasing("))

        // 页面不是「淡出来的」：可见度是这条行程的一部分。
        assertTrue(transform.contains("alpha = resolveMiuixVideoCardMorphAlpha(morph)"))
    }

    @Test
    fun theMorphRunsOnItsOwnFrameClockInBothDirections() {
        val host = File(
            "src/main/java/com/android/purebilibili/navigation3/BiliPaiNavDisplayHost.kt"
        ).readText()

        // 展开：从这一层**第一帧真正画出来**开始计墙钟。
        //
        // 实测按帧数走会被首帧组合整段吞掉（主线程卡一百多毫秒，帧回调照发不误，
        // 整段行程压成一帧），所以起点必须是「看见」，不是「开始组合」。
        val clock = host.substringAfter("private fun VideoCardMorphClock(")
            .substringBefore("internal const val VIDEO_CARD_MORPH_IN_MS")
        assertTrue(clock.contains("driver.arm(armToken)"))
        // 只在第一次组合时压回 0：两个详情 entry 共用一块时钟，重复 arm 会把新页压小。
        assertTrue(clock.contains("if (!armed[0])"))
        assertTrue(clock.contains("while (firstDrawAt[0] == 0L) withFrameNanos { }"))
        assertTrue(clock.contains("VIDEO_CARD_MORPH_IN_MS"))
        // 逐帧累加 + 单帧限幅：主线程停两百多毫秒时，那一停只能算一帧，
        // 不能把整段行程一次吃掉（否则观感就是「停一下，然后直接换脸」）。
        assertTrue(clock.contains("elapsedMs += deltaMs.coerceIn(0f, VIDEO_CARD_MORPH_MAX_STEP_MS)"))
        assertFalse(clock.contains("System.nanoTime() - startNanos"))
        assertFalse(clock.contains("animateTo("))
        assertFalse(clock.contains("tween("))

        // 时间戳由 entry 的 graphicsLayer 在绘制阶段打，早于本帧内容绘制。
        assertTrue(host.contains("if (entryFirstDrawAt[0] == 0L) {"))
        assertTrue(host.contains("entryFirstDrawAt[0] = System.nanoTime()"))
        assertFalse(host.contains("fadeOutDrawAtNanos"))
        assertFalse(host.contains("VIDEO_CARD_FADE_DRAW_TIMEOUT_MS"))

        // 返回：同一层、同一个世代号，按帧累计推进。
        assertTrue(host.contains("VIDEO_CARD_MORPH_OUT_MS"))
        val performBack = host.substringAfter("val performBack = remember(")
            .substringBefore("DisposableEffect(programmaticBackDispatcher")
        assertTrue(performBack.contains("videoCardMorph.beginReturn()"))
        // 弹出同一帧就起跑：放到弹出之后会白丢一帧，而离场 entry 只活四百多毫秒。
        assertTrue(performBack.indexOf("beginReturn()") < performBack.indexOf("latestOnBack()"))
        assertTrue(performBack.contains("runCardMorphRamp("))
        // 退到另一个详情页时这一页不收缩：那一页自己就是飞行页，几何归它管。
        assertTrue(performBack.contains("val returnsToCardSource = !isCardMorphDestinationNavKey("))

        // 几何在导航层，entry 自己不再整页调 alpha——两处同时改可见度会互相打架。
        val entryBox = host.substringAfter("ProvideMiuixNavViewModelApplicationExtras(application) {")
            .substringBefore("content(key)")
        assertFalse(entryBox.contains("alpha = videoCardMorph.value()"))

        // 飞行页内部一律当作已经全屏：页面自己的缩放、反算、来源卡重建全部歇着。
        assertTrue(host.contains("entryCardTransitionState.copy(progressProvider = { 1f })"))
        assertTrue(host.contains("morphProvider = { videoCardMorph.value() }"))
    }

    @Test
    fun morphFrameLandsExactlyOnTheCardAndOnTheScreen() {
        val width = 1080f
        val height = 2400f
        val bounds = androidx.compose.ui.geometry.Rect(24f, 120f, 534f, 800f)

        // 0：屏幕上正好是那张卡——左上角对齐、宽度铺满卡片、可见高度等于卡片高度。
        val start = resolveMiuixVideoCardMorphFrame(bounds, width, height, 0f)
        assertEquals(bounds.left, start.translationX, 0.01f)
        assertEquals(bounds.top, start.translationY, 0.01f)
        assertEquals(bounds.width / width, start.scale, 0.0001f)
        assertEquals(bounds.height, start.visibleHeightPx * start.scale, 0.01f)
        assertEquals(0f, start.visibleTopPx, 0.01f)

        // 1：原样的整屏。
        val end = resolveMiuixVideoCardMorphFrame(bounds, width, height, 1f)
        assertEquals(0f, end.translationX, 0.01f)
        assertEquals(0f, end.translationY, 0.01f)
        assertEquals(1f, end.scale, 0.0001f)
        assertEquals(height, end.visibleHeightPx * end.scale, 0.01f)
        assertEquals(0f, end.visibleTopPx, 0.01f)
    }

    @Test
    fun theStatusBarBandIsSqueezedOutWhenThePageSitsOnTheCard() {
        val width = 1080f
        val height = 2400f
        val inset = 90f
        val bounds = androidx.compose.ui.geometry.Rect(24f, 120f, 534f, 800f)

        // 缩到卡片大小时，播放器顶边（页面 y = inset）必须正好落在卡片顶边。
        val start = resolveMiuixVideoCardMorphFrame(bounds, width, height, 0f, inset)
        assertEquals(bounds.top, start.translationY + start.visibleTopPx * start.scale, 0.01f)
        // 露出来的正是播放器：从状态栏那一截的下沿开始，高度还是卡片高度。
        assertEquals(inset, start.visibleTopPx, 0.01f)
        assertEquals(bounds.height, start.visibleHeightPx * start.scale, 0.01f)

        // 整屏时这一截必须归零，否则页面会整体上移一条状态栏。
        val end = resolveMiuixVideoCardMorphFrame(bounds, width, height, 1f, inset)
        assertEquals(0f, end.translationY, 0.01f)
        assertEquals(0f, end.visibleTopPx, 0.01f)
    }

    @Test
    fun morphStaysUniformAndNeverReversesAnEdge() {
        val width = 1240f
        val height = 2772f
        listOf(
            androidx.compose.ui.geometry.Rect(62f, 640f, 569f, 1560f),
            androidx.compose.ui.geometry.Rect(670f, 640f, 1177f, 1560f),
            androidx.compose.ui.geometry.Rect(24f, 1900f, 1216f, 2250f),
        ).forEach { bounds ->
            var previousLeft = Float.POSITIVE_INFINITY
            var previousTop = Float.POSITIVE_INFINITY
            var previousRight = Float.NEGATIVE_INFINITY
            var previousBottom = Float.NEGATIVE_INFINITY
            for (step in 0..200) {
                val morph = step / 200f
                val frame = resolveMiuixVideoCardMorphFrame(bounds, width, height, morph)
                val left = frame.translationX
                val top = frame.translationY + frame.visibleTopPx * frame.scale
                val right = left + frame.scale * width
                val bottom = top + frame.visibleHeightPx * frame.scale

                // 四条边各自单调外扩，中途不回抽。
                assertTrue(left <= previousLeft + 0.01f, "left reversed at morph=$morph")
                assertTrue(top <= previousTop + 0.01f, "top reversed at morph=$morph")
                assertTrue(right >= previousRight - 0.01f, "right reversed at morph=$morph")
                assertTrue(bottom >= previousBottom - 0.01f, "bottom reversed at morph=$morph")
                previousLeft = left
                previousTop = top
                previousRight = right
                previousBottom = bottom

                // 一个比例两轴共用：画面永远等比，不会被压扁或拉长。
                assertTrue(frame.scale in 0.02f..1.0001f, "scale out of range at morph=$morph")
                // 窗口永远被画面铺满：上下不出界，也不留空。
                assertTrue(bottom <= height + 0.05f, "window taller than page at morph=$morph")
            }
        }
    }

    @Test
    fun thePageAppearsOutOfTheCardInsteadOfCuttingIn() {
        // 起点完全透明：那一帧屏幕上只有底下那张真卡。
        assertEquals(0f, resolveMiuixVideoCardMorphAlpha(0f), 0.0001f)
        // 到铺满那一点之前都是半透明，卡片因此是被「长出来的页面」逐渐盖住的。
        assertTrue(resolveMiuixVideoCardMorphAlpha(0.1f) in 0.2f..0.35f)
        assertEquals(1f, resolveMiuixVideoCardMorphAlpha(MIUIX_VIDEO_CARD_MORPH_SOLID_AT), 0.0001f)
        assertEquals(1f, resolveMiuixVideoCardMorphAlpha(1f), 0.0001f)
        var previous = -1f
        for (step in 0..100) {
            val alpha = resolveMiuixVideoCardMorphAlpha(step / 100f)
            assertTrue(alpha in 0f..1f, "out of range at $step")
            assertTrue(alpha >= previous - 0.0001f, "not monotonic at $step")
            previous = alpha
        }
    }

    @Test
    fun returnDepthClearsBlurInsteadOfReversingIt() {
        assertEquals(
            1f,
            resolveMiuixVideoCardDepthProgress(relativeDepth = 0f),
            absoluteTolerance = 0.0001f,
        )
        assertEquals(
            0.5f,
            resolveMiuixVideoCardDepthProgress(relativeDepth = -0.5f),
            absoluteTolerance = 0.0001f,
        )
        assertEquals(
            0f,
            resolveMiuixVideoCardDepthProgress(relativeDepth = -1f),
            absoluteTolerance = 0.0001f,
        )
    }

    @Test
    fun cardClipKeepsPhysicalCornerRadiusAcrossNonUniformScale() {
        val radii = resolveMiuixVideoCardClipRadii(
            sourceCornerPx = 12f,
            outerScaleX = 0.5f,
            outerScaleY = 0.25f,
        )

        assertEquals(12f, radii.radiusX * 0.5f, absoluteTolerance = 0.0001f)
        assertEquals(12f, radii.radiusY * 0.25f, absoluteTolerance = 0.0001f)
    }

    @Test
    fun flyingCardCornersBloomMidPeelThenLandOnTheSourceRadius() {
        assertEquals(
            0f,
            resolveMiuixVideoCardGestureCornerPx(
                sourceCornerPx = 16f,
                morphProgress = 1f,
                floatingCornerPx = 28f,
            ),
            0.0001f,
        )
        assertEquals(
            32f,
            resolveMiuixVideoCardGestureCornerPx(
                sourceCornerPx = 16f,
                morphProgress = 1f,
                floatingCornerPx = 32f,
                fullscreenCornerPx = 32f,
            ),
            0.0001f,
        )
        assertEquals(
            16f,
            resolveMiuixVideoCardGestureCornerPx(
                sourceCornerPx = 16f,
                morphProgress = 0f,
                floatingCornerPx = 28f,
            ),
            0.0001f,
        )
        assertEquals(
            28f,
            resolveMiuixVideoCardGestureCornerPx(
                sourceCornerPx = 16f,
                morphProgress = 0.5f,
                floatingCornerPx = 28f,
            ),
            0.01f,
        )
        val mid = resolveMiuixVideoCardClipRadii(
            sourceCornerPx = 16f,
            outerScaleX = 0.5f,
            outerScaleY = 0.4f,
            morphProgress = 0.5f,
            floatingCornerPx = 28f,
        )
        assertEquals(28f, mid.radiusX * 0.5f, absoluteTolerance = 0.01f)
        assertEquals(28f, mid.radiusY * 0.4f, absoluteTolerance = 0.01f)

    }

    @Test
    fun transparentHorizontalRow_doesNotCutCoverWithAnOuterFloatingCorner() {
        for (shortSide in listOf(20f, 120f, 1080f)) {
            for (deviceCorner in listOf(12f, 32f, 96f, 240f)) {
                // 同一策略覆盖进入、返回及预测返回取消；外框保持矩形，媒体层拥有封面圆角。
                for (step in 0..100) {
                    assertEquals(0f, resolveMiuixVideoCardGestureCornerPx(
                        sourceCornerPx = 0f,
                        morphProgress = step / 100f,
                        floatingCornerPx = deviceCorner,
                        sourceLayout = VideoCardSourceLayout.SIDE_BY_SIDE,
                        currentShortSidePx = shortSide,
                    ))
                }
                // 无底板的横卡例外不能改变其他来源布局的悬浮圆角。
                assertEquals(minOf(deviceCorner, shortSide / 2f), resolveMiuixVideoCardGestureCornerPx(
                    sourceCornerPx = 0f,
                    morphProgress = .5f,
                    floatingCornerPx = deviceCorner,
                    sourceLayout = VideoCardSourceLayout.COVER_ONLY,
                    currentShortSidePx = shortSide,
                ))
            }
        }
    }

    @Test
    fun horizontalCardCornersReachDeviceRadiusAndReverseToTheirSourceRadius() {
        for (source in listOf(8f, 12f, 48f)) {
            for (deviceCorner in listOf(12f, 28f, 96f, 240f)) {
                val samples = (0..100).map { step ->
                    resolveMiuixVideoCardGestureCornerPx(
                        sourceCornerPx = source,
                        morphProgress = step / 100f,
                        floatingCornerPx = deviceCorner,
                        sourceLayout = VideoCardSourceLayout.SIDE_BY_SIDE,
                        currentShortSidePx = source * 10f,
                    )
                }
                assertEquals(source, samples.first(), .0001f)
                assertEquals(0f, samples.last(), .0001f)
                assertEquals(minOf(deviceCorner, source * 5f), samples[50], .0001f)
                assertTrue(samples.all { it in 0f..source * 5f })
                assertTrue(samples.drop(50).zipWithNext().all { (a, b) -> b <= a })
                val reversed = (100 downTo 0).map { step ->
                    resolveMiuixVideoCardGestureCornerPx(
                        source, step / 100f, deviceCorner,
                        sourceLayout = VideoCardSourceLayout.SIDE_BY_SIDE,
                        currentShortSidePx = source * 10f,
                    )
                }
                assertEquals(samples, reversed.reversed())
            }
        }
    }

    @Test
    fun stackedCardCornersMatchHorizontalCardsForEveryDepthAndWindowSize() {
        for (source in listOf(8f, 16f, 48f)) {
            for (shortSide in listOf(20f, 120f, 1080f)) {
                for (deviceCorner in listOf(12f, 28f, 96f, 240f)) {
                    val samples = (0..100).map { step ->
                        val depth = step / 100f
                        val stacked = resolveMiuixVideoCardGestureCornerPx(
                            source, depth, deviceCorner, sourceLayout = VideoCardSourceLayout.STACKED,
                            currentShortSidePx = shortSide,
                        )
                        assertEquals(resolveMiuixVideoCardGestureCornerPx(
                            source, depth, deviceCorner, sourceLayout = VideoCardSourceLayout.SIDE_BY_SIDE,
                            currentShortSidePx = shortSide,
                        ), stacked, .0001f)
                        assertEquals(resolveMiuixVideoCardGestureCornerPx(
                            source, depth, deviceCorner, sourceLayout = VideoCardSourceLayout.COVER_ONLY,
                            currentShortSidePx = shortSide,
                        ), stacked, .0001f)
                        assertTrue(stacked in 0f..shortSide / 2f)
                        stacked
                    }
                    assertEquals(minOf(source, shortSide / 2f), samples.first(), .0001f)
                    assertEquals(0f, samples.last(), .0001f)
                    assertEquals(minOf(deviceCorner, shortSide / 2f), samples[50], .0001f)
                    assertTrue(samples.drop(50).zipWithNext().all { (a, b) -> b <= a })
                }
            }
        }
        // 仅封面的悬浮圆角也受实际窗口短边约束。
        assertEquals(10f, resolveMiuixVideoCardGestureCornerPx(
            16f, .5f, 240f, currentShortSidePx = 20f,
        ), .0001f)
    }

    @Test
    fun horizontalCornerFitsItsCurrentShortSideAndRemainsCircularAfterUnequalScaling() {
        assertEquals(10f, resolveMiuixVideoCardGestureCornerPx(
            sourceCornerPx = 48f,
            morphProgress = .25f,
            floatingCornerPx = 240f,
            sourceLayout = VideoCardSourceLayout.SIDE_BY_SIDE,
            currentShortSidePx = 20f,
        ), .0001f)
        val radii = resolveMiuixVideoCardClipRadii(
            sourceCornerPx = 16f,
            outerScaleX = .8f,
            outerScaleY = .2f,
            morphProgress = .5f,
            floatingCornerPx = 96f,
            sourceLayout = VideoCardSourceLayout.SIDE_BY_SIDE,
            currentShortSidePx = 120f,
        )
        assertEquals(60f, radii.radiusX * .8f, .0001f)
        assertEquals(60f, radii.radiusY * .2f, .0001f)
    }

    @Test
    fun fillWidthTopPreservesAspectRatioAndTopAlignment() {
        val compensation = resolveMiuixVideoCardContentCompensation(
            outerScaleX = 0.5f,
            outerScaleY = 0.25f,
            contentScale = MiuixVideoCardContentScale.FillWidthTop,
        )

        assertEquals(0.5f, 0.5f * compensation.scaleX, absoluteTolerance = 0.0001f)
        assertEquals(0.5f, 0.25f * compensation.scaleY, absoluteTolerance = 0.0001f)
        assertEquals(TransformOrigin(0.5f, 0f), compensation.transformOrigin)
    }

    @Test
    fun cropCenterPreservesAspectRatioUsingCoverScale() {
        val compensation = resolveMiuixVideoCardContentCompensation(
            outerScaleX = 0.35f,
            outerScaleY = 0.6f,
            contentScale = MiuixVideoCardContentScale.CropCenter,
        )

        assertEquals(0.6f, 0.35f * compensation.scaleX, absoluteTolerance = 0.0001f)
        assertEquals(0.6f, 0.6f * compensation.scaleY, absoluteTolerance = 0.0001f)
        assertEquals(TransformOrigin.Center, compensation.transformOrigin)
    }

    @Test
    fun sideBySideAndStackedUseFillWidthTopForLandingAnchors() {
        assertEquals(
            MiuixVideoCardContentScale.FillWidthTop,
            resolveMiuixVideoCardContentScaleForSourceLayout(
                sourceLayout = VideoCardSourceLayout.SIDE_BY_SIDE,
            ),
        )
        assertEquals(
            MiuixVideoCardContentScale.FillWidthTop,
            resolveMiuixVideoCardContentScaleForSourceLayout(
                sourceLayout = VideoCardSourceLayout.STACKED,
            ),
        )
        assertEquals(
            MiuixVideoCardContentScale.CropCenter,
            resolveMiuixVideoCardContentScaleForSourceLayout(
                sourceLayout = VideoCardSourceLayout.STACKED,
                fullscreen = true,
            ),
        )
    }

    @Test
    fun gestureVisualOriginTracksTheFlyingCardInsteadOfTheFullscreenCenter() {
        val source = androidx.compose.ui.geometry.Rect(80f, 400f, 500f, 900f)
        val origin = resolveMiuixVideoCardGestureVisualOrigin(
            sourceBounds = source,
            layoutWidth = 1080f,
            layoutHeight = 2400f,
            morph = 0f,
            localOrigin = TransformOrigin(0.14f, 0.6f),
        )
        assertEquals(
            (source.left + source.width * 0.14f) / 1080f,
            origin.pivotFractionX,
            0.0001f,
        )
        assertEquals(
            (source.top + source.height * 0.6f) / 2400f,
            origin.pivotFractionY,
            0.0001f,
        )
        val fullscreen = resolveMiuixVideoCardGestureVisualOrigin(
            sourceBounds = source,
            layoutWidth = 1080f,
            layoutHeight = 2400f,
            morph = 1f,
            localOrigin = TransformOrigin(0.14f, 0.6f),
        )
        assertEquals(0.14f, fullscreen.pivotFractionX, 0.0001f)
        assertEquals(0.6f, fullscreen.pivotFractionY, 0.0001f)
    }

}
