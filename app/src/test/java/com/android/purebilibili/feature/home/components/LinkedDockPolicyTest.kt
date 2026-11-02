package com.android.purebilibili.feature.home.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LinkedDockPolicyTest {
    @Test
    fun glassHandoffIsContinuousMonotonicAndBoundedInBothDirections() {
        assertEquals(0f, resolveLinkedDockGlassHandoffProgress(-0.1f))
        assertEquals(1f, resolveLinkedDockGlassHandoffProgress(1.1f))
        assertEquals(0.125f, resolveLinkedDockGlassHandoffProgress(0.17f), 0.0001f)
        val forward = (0..100).map { resolveLinkedDockGlassHandoffProgress(it / 100f) }
        assertTrue(forward.zipWithNext().all { (a, b) -> b >= a && b - a < 0.09f })
        assertEquals(forward.reversed(), (100 downTo 0).map { resolveLinkedDockGlassHandoffProgress(it / 100f) })
        forward.forEach { compactAlpha ->
            val navigationAlpha = 1f - compactAlpha
            assertEquals(1f, navigationAlpha + compactAlpha, 0.0001f)
        }
    }

    @Test
    fun expandedDockKeepsSearchBesideActualNavigationWidth() {
        assertEquals(
            196,
            resolveLinkedDockNavigationX(
                maximumWidth = 600,
                navigationWidth = 144,
                button = 56,
                gap = 8,
                searchEnabled = true,
            ),
        )
        assertEquals(
            348,
            resolveLinkedDockSearchX(
                maximumWidth = 600,
                navigationWidth = 144,
                searchWidth = 56,
                button = 56,
                gap = 8,
                mergeProgress = 0f,
                searchProgress = 0f,
            ),
        )
        assertEquals(
            544,
            resolveLinkedDockSearchX(
                maximumWidth = 600,
                navigationWidth = 144,
                searchWidth = 56,
                button = 56,
                gap = 8,
                mergeProgress = 1f,
                searchProgress = 0f,
            ),
        )
    }

    @Test
    fun globalCollapseRequestUsesCompactPlaybackPhaseWhenAudioIsPresent() {
        assertEquals(
            LinkedDockPhase.Playback,
            resolveLinkedDockRestingPhase(collapseRequested = true, hasAudio = true),
        )
    }

    @Test
    fun compactSearchDockKeepsHomeAndSearchOnOneRowWithoutAudio() {
        val geometry = resolveLinkedDockGeometry(
            width = 336,
            button = 56,
            barHeight = 64,
            gap = 8,
            hasAudio = false,
            searchEnabled = true,
            mergeProgress = 1f,
            searchProgress = 1f,
        )
        assertEquals(272, geometry.searchWidth)
        assertEquals(0, geometry.audioWidth)
        assertEquals(0, geometry.top)
        assertEquals(64, geometry.height)
    }

    @Test
    fun collapsedWithoutAudioKeepsSearchShrunk() {
        assertEquals(
            LinkedDockPhase.Expanded,
            resolveLinkedDockRestingPhase(collapseRequested = true, hasAudio = false),
        )
        assertEquals(
            LinkedDockPhase.Expanded,
            resolveLinkedDockRestingPhase(collapseRequested = false, hasAudio = true),
        )
    }

    @Test
    fun audioDismissalRestoresExpandedDockAndPreservesSearch() {
        assertEquals(
            LinkedDockPhase.Expanded,
            resolveLinkedDockPhaseOnAudioChange(LinkedDockPhase.Playback, hasAudio = false),
        )
        assertEquals(
            LinkedDockPhase.Expanded,
            resolveLinkedDockPhaseOnAudioChange(LinkedDockPhase.Compact, hasAudio = false),
        )
        assertEquals(
            LinkedDockPhase.Playback,
            resolveLinkedDockPhaseOnAudioChange(LinkedDockPhase.Compact, hasAudio = true),
        )
        assertEquals(
            LinkedDockPhase.Playback,
            resolveLinkedDockPhaseOnAudioChange(LinkedDockPhase.Playback, hasAudio = true),
        )
        assertEquals(
            LinkedDockPhase.Search,
            resolveLinkedDockPhaseOnAudioChange(LinkedDockPhase.Search, hasAudio = false),
        )
        assertEquals(
            LinkedDockPhase.Expanded,
            resolveLinkedDockPhaseOnAudioChange(LinkedDockPhase.Expanded, hasAudio = false),
        )
    }

    @Test
    fun initialPhasePreservesSavedPhaseAcrossMounts() {
        assertEquals(
            LinkedDockPhase.Playback,
            resolveLinkedDockInitialPhase(
                currentItem = BottomNavItem.HOME,
                collapseRequested = false,
                hasAudio = true,
                savedPhase = LinkedDockPhase.Playback,
            ),
        )
        assertEquals(
            LinkedDockPhase.Search,
            resolveLinkedDockInitialPhase(
                currentItem = BottomNavItem.HOME,
                collapseRequested = false,
                hasAudio = true,
                savedPhase = LinkedDockPhase.Search,
            ),
        )
        assertEquals(
            LinkedDockPhase.Expanded,
            resolveLinkedDockInitialPhase(
                currentItem = BottomNavItem.HOME,
                collapseRequested = false,
                hasAudio = false,
                savedPhase = LinkedDockPhase.Playback,
            ),
        )
        assertEquals(
            LinkedDockPhase.Expanded,
            resolveLinkedDockInitialPhase(
                currentItem = BottomNavItem.HOME,
                collapseRequested = false,
                hasAudio = true,
                savedPhase = null,
            ),
        )
        assertEquals(
            LinkedDockPhase.Playback,
            resolveLinkedDockInitialPhase(
                currentItem = BottomNavItem.DYNAMIC,
                collapseRequested = true,
                hasAudio = true,
                savedPhase = null,
            ),
        )
        assertEquals(
            LinkedDockPhase.Expanded,
            resolveLinkedDockInitialPhase(
                currentItem = BottomNavItem.DYNAMIC,
                collapseRequested = true,
                hasAudio = false,
                savedPhase = null,
            ),
        )
    }

    @Test
    fun expandedAudioOccupiesItsOwnRow() {
        val geometry = geometry(merge = 0f, search = 0f)
        assertEquals(336, geometry.audioWidth)
        assertEquals(0, geometry.audioX)
        assertEquals(0, geometry.audioY)
        assertEquals(72, geometry.top)
        assertEquals(136, geometry.height)
    }

    @Test
    fun compactPlaybackKeepsGapsBetweenThreeSeparateCapsules() {
        val geometry = geometry(merge = 1f, search = 0f)
        assertEquals(64, geometry.audioX)
        assertEquals(208, geometry.audioWidth)
        assertEquals(336, geometry.audioX + geometry.audioWidth + 8 + geometry.searchWidth)
        assertEquals(64, geometry.height)
    }

    @Test
    fun compactPlaybackAnchorsPastTheInsetHomeBall() {
        // COUI 原生胶囊收窄成球时左右各留 16dp 内边距，球的可见右缘比 slot 起点更靠右。
        // dock 传入球的实际右缘后，播放条必须从球右侧接出并留出 gap，否则会压住半个球。
        val ballLeft = 20
        val ballInset = 16
        val button = 56
        val gap = 8
        val compactLeft = ballLeft + ballInset + button + gap
        val geometry = resolveLinkedDockGeometry(
            width = 296,
            button = button,
            barHeight = 56,
            gap = gap,
            hasAudio = true,
            searchEnabled = false,
            mergeProgress = 1f,
            searchProgress = 0f,
            expandedAudioWidth = 256,
            expandedAudioX = ballLeft,
            compactAudioLeft = compactLeft,
        )
        assertEquals(compactLeft, geometry.audioX)
        assertEquals(296 - compactLeft, geometry.audioWidth)
        // 球右缘 + gap 之后才开始，任何一帧都不会压到球上。
        assertTrue(geometry.audioX >= ballLeft + ballInset + button + gap)
    }

    @Test
    fun insetHomeBallStillSettlesOnTheExpandedCluster() {
        val expanded = resolveLinkedDockGeometry(
            width = 296,
            button = 56,
            barHeight = 56,
            gap = 8,
            hasAudio = true,
            searchEnabled = false,
            mergeProgress = 0f,
            searchProgress = 0f,
            expandedAudioWidth = 256,
            expandedAudioX = 20,
            compactAudioLeft = 100,
        )
        assertEquals(20, expanded.audioX)
        assertEquals(256, expanded.audioWidth)
    }

    @Test
    fun compactGroupSitsCenteredWithItsRightEdgePinnedToTheExpandedStrip() {
        // 收起态「球 + 条」整组在容器里对称留白，右缘钉在展开态小横条的右缘：
        // 展开动画里条的右端因此完全不动，只由左缘向左铺开。
        val width = 296
        val expandedX = 20
        val expandedWidth = 256
        val inset = 16
        val button = 56
        val gap = 8
        val group = resolveLinkedDockCompactGroup(
            width = width,
            expandedAudioX = expandedX,
            expandedAudioWidth = expandedWidth,
            capsuleInset = inset,
            button = button,
            gap = gap,
            centered = true,
        )
        val ballLeft = group.capsuleLeft + inset
        val audioRight = requireNotNull(group.audioRight)
        assertEquals(expandedX + expandedWidth, audioRight)
        assertEquals(ballLeft + button + gap, group.audioLeft)
        // 球的可见左缘与展开态播放条的左缘落在同一点：展开时左缘一动不动，
        // 胶囊只从球的右边长开，不存在「先挪一段再展开」。
        assertEquals(expandedX, ballLeft)
        // 可见整组 [球左缘, 条右缘] 居中：两侧留白相等。
        assertEquals(ballLeft, width - audioRight)
        // 条右缘在收拢/展开两端重合，动画中不会整体横移。
        val compact = resolveLinkedDockGeometry(
            width = width,
            button = button,
            barHeight = 56,
            gap = gap,
            hasAudio = true,
            searchEnabled = false,
            mergeProgress = 1f,
            searchProgress = 0f,
            expandedAudioWidth = expandedWidth,
            expandedAudioX = expandedX,
            compactAudioLeft = group.audioLeft,
            compactAudioRight = group.audioRight,
        )
        assertEquals(group.audioLeft, compact.audioX)
        assertEquals(audioRight, compact.audioX + compact.audioWidth)
        val expanded = resolveLinkedDockGeometry(
            width = width,
            button = button,
            barHeight = 56,
            gap = gap,
            hasAudio = true,
            searchEnabled = false,
            mergeProgress = 0f,
            searchProgress = 0f,
            expandedAudioWidth = expandedWidth,
            expandedAudioX = expandedX,
            compactAudioLeft = group.audioLeft,
            compactAudioRight = group.audioRight,
        )
        assertEquals(expandedX, expanded.audioX)
        assertEquals(expandedWidth, expanded.audioWidth)
        assertEquals(audioRight, expanded.audioX + expanded.audioWidth)
    }

    @Test
    fun rightEdgeStaysPinnedAndLeftEdgeOnlyAdvancesAcrossTheWholeMergeRange() {
        // 展开是一段连续形变，不能出现"先把球挪到位、再让条铺开"的两段感：
        // 对整个 merge 区间采样，条的右缘必须钉住不动，左缘只能单向朝展开位移动，
        // 宽度只能单向增长。任何一处反向都会让动画看起来停顿或回弹。
        // 参数取导航胶囊的可见表面（296dp 容器内 256dp，左缘 20dp）。
        val width = 296
        val expandedX = 20
        val expandedWidth = 256
        val inset = 16
        val button = 56
        val gap = 8
        val group = resolveLinkedDockCompactGroup(
            width = width,
            expandedAudioX = expandedX,
            expandedAudioWidth = expandedWidth,
            capsuleInset = inset,
            button = button,
            gap = gap,
            centered = true,
        )
        val audioRight = requireNotNull(group.audioRight)
        var previousLeft = Int.MIN_VALUE
        var previousWidth = Int.MAX_VALUE
        for (step in 0..100) {
            val merge = step / 100f
            val geometry = resolveLinkedDockGeometry(
                width = width,
                button = button,
                barHeight = 56,
                gap = gap,
                hasAudio = true,
                searchEnabled = false,
                mergeProgress = merge,
                searchProgress = 0f,
                expandedAudioWidth = expandedWidth,
                expandedAudioX = expandedX,
                compactAudioLeft = group.audioLeft,
                compactAudioRight = group.audioRight,
            )
            // 右缘只允许有 1px 的四舍五入抖动，视觉上是同一个位置。
            assertEquals(
                audioRight.toFloat(),
                (geometry.audioX + geometry.audioWidth).toFloat(),
                1f,
                "merge=$merge right edge",
            )
            // merge 0 = 展开，1 = 收拢；从左向右采样时左缘只右移、宽度只收窄，
            // 中途折返就会看出"停顿—再跳"的两段感。
            assertTrue(geometry.audioX >= previousLeft, "merge=$merge left edge")
            assertTrue(geometry.audioWidth <= previousWidth, "merge=$merge width")
            previousLeft = geometry.audioX
            previousWidth = geometry.audioWidth
        }
        assertEquals(group.audioLeft, previousLeft)
        assertEquals(audioRight - group.audioLeft, previousWidth)
    }

    @Test
    fun capsuleLeftLandsOnTheExpandedVisibleLeftEdgeSoTheBallOnlyGrowsRightward() {
        // 按 dock 的真实参数复算：296dp 容器、导航槽位 288dp、COUI 胶囊左右各内缩 16dp。
        // 槽位补回两圈内缩后，可见胶囊和播放条都是 256dp；
        // 整组居中 ⇒ 球的可见左缘必然等于展开态可见左缘，展开时左缘一动不动。
        val width = 296
        val inset = 16
        val navSlotWidth = 288
        val navigationX = (width - navSlotWidth) / 2
        val stripExpandedX = navigationX + inset
        val stripExpandedWidth = navSlotWidth - inset * 2
        assertEquals(4, navigationX)
        assertEquals(20, stripExpandedX)
        assertEquals(256, stripExpandedWidth)
        val group = resolveLinkedDockCompactGroup(
            width = width,
            expandedAudioX = stripExpandedX,
            expandedAudioWidth = stripExpandedWidth,
            capsuleInset = inset,
            button = 56,
            gap = 8,
            centered = true,
        )
        // 槽位左缘两端重合：动画里没有任何横向位移。
        assertEquals(navigationX, group.capsuleLeft)
        // 球的可见左缘 == 展开态播放条左缘。
        assertEquals(stripExpandedX, group.capsuleLeft + inset)
        // 整组 [球可见左缘, 条右缘] 在容器里对称留白。
        val audioRight = requireNotNull(group.audioRight)
        assertEquals(stripExpandedX, width - audioRight)
        assertEquals(stripExpandedX + stripExpandedWidth, audioRight)
    }

    @Test
    fun uncenteredCompactGroupFallsBackToTheSearchAwareLayout() {
        // 搜索圆钮占右侧时必须退回旧布局：条右缘交给几何函数按圆钮退位。
        val group = resolveLinkedDockCompactGroup(
            width = 296,
            expandedAudioX = 20,
            expandedAudioWidth = 256,
            capsuleInset = 16,
            button = 56,
            gap = 8,
            centered = false,
            fallbackCapsuleLeft = 20,
        )
        assertEquals(20, group.capsuleLeft)
        assertEquals(100, group.audioLeft)
        assertEquals(null, group.audioRight)
    }

    @Test
    fun intermediateMergeKeepsAudioYZeroToAvoidQuadraticDropDistortion() {
        val half = geometry(merge = 0.5f, search = 0f)
        assertEquals(0, half.audioY)
        assertEquals(36, half.top)
        assertEquals(100, half.height)
        assertEquals(32, half.audioX)
        assertEquals(272, half.audioWidth)
    }

    @Test
    fun playbackGapsRemainVisibleAcrossWindowWidths() {
        for (width in listOf(240, 296, 336, 600)) {
            for (searchEnabled in listOf(false, true)) {
                val geometry = resolveLinkedDockGeometry(width, 56, 64, 8, true, searchEnabled, 1f, 0f)
                assertEquals(8, geometry.audioX - 56)
                val trailingGap = width - geometry.searchWidth - geometry.audioX - geometry.audioWidth
                assertEquals(if (searchEnabled) 8 else 0, trailingGap)
                assertTrue(geometry.audioWidth >= 112)
            }
        }
    }

    @Test
    fun searchLeavesOneAccessibleArtworkTarget() {
        val geometry = geometry(merge = 1f, search = 1f)
        assertEquals(56, geometry.audioWidth)
        assertEquals(208, geometry.searchWidth)
        assertEquals(64, geometry.height)
    }

    @Test
    fun narrowAndWideLayoutsDoNotOverlapAtRest() {
        for (width in listOf(240, 296, 336, 600)) {
            for (hasAudio in listOf(false, true)) {
                val geometry = resolveLinkedDockGeometry(width, 56, 64, 8, hasAudio, true, 1f, 1f)
                assertTrue(geometry.searchWidth >= 56)
                assertTrue(geometry.audioWidth >= 0)
                val occupiedWidth = 56 + geometry.audioWidth + geometry.searchWidth +
                    if (hasAudio) 16 else 8
                assertEquals(width, occupiedWidth)
            }
        }
    }

    @Test
    fun outOfRangeAnimationProgressCannotProduceNegativeSizes() {
        val geometry = geometry(merge = 1.05f, search = 1.04f)
        assertEquals(0, geometry.top)
        assertEquals(56, geometry.audioWidth)
        assertEquals(64, geometry.height)
    }

    @Test
    fun presenceCollapsesTowardTheRightEdgeWithoutMovingTheAnchor() {
        val full = resolveLinkedDockGeometry(
            width = 336, button = 56, barHeight = 64, gap = 8,
            hasAudio = true, searchEnabled = true,
            mergeProgress = 1f, searchProgress = 0f,
            presenceProgress = 1f,
        )
        val half = resolveLinkedDockGeometry(
            width = 336, button = 56, barHeight = 64, gap = 8,
            hasAudio = true, searchEnabled = true,
            mergeProgress = 1f, searchProgress = 0f,
            presenceProgress = 0.5f,
        )
        val gone = resolveLinkedDockGeometry(
            width = 336, button = 56, barHeight = 64, gap = 8,
            hasAudio = true, searchEnabled = true,
            mergeProgress = 1f, searchProgress = 0f,
            presenceProgress = 0f,
        )
        // 右缘锚定：胶囊收放时右边界不动，左缘随宽度移动。
        val rightEdge = full.audioX + full.audioWidth
        assertEquals(rightEdge, half.audioX + half.audioWidth)
        assertEquals(full.audioWidth / 2, half.audioWidth)
        assertEquals(0, gone.audioWidth)
        assertEquals(rightEdge, gone.audioX)
    }

    @Test
    fun expandedPresenceAnchorsToTheContainerRightEdge() {
        val half = resolveLinkedDockGeometry(
            width = 336, button = 56, barHeight = 64, gap = 8,
            hasAudio = true, searchEnabled = true,
            mergeProgress = 0f, searchProgress = 0f,
            presenceProgress = 0.5f,
        )
        assertEquals(336 / 2, half.audioWidth)
        assertEquals(336, half.audioX + half.audioWidth)
    }

    @Test
    fun homeScrollDirectionChangeStartsANewThreshold() {
        assertEquals(16f, accumulateDockScroll(10f, 6f))
        assertEquals(-3f, accumulateDockScroll(16f, -3f))
        assertEquals(-13f, accumulateDockScroll(-3f, -10f))
    }

    @Test
    fun backHandlerOnlyEnabledForSearchAtTopLevel() {
        assertTrue(shouldEnableLinkedDockBackHandler(LinkedDockPhase.Search, isTopLevelDestination = true))
        kotlin.test.assertFalse(shouldEnableLinkedDockBackHandler(LinkedDockPhase.Search, isTopLevelDestination = false))
        kotlin.test.assertFalse(shouldEnableLinkedDockBackHandler(LinkedDockPhase.Playback, isTopLevelDestination = true))
        kotlin.test.assertFalse(shouldEnableLinkedDockBackHandler(LinkedDockPhase.Playback, isTopLevelDestination = false))
        kotlin.test.assertFalse(shouldEnableLinkedDockBackHandler(LinkedDockPhase.Expanded, isTopLevelDestination = true))
        kotlin.test.assertFalse(shouldEnableLinkedDockBackHandler(LinkedDockPhase.Expanded, isTopLevelDestination = false))
    }

    @Test
    fun searchDismissRestoresPlaybackIfAudioActiveElseExpanded() {
        assertEquals(LinkedDockPhase.Playback, resolveLinkedDockPhaseOnSearchDismiss(hasAudio = true))
        assertEquals(LinkedDockPhase.Expanded, resolveLinkedDockPhaseOnSearchDismiss(hasAudio = false))
        assertTrue(shouldResetLinkedDockSearchQuery(LinkedDockPhase.Expanded))
        assertTrue(shouldResetLinkedDockSearchQuery(LinkedDockPhase.Compact))
        assertTrue(shouldResetLinkedDockSearchQuery(LinkedDockPhase.Playback))
        assertFalse(shouldResetLinkedDockSearchQuery(LinkedDockPhase.Search))
        assertEquals(
            LinkedDockPhase.Expanded,
            resolveLinkedDockPhaseOnSearchDismiss(
                hasAudio = false,
                previousPhase = LinkedDockPhase.Compact,
            ),
        )
    }

    @Test
    fun searchPlaybackTargetConsumesFirstTapToRestorePlayback() {
        assertTrue(shouldExpandPlaybackFromSearch(LinkedDockPhase.Search, hasAudio = true))
        kotlin.test.assertFalse(shouldExpandPlaybackFromSearch(LinkedDockPhase.Search, hasAudio = false))
        kotlin.test.assertFalse(shouldExpandPlaybackFromSearch(LinkedDockPhase.Playback, hasAudio = true))
        kotlin.test.assertFalse(shouldExpandPlaybackFromSearch(LinkedDockPhase.Expanded, hasAudio = true))
    }

    @Test
    fun expandedAudioMatchesBottomBarClusterLengthAndOrigin() {
        // 展开态小横条与底栏整簇（导航胶囊 + 搜索圆钮）同宽同起点，
        // 两行胶囊左右边缘对齐。
        val clusterWidth = 352
        val clusterX = 124
        val geometry = resolveLinkedDockGeometry(
            width = 600, button = 56, barHeight = 64, gap = 8,
            hasAudio = true, searchEnabled = true,
            mergeProgress = 0f, searchProgress = 0f,
            presenceProgress = 1f,
            expandedAudioWidth = clusterWidth,
            expandedAudioX = clusterX,
        )
        assertEquals(clusterWidth, geometry.audioWidth)
        assertEquals(clusterX, geometry.audioX)
    }

    @Test
    fun expandedAudioPresenceKeepsRightEdgeAnchoredAtClusterEdge() {
        val clusterWidth = 352
        val clusterX = 124
        val full = resolveLinkedDockGeometry(
            width = 600, button = 56, barHeight = 64, gap = 8,
            hasAudio = true, searchEnabled = true,
            mergeProgress = 0f, searchProgress = 0f,
            presenceProgress = 1f,
            expandedAudioWidth = clusterWidth,
            expandedAudioX = clusterX,
        )
        val half = resolveLinkedDockGeometry(
            width = 600, button = 56, barHeight = 64, gap = 8,
            hasAudio = true, searchEnabled = true,
            mergeProgress = 0f, searchProgress = 0f,
            presenceProgress = 0.5f,
            expandedAudioWidth = clusterWidth,
            expandedAudioX = clusterX,
        )
        assertEquals(clusterX + clusterWidth, full.audioX + full.audioWidth)
        assertEquals(full.audioX + full.audioWidth, half.audioX + half.audioWidth)
        assertEquals(full.audioWidth / 2, half.audioWidth)
    }

    @Test
    fun mergeAnimationStillCollapsesAudioTowardFirstButton() {
        // 对齐底栏簇只改展开态落点；merge 动画终点（并入首按钮旁的胶囊）不变。
        val geometry = resolveLinkedDockGeometry(
            width = 600, button = 56, barHeight = 64, gap = 8,
            hasAudio = true, searchEnabled = true,
            mergeProgress = 1f, searchProgress = 0f,
            presenceProgress = 1f,
            expandedAudioWidth = 352,
            expandedAudioX = 124,
        )
        assertEquals(64, geometry.audioX)
        assertEquals(600 - 56 - geometry.searchWidth - 16, geometry.audioWidth)
    }

    private fun geometry(merge: Float, search: Float) =
        resolveLinkedDockGeometry(336, 56, 64, 8, true, true, merge, search)
}
