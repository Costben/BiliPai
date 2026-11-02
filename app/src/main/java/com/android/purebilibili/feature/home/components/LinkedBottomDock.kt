package com.android.purebilibili.feature.home.components

import com.android.purebilibili.core.ui.AppContentColors

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.ui.components.AppIcon
import com.android.purebilibili.core.ui.components.AppElasticPressState
import com.android.purebilibili.core.ui.components.appElasticPress
import com.android.purebilibili.core.ui.components.rememberAppElasticPressState
import io.github.suqi8.coui.kmp.basic.spotLight as couiSpotLight
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.ui.graphics.TransformOrigin
import com.android.purebilibili.core.ui.motion.rememberSystemReduceMotion
import com.android.purebilibili.feature.audio.screen.AUDIO_NOW_PLAYING_PRESENCE_ENTER_SLIDE_DP
import com.android.purebilibili.feature.audio.screen.resolveAudioNowPlayingPresenceAlpha
import com.android.purebilibili.feature.audio.screen.resolveAudioNowPlayingPresenceAnimationSpec
import com.android.purebilibili.feature.audio.screen.resolveAudioNowPlayingPresenceEnterSpringSpec
import com.android.purebilibili.feature.home.LocalHomeScrollOffset
import kotlinx.coroutines.flow.collect
import dev.chrisbanes.haze.HazeState
import io.github.suqi8.coui.kmp.blur.Backdrop
import io.github.suqi8.coui.kmp.basic.MetaBallDefaults
import io.github.suqi8.coui.kmp.basic.MetaBallSurface
import io.github.suqi8.coui.kmp.basic.MetaBallParticle
import io.github.suqi8.coui.kmp.basic.BottomFloatingNavigationBarDefaults
import io.github.suqi8.coui.kmp.basic.GlassContainer
import io.github.suqi8.coui.kmp.basic.couiGlassSurface
import io.github.suqi8.coui.kmp.basic.rememberGlassHighlight
import com.android.purebilibili.core.theme.LocalAppUiStyle
import com.android.purebilibili.core.theme.AppUiStyle
import kotlin.math.roundToInt

typealias LinkedDockNowPlayingSlot = @Composable (
    Modifier,
    () -> Float,
    () -> Float,
    () -> Float,
    (() -> Unit)?,
    Boolean,
) -> Unit

/**
 * 当底栏材质由外层（融合球或 COUI 原生胶囊）承担时，导航内容必须停止绘制自己的外壳，
 * 否则同一块区域会叠两层玻璃（高光翻倍、边缘出现硬切）。
 */
internal val LocalLinkedDockShellSuppressed = androidx.compose.runtime.compositionLocalOf { false }

/**
 * 底栏「整条胶囊 → 首页球」的合并进度（0 = 完全展开，1 = 只剩一颗球）。
 *
 * ColorOS 17 下由 COUI 原生胶囊自己收窄承担形变，导航内容只读取这个进度做淡出，
 * 不再参与任何外壳绘制。
 */
internal val LocalLinkedDockCollapseProgress =
    androidx.compose.runtime.compositionLocalOf<() -> Float> { { 0f } }

/**
 * 收起态首页球与播放条之间的间距。
 *
 * 融合球的场域向外扩 [MetaBallDefaults.Extension] × [MetaBallDefaults.EXTENSION_FACTOR_X]
 * （4dp × 1.5 = 6dp 每侧），两球中心距因此至少要留 12dp 才不会在静止时焊成一块；
 * 取 14dp 留 2dp 余量 —— 静止时是两颗分开的球，运动靠近时才连成一块胶体。
 * 播放条的收起宽度由「右缘对称贴边」反算，间距变宽它自然窄一点。
 */
private val COMPACT_MELT_GAP = 14.dp

/**
 * 按住/拖动时融合球额外扩张的场域。
 *
 * 静止时两球间隔 14dp，需要每侧 7dp 的场域才连得起来（基础 4dp × 1.5 = 7dp 刚好不够），
 * 按住时补到 10dp，两块玻璃之间就拉出一条真实的颈。
 */
private const val MELT_PULL_EXTENSION_DP = 6f


@Composable
internal fun LinkedBottomDock(
    currentItem: BottomNavItem,
    firstItem: BottomNavItem,
    firstLabel: String,
    searchEnabled: Boolean,
    isFeedScrollInProgress: Boolean,
    collapseRequested: Boolean,
    onSearchClick: () -> Unit,
    onSearchKeywordSubmit: (String) -> Unit,
    containerColor: Color,
    backdrop: Backdrop?,
    glassEnabled: Boolean,
    liquidGlassTuning: LiquidGlassTuning,
    iconStyle: SharedFloatingBottomBarIconStyle,
    navigationItemCount: Int,
    navigationLabelMode: Int,
    navigationMinEdgePadding: androidx.compose.ui.unit.Dp,
    nowPlayingContent: LinkedDockNowPlayingSlot?,
    dockPhase: LinkedDockPhase? = null,
    onDockPhaseChange: ((LinkedDockPhase) -> Unit)? = null,
    isTopLevelDestination: Boolean = true,
    mergeOnScrollDownEnabled: Boolean = true,
    compactOnScrollEnabled: Boolean = false,
    currentLabel: String = firstLabel,
    animateNowPlayingPresence: Boolean = true,
    hasUiSkinDecoration: Boolean = false,
    modifier: Modifier = Modifier,
    blurEnabled: Boolean = false,
    hazeState: HazeState? = null,
    navigationContent: @Composable () -> Unit,
) {
    com.android.purebilibili.core.ui.components.ProvideAppElasticFeedback {
        val searchHaptic = com.android.purebilibili.core.util.rememberHapticFeedback(
            com.android.purebilibili.core.ui.AppHapticScene.NAVIGATION,
        )
        val hasAudio = nowPlayingContent != null
        // ColorOS 17 的底栏不再跟随滚动做位移/缩放：图形层级的 alpha/scale 会把玻璃
        // 变成离屏缓冲或拉伸纹理，导致"一滚就没模糊、边缘出现硬切"。COUI17 保持底栏
        // 固定不动，收缩只发生在"整栏 → 首页球"这一条融合球动画上。
        val isCoui17Style = LocalAppUiStyle.current == AppUiStyle.COUI17
        var keepSlotComposed by remember { mutableStateOf(hasAudio) }
        val useNavigationCompact = shouldUseScrollCompactNavigation(
            enabled = compactOnScrollEnabled && !isCoui17Style,
            hasAudio = hasAudio,
            retainingAudio = keepSlotComposed,
        )
        var compactNavigation by remember(currentItem, useNavigationCompact) { mutableStateOf(false) }
        var internalPhase by remember(currentItem, searchEnabled) {
            mutableStateOf(
                resolveLinkedDockInitialPhase(
                    currentItem = currentItem,
                    collapseRequested = collapseRequested,
                    hasAudio = hasAudio,
                    savedPhase = dockPhase,
                )
            )
        }
        val phase = dockPhase ?: internalPhase
        val updatePhase: (LinkedDockPhase) -> Unit = { newPhase ->
            if (dockPhase != null && onDockPhaseChange != null) {
                onDockPhaseChange(newPhase)
            } else {
                internalPhase = newPhase
            }
        }
        LaunchedEffect(useNavigationCompact, currentItem, isTopLevelDestination) {
            if (useNavigationCompact && isTopLevelDestination && phase != LinkedDockPhase.Search) {
                updatePhase(LinkedDockPhase.Expanded)
            }
        }
        LaunchedEffect(phase) {
            if (phase == LinkedDockPhase.Search) compactNavigation = false
        }
        LaunchedEffect(hasAudio, dockPhase) {
            if (dockPhase == null) {
                internalPhase = resolveLinkedDockPhaseOnAudioChange(internalPhase, hasAudio)
            }
        }
        var query by remember { mutableStateOf("") }
        LaunchedEffect(phase) {
            if (shouldResetLinkedDockSearchQuery(phase)) {
                query = ""
            }
        }
        var pendingUserImeRequest by remember { mutableStateOf(false) }
        val focusManager = LocalFocusManager.current
        val keyboardController = LocalSoftwareKeyboardController.current
        val scroll = LocalHomeScrollOffset.current
        val currentPhase by rememberUpdatedState(phase)
        val scrolling by rememberUpdatedState(isFeedScrollInProgress)
        val threshold = with(LocalDensity.current) { 24.dp.toPx() }
        LaunchedEffect(currentItem, hasAudio, searchEnabled, scroll, threshold, isTopLevelDestination, mergeOnScrollDownEnabled, glassEnabled, useNavigationCompact) {
            var previous = scroll.floatValue
            var accumulated = 0f
            snapshotFlow { scroll.floatValue to scrolling }.collect { (offset, active) ->
                val delta = offset - previous
                previous = offset
                if (!active || !isTopLevelDestination || currentPhase == LinkedDockPhase.Search) {
                    accumulated = 0f
                } else if (useNavigationCompact) {
                    // Without playback, only navigation compacts; playback retains its original phases.
                    accumulated = accumulateDockScroll(accumulated, delta)
                    resolveScrollCompactIntent(accumulated, threshold)?.let { compact ->
                        compactNavigation = compact
                        accumulated = 0f
                    }
                } else if (!mergeOnScrollDownEnabled) {
                    // 用户选择下滑不合体：只保留向上滚动回到展开（分体）的能力。
                    accumulated = accumulateDockScroll(accumulated, delta)
                    // The shared offset is a relative accumulator, not distance from the top.
                    // Glass requires the full direction threshold even when it becomes negative.
                    if ((!glassEnabled && offset <= 0f && delta < 0f) || accumulated <= -threshold) {
                        updatePhase(LinkedDockPhase.Expanded)
                        accumulated = 0f
                    }
                } else {
                    accumulated = accumulateDockScroll(accumulated, delta)
                    // The shared offset is a relative accumulator, not distance from the top.
                    // Glass requires the full direction threshold even when it becomes negative.
                    if ((!glassEnabled && offset <= 0f && delta < 0f) || accumulated <= -threshold) {
                        updatePhase(LinkedDockPhase.Expanded)
                        accumulated = 0f
                    } else if (hasAudio && accumulated >= threshold) {
                        updatePhase(LinkedDockPhase.Playback)
                        accumulated = 0f
                    }
                }
            }
        }
        // Keep the dock phase while a child destination covers the current tab. Keying this effect
        // by isTopLevelDestination made the returning page re-expand/re-collapse the playback strip,
        // which also shifted the predictive-back target after the gesture had started.
        // Skip while the list is scrolling so resting phase does not fight scroll-driven search size.
        LaunchedEffect(currentItem, collapseRequested, hasAudio, useNavigationCompact) {
            if (
                isTopLevelDestination && !useNavigationCompact &&
                currentItem != BottomNavItem.HOME &&
                currentPhase != LinkedDockPhase.Search &&
                !isFeedScrollInProgress
            ) {
                updatePhase(resolveLinkedDockRestingPhase(collapseRequested, hasAudio))
            }
        }
        fun expand() {
            focusManager.clearFocus()
            keyboardController?.hide()
            updatePhase(LinkedDockPhase.Expanded)
        }
        var phaseBeforeSearch by remember { mutableStateOf(LinkedDockPhase.Expanded) }
        val backEnabled = shouldEnableLinkedDockBackHandler(
            phase = phase,
            isTopLevelDestination = isTopLevelDestination,
        )
        BackHandler(enabled = backEnabled) {
            focusManager.clearFocus()
            keyboardController?.hide()
            updatePhase(resolveLinkedDockPhaseOnSearchDismiss(hasAudio, phaseBeforeSearch))
        }
        val reduceMotion = rememberSystemReduceMotion() ||
            !com.android.purebilibili.core.ui.LocalComponentMotionEnabled.current
        val transition = updateTransition(targetState = phase, label = "linkedBottomDock")
        // 共用弹簧保持合体、解体和中途反向时的速度连续。
        val merge = transition.animateFloat(
            transitionSpec = {
                when {
                    reduceMotion -> snap()
                    // COUI17 的整栏 → 首页球是一段"缩小并融进去"的流体形变，
                    // 用更低刚度的弹簧让它走完整段轨迹，而不是在百毫秒内闪过去。
                    isCoui17Style -> bottomBarDockMeltSpring()
                    else -> bottomBarSearchMorphSpring()
                }
            },
            label = "dockMerge",
        ) { if (it == LinkedDockPhase.Expanded) 0f else 1f }
        val search = transition.animateFloat(
            transitionSpec = {
                if (reduceMotion) snap() else bottomBarSearchMorphSpring()
            },
            label = "dockSearch",
        ) { if (it == LinkedDockPhase.Search || it == LinkedDockPhase.Compact) 1f else 0f }
        val imeSettled = WindowInsets.ime
            .getBottom(LocalDensity.current) == 0
        val mergeProgressProvider = remember(merge) {
            { merge.value.coerceIn(0f, 1f) }
        }
        val searchProgressProvider = remember(search) {
            { search.value.coerceIn(0f, 1f) }
        }
        val identityIconScaleProvider = remember { { 1f } }

        // Shared morph owns both directions. Keep the real slot at its endpoint while
        // handoff alpha hides it; replaying an entrance spring causes a second landing.
        val presence = remember { Animatable(if (hasAudio) 1f else 0f) }
        val slotEntering = remember { mutableStateOf(false) }
        val nowPlayingLayoutStable = !transition.isRunning && !presence.isRunning && imeSettled
        LaunchedEffect(hasAudio, animateNowPlayingPresence, reduceMotion) {
            val target = if (hasAudio) 1f else 0f
            when {
                !animateNowPlayingPresence -> {
                    slotEntering.value = false
                    presence.snapTo(target)
                    keepSlotComposed = hasAudio
                }
                presence.value == target -> {
                    slotEntering.value = false
                    keepSlotComposed = hasAudio
                }
                hasAudio -> {
                    keepSlotComposed = true
                    if (reduceMotion) {
                        slotEntering.value = false
                        presence.snapTo(1f)
                    } else {
                        slotEntering.value = true
                        presence.animateTo(
                            targetValue = 1f,
                            animationSpec = resolveAudioNowPlayingPresenceEnterSpringSpec(),
                        )
                        slotEntering.value = false
                    }
                }
                animateNowPlayingPresence -> {
                    slotEntering.value = false
                    presence.animateTo(
                        targetValue = 0f,
                        animationSpec = resolveAudioNowPlayingPresenceAnimationSpec(
                            active = false,
                            reduceMotion = reduceMotion,
                        ),
                    )
                    keepSlotComposed = false
                }
                else -> {
                    slotEntering.value = false
                    presence.snapTo(0f)
                    keepSlotComposed = false
                }
            }
        }
        val presenceProgressProvider = remember(presence) {
            { presence.value.coerceIn(0f, 1f) }
        }
        // 入场期间槽宽直接取目标值（上滑 + fade 已足够），宽度收放只在退出时生效。
        val slotGeometryPresenceProvider = remember {
            { if (slotEntering.value) 1f else presence.value.coerceIn(0f, 1f) }
        }
        // 保留最后一个有效播放条，退出动画完成后再释放；最新值在关闭时已是 null。
        var retainedNowPlayingContent by remember { mutableStateOf(nowPlayingContent) }
        SideEffect {
            if (nowPlayingContent != null || !keepSlotComposed) {
                retainedNowPlayingContent = nowPlayingContent
            }
        }
        val nowPlayingSlot = nowPlayingContent
            ?: retainedNowPlayingContent.takeIf { keepSlotComposed }

        val nativeMiuixNavigation = com.android.purebilibili.core.ui.isMiuixNonGlassEnabled() && !hasUiSkinDecoration
        val shape = resolveSharedBottomBarCapsuleShape()
        val contentColor = AppContentColors.primaryText()
        // Only glass accents use the exact picked color; native and neutral actions retain their roles.
        val accentColor = AppContentColors.selectionAccent(glassEnabled)
        val navigationColor = com.android.purebilibili.core.ui.AppInteractionColors.nativeNavigation(
            com.android.purebilibili.core.ui.AppNavigationColorRole.BAR,
        )?.selectedContent ?: accentColor
        BoxWithConstraints(
            modifier = modifier.fillMaxWidth().imePadding().navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            val density = LocalDensity.current
            val maximumWidth: Int
            val button: Int
            val barHeight: Int
            val controlHeight: Int
            val gap: Int
            val verticalGap: Int
            with(density) {
                maximumWidth = constraints.maxWidth.coerceAtMost(600.dp.roundToPx())
                button = resolveBiliPaiBottomBarSearchCircleSize().roundToPx()
                barHeight = resolveBiliPaiBottomBarDockHeight(
                    searchExpanded = false,
                    hasUiSkinDecoration = hasUiSkinDecoration,
                    nativeMiuixNavigation = nativeMiuixNavigation,
                ).roundToPx()
                controlHeight = barHeight
                gap = 8.dp.roundToPx()
                verticalGap = 8.dp.roundToPx()
            }
            val searchHeight = button
            val expandedGlassSearchHeight = with(density) { 48.dp.roundToPx() }
            // 玻璃合体搜索从圆按钮过渡到 48dp 输入框；原生路径保持原有高度。
            fun searchVisualHeight(): Int = if (glassEnabled) {
                (searchHeight + (expandedGlassSearchHeight - searchHeight) * search.value.coerceIn(0f, 1f)).roundToInt()
            } else searchHeight
            // 两行宿主与导航行高度固定；搜索框仅在自身测量、放置阶段读取展开进度，
            // 不把搜索框高度变化传回页面组合或整条底栏的测量。
            val containerHeight = barHeight + verticalGap + barHeight
            val navRowY = containerHeight - barHeight
            val controlRowY = navRowY + (barHeight - controlHeight) / 2

            val preferredNavigationWidth = with(density) {
                resolveBiliPaiFloatingBottomBarWidth(
                    containerWidth = maximumWidth.toDp(),
                    itemCount = navigationItemCount,
                    minEdgePadding = navigationMinEdgePadding,
                    labelMode = navigationLabelMode,
                    // ColorOS 17 底栏每个 tab 的内容槽宽是 56dp：5×56 + 2×4 = 288dp。
                    // 按下态高光铺到的宽度就是这一条，胶囊贴着它收口，不再铺满屏宽。
                    cornerRadius = 28.dp,
                ).roundToPx()
            }
            val isCoui17 = LocalAppUiStyle.current == AppUiStyle.COUI17
            // ColorOS 17 的材质完全由 COUI 原生胶囊（BottomFloatingNavigationBar）承担：
            // 不再叠加任何自绘外壳或融合球背景，收缩时只让胶囊自身收窄成圆，
            // 玻璃采样始终落在真实几何上，不会出现"多套一层宽背景"或"一动就失去模糊"。
            val couiNativeCapsule = isCoui17
            // COUI 胶囊在窄屏上左右各留 16dp 外边距，可见表面因此比槽位窄两圈。
            val couiCapsuleInsetPx = with(density) {
                BottomFloatingNavigationBarDefaults.PaddingHorizontal.roundToPx()
            }
            val reservedSearchWidth = if (searchEnabled) button + gap else 0
            val expandedNavigationWidth = preferredNavigationWidth.coerceAtMost(
                (maximumWidth - reservedSearchWidth).coerceAtLeast(0)
            )
            // 槽位把这两圈补回来：可见胶囊和上方播放条一样宽（256dp），两行左右边缘对齐。
            // 不补的话槽位 256dp 只画出 224dp 的胶囊，球的左缘永远够不到展开态的左缘。
            val navWidth = if (couiNativeCapsule && !searchEnabled) {
                (expandedNavigationWidth + couiCapsuleInsetPx * 2).coerceAtMost(maximumWidth)
            } else {
                expandedNavigationWidth.coerceAtMost(maximumWidth)
            }
            val navigationX = resolveLinkedDockNavigationX(
                maximumWidth = maximumWidth,
                navigationWidth = navWidth,
                button = button,
                gap = gap,
                searchEnabled = searchEnabled,
            )
            // 展开态播放条对齐导航胶囊的**可见表面**，而不是槽位。
            // 收起态「球 + 条」整组居中后，球的可见左缘恰好落在展开态可见左缘上：
            // 展开时左缘一动不动，胶囊只从球的右边长开。
            val stripExpandedX = navigationX + if (couiNativeCapsule) couiCapsuleInsetPx else 0
            val stripExpandedWidth = if (couiNativeCapsule) {
                (navWidth - couiCapsuleInsetPx * 2).coerceAtLeast(0)
            } else {
                // 其它皮肤没有内缩，播放条仍与底栏整簇（导航胶囊 + 搜索圆钮）同宽同起点。
                (navWidth + reservedSearchWidth).coerceAtMost(maximumWidth)
            }

            // Glass keeps the navigation/backdrop mounted across both endpoints. At the compact
            // endpoint skip placement so the hidden row neither draws nor intercepts touches.
            // Native toolbars retain their original composition and motion behavior.
            val collapsedAtRest = phase != LinkedDockPhase.Expanded && !transition.isRunning

            // 与播放条的玻璃取同一个暗色判定：两行共用一份底栏背景，
            // 主题判断不同源会让暗色下球和条一个是深玻璃、一个是白玻璃。
            val isDarkTheme = resolveBottomBarDarkTheme(
                com.android.purebilibili.core.ui.AppSurfaceTokens.background(),
            )
            val couiSmallHighlight = rememberGlassHighlight(GlassContainer.Small, backdrop)
            val ballSize = button
            // 收窄到「球」时必须把胶囊内边距算进去，否则胶囊永远收不成正圆。
            val couiCapsuleHorizontalInset =
                BottomFloatingNavigationBarDefaults.PaddingHorizontal * 2
            val ballRowWidth = with(density) { (ballSize.toDp() + couiCapsuleHorizontalInset).roundToPx() }
            val collapsedNavWidth = if (couiNativeCapsule) ballRowWidth else navWidth
            // 收起态「球 + 播放条」贴着容器左右内缘对称落位：
            // 球的可见左缘到屏幕左边界、播放条的右缘到屏幕右边界，两段距离恒等。
            // 12dp 容器内边距 + 2dp = 屏幕两侧各 14dp 留白。
            val compactEdgeInsetPx = with(density) { 2.dp.roundToPx() }
            // 但贴左不能无限贴：竖屏容器和胶囊一样宽，球只差十几个 dp；
            // 横屏/平板上容器比胶囊宽得多，展开态胶囊是居中的，若仍把球钉在容器左缘，
            // 球就得横穿小半个屏幕才能落到展开态左缘上，观感就是"球先飞到中间再展开"。
            // 因此限制球的左缘最多比展开态胶囊左缘靠左 [compactDriftLimitDp]，
            // 超出就贴着展开态左缘落位，整段动画里球只微移、宽度同步向右长开。
            val compactDriftLimitPx = with(density) { 16.dp.roundToPx() }
            val expandedCapsuleLeft = navigationX + if (couiNativeCapsule) couiCapsuleInsetPx else 0
            val compactBallLeftPx = maxOf(compactEdgeInsetPx, expandedCapsuleLeft - compactDriftLimitPx)
            val compactGroup = resolveLinkedDockCompactGroup(
                width = maximumWidth,
                expandedAudioX = stripExpandedX,
                expandedAudioWidth = stripExpandedWidth,
                capsuleInset = if (couiNativeCapsule) couiCapsuleInsetPx else 0,
                button = button,
                gap = gap,
                centered = couiNativeCapsule && !searchEnabled,
                fallbackCapsuleLeft = if (couiNativeCapsule) navigationX else 0,
                compactBallLeft = if (couiNativeCapsule && !searchEnabled) {
                    compactBallLeftPx
                } else {
                    null
                },
                compactAudioRight = if (couiNativeCapsule && !searchEnabled) {
                    (maximumWidth - compactBallLeftPx).coerceAtLeast(0)
                } else {
                    null
                },
                // 收起态两颗球之间留出更宽的缝：融合球的表面张力会把贴太近的两块轮廓焊在一起。
                compactGap = if (couiNativeCapsule && !searchEnabled) {
                    with(density) { COMPACT_MELT_GAP.roundToPx() }
                } else {
                    gap
                },
            )
            // 收起态槽位左缘比展开态更靠左（球贴左、展开归一屏居中），
            // 所以整段动画里横向位置按 merge 平滑过渡，收起↔展开两端各自落在自己的位置上。
            val capsuleLeft = compactGroup.capsuleLeft
            val capsuleLeftExpanded = if (couiNativeCapsule) navigationX else 0
            fun capsuleSlotLeft(mergeValue: Float): Int {
                if (!couiNativeCapsule) return capsuleLeft
                val m = mergeValue.coerceIn(0f, 1f)
                return (capsuleLeft + (capsuleLeftExpanded - capsuleLeft) * (1f - m)).roundToInt()
            }
            // 导航胶囊和播放条共用同一块融合球表面：收起态那颗球就是收窄后的胶囊，
            // 它和旁边的播放条是同一块胶体上的两颗粒子，按压/拖动时整块轮廓一起形变。
            // 判据用 derivedStateOf 收敛成布尔量，避免整条底栏逐帧重组。
            val mergePastExpanded by remember {
                derivedStateOf { merge.value > 0.02f }
            }
            val metaballEligible = couiNativeCapsule && nowPlayingSlot != null && backdrop != null &&
                mergePastExpanded && !reduceMotion
            // 融合球和原生胶囊是同一块材质，交接必须是同一帧完成的硬切。
            // 这段原本是 90/170ms 的淡入淡出，但胶囊的抑制（透明 + 去掉 backdrop + 去掉阴影）
            // 是当帧生效的：折叠/展开各会有几十毫秒「两边都没有玻璃」的空档，
            // 表现就是背景突然消失几帧。材质同源，硬切看不到跳变。
            val metaballActive = metaballEligible
            val metaballWeight = if (metaballEligible) 1f else 0f
            var capsuleShellBounds by remember { mutableStateOf(Rect.Zero) }
            var stripShellBounds by remember { mutableStateOf(Rect.Zero) }
            // 融合球接管期间，播放条把自己的玻璃交给融合球，只保留内容。
            val stripSurfaceProgressProvider: () -> Float = { metaballWeight }
            val homeElasticState = rememberAppElasticPressState(
                visualSize = with(density) { ballSize.toDp() },
            )
            val homeGlassMotion = glassEnabled && backdrop != null
            val homeDragEnabled = phase != LinkedDockPhase.Expanded &&
                com.android.purebilibili.core.ui.LocalComponentMotionEnabled.current &&
                com.android.purebilibili.core.ui.LocalAppThemeConfig.current.globalDragFeedbackEnabled
            var homeDragActive by remember { mutableStateOf(false) }
            // 播放条自己不再记一份形变：融合球要读到同一组数值，整块胶体才会一起动。
            val stripElasticState = rememberAppElasticPressState()

            Box(
                modifier = Modifier.size(
                    with(density) { maximumWidth.toDp() },
                    with(density) { containerHeight.toDp() },
                )
            ) {
                if (metaballActive) {
                    // 融合球和播放条各画一段各自的原生玻璃，只有运动到彼此贴近时才由这里
                    // 把两块轮廓焊成一块；静止时两者之间留出 [COMPACT_MELT_GAP]，是两颗分开的球。
                    // 材质必须和 COUI 原生胶囊完全同源（bar 色 + 同一组混合层），
                    // 否则暗色下融合球是平铺的灰壳，和旁边的深玻璃对不上。
                    DockMeltBallSurface(
                        ballBounds = capsuleShellBounds,
                        ballInsetPx = couiCapsuleInsetPx.toFloat(),
                        stripBounds = stripShellBounds,
                        ballElastic = homeElasticState,
                        stripElastic = stripElasticState,
                        stripAlpha = presence.value.coerceIn(0f, 1f),
                        progress = metaballWeight,
                        extension = MetaBallDefaults.Extension,
                        containerColor = if (couiNativeCapsule) {
                            io.github.suqi8.coui.kmp.theme.COUITheme.colorScheme.bar
                        } else {
                            containerColor
                        },
                        backdrop = backdrop,
                        blurRadius = BottomFloatingNavigationBarDefaults.blurRadius(),
                        containerAlpha = if (isDarkTheme) 0.8f else 0.85f,
                        blurColors = if (couiNativeCapsule) {
                            BottomFloatingNavigationBarDefaults.blurColors(isDarkTheme)
                        } else {
                            null
                        },
                        continuousCurve = couiNativeCapsule,
                        rimColor = Color.White.copy(
                            alpha = if (isDarkTheme) {
                                BottomFloatingNavigationBarDefaults.RIM_ALPHA_DARK
                            } else {
                                BottomFloatingNavigationBarDefaults.RIM_ALPHA_LIGHT
                            },
                        ),
                        modifier = Modifier.matchParentSize(),
                    )
                }

                // ColorOS 17：收拢态的那颗球就是收窄后的 COUI 原生胶囊本身。
                // 如果在这里把它整个卸载，胶囊会先缩成圆再"闪一下消失"，球也就丢了玻璃材质。
                // 所以 COUI17 永远保留这一行，只让它的宽度收成球径。
                if (glassEnabled || !collapsedAtRest || couiNativeCapsule) {
                    Box(
                        modifier = Modifier
                            .offset {
                                // 底栏固定不随滚动上移；横向位置在收起/展开两端不同，
                                // 按 merge 在「贴左的槽位」和「居中后的槽位」之间平滑过渡。
                                IntOffset(capsuleSlotLeft(merge.value), navRowY)
                            }
                            .onGloballyPositioned { coordinates ->
                                capsuleShellBounds = coordinates.boundsInParent()
                            }
                            .layout { measurable, _ ->
                                // ColorOS 17：胶囊随合并进度真实收窄（整条胶囊 → 首页球）。
                                // 宽度每帧重新测量，玻璃、高光、圆角都贴在真实轮廓上，
                                // 所以不会出现"一形变就失去模糊"。
                                val collapse = if (couiNativeCapsule) {
                                    merge.value.coerceIn(0f, 1f)
                                } else {
                                    0f
                                }
                                val width = (
                                    navWidth + (collapsedNavWidth - navWidth) * collapse
                                    ).roundToInt().coerceAtLeast(0)
                                val placeable = measurable.measure(Constraints.fixed(width, barHeight))
                                layout(width, barHeight) { placeable.placeRelative(0, 0) }
                            }
                            .then(
                                if (!couiNativeCapsule && glassEnabled) Modifier.layout { measurable, constraints ->
                                    val placeable = measurable.measure(constraints)
                                    layout(placeable.width, placeable.height) {
                                        if (merge.value < 1f) {
                                            placeable.placeRelative(0, 0)
                                        }
                                    }
                                } else Modifier
                            )
                            .graphicsLayer {
                                // 非 COUI17 皮肤保留原有的退出淡出与轻微收拢。
                                if (!couiNativeCapsule) {
                                    val exitProgress = (merge.value / 0.34f).coerceIn(0f, 1f)
                                    alpha = if (glassEnabled) {
                                        1f - resolveLinkedDockGlassHandoffProgress(merge.value)
                                    } else 1f - EaseInCubic.transform(exitProgress)
                                    val collapse = EaseOutCubic.transform(merge.value.coerceIn(0f, 1f))
                                    scaleX = 1f - 0.10f * collapse
                                    scaleY = 1f - 0.10f * collapse
                                    transformOrigin = TransformOrigin(0f, 0.5f)
                                } else if (!metaballEligible) {
                                    // 收起态那颗球就是这条胶囊本身：按压/拖动的形变必须作用到
                                    // 玻璃上，否则只有手指下的高光在动、球本身纹丝不动。
                                    // 融合球接管时胶囊已经透明，改由融合球的粒子承担这段形变，
                                    // 这里必须让开，否则位移会被算两次。
                                    homeElasticState.layerBlock(this)
                                }
                            }
                            .pointerInput(phase, couiNativeCapsule) {
                                // COUI17 收拢态仍然显示胶囊（它本身就是那颗球），
                                // 所以不能吞事件，否则首页球按钮点不动。
                                if (!couiNativeCapsule && phase != LinkedDockPhase.Expanded) {
                                    awaitPointerEventScope {
                                        while (true) {
                                            awaitPointerEvent(PointerEventPass.Initial)
                                                .changes.forEach { it.consume() }
                                        }
                                    }
                                }
                            }
                            .then(
                                if (phase != LinkedDockPhase.Expanded) {
                                    Modifier.clearAndSetSemantics {}
                                } else {
                                    Modifier
                                }
                            ),
                    ) {
                        CompositionLocalProvider(
                            LocalLinkedDockShellSuppressed provides metaballActive,
                            LocalLinkedDockCollapseProgress provides mergeProgressProvider,
                        ) {
                            if (compactOnScrollEnabled && !isCoui17) {
                                ScrollCompactNavigation(
                                    compact = useNavigationCompact && compactNavigation && phase == LinkedDockPhase.Expanded,
                                    currentItem = currentItem,
                                    currentLabel = currentLabel,
                                    iconStyle = iconStyle,
                                    expandedWidthPx = navWidth,
                                    expandedHeightPx = barHeight,
                                    buttonSizePx = button.coerceAtMost(navWidth),
                                    backdrop = backdrop,
                                    containerColor = containerColor,
                                    glassEnabled = glassEnabled,
                                    blurEnabled = blurEnabled,
                                    hazeState = hazeState,
                                    liquidGlassTuning = liquidGlassTuning,
                                    onExpand = { compactNavigation = false },
                                    content = navigationContent,
                                )
                            } else navigationContent()
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .offset {
                            // ColorOS 17 下这颗球就是收窄后的 COUI 原生胶囊本身，
                            // 圆心必须与胶囊圆心严格重合；其它皮肤仍在最左侧。
                            val x = if (couiNativeCapsule) {
                                capsuleSlotLeft(merge.value) + couiCapsuleInsetPx
                            } else 0
                            IntOffset(x, navRowY + (barHeight - ballSize) / 2)
                        }
                        .size(
                            with(density) { ballSize.toDp() },
                            with(density) { ballSize.toDp() },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    // Keep placement outside the elastic observer: its gesture geometry must
                    // be the visible button, rather than the full dock before offset/size.
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .appElasticPress(
                                enabled = phase != LinkedDockPhase.Expanded,
                                state = homeElasticState,
                                transformInBackdrop = homeGlassMotion && !couiNativeCapsule,
                                // COUI17 那颗球是收窄后的原生胶囊，高光改用底栏同一束聚光，
                                // 不再叠这层白光，否则和底栏的观感对不上。
                                highlightEnabled = !couiNativeCapsule,
                                allowConsumedDrag = { homeDragActive },
                            )
                            .then(
                                // A pointerInput node still participates in hit testing even
                                // when its coroutine returns immediately. The transparent home
                                // button overlaps the first navigation slot in expanded mode.
                                if (homeDragEnabled) Modifier.pointerInput(homeElasticState) {
                                    // Own decorative drag after touch slop; cancel the click
                                    // instead of expanding the dock on drag release.
                                    try {
                                        detectDragGestures(
                                            onDragStart = { homeDragActive = true },
                                            onDragEnd = { homeDragActive = false },
                                            onDragCancel = { homeDragActive = false },
                                        ) { change, _ -> change.consume() }
                                    } finally {
                                        homeDragActive = false
                                    }
                                } else Modifier
                            )
                            .graphicsLayer {
                                if (couiNativeCapsule) {
                                    // 胶囊收窄成球时图标才浮现，落点与球心重合，无第二次落位。
                                    val collapse = merge.value.coerceIn(0f, 1f)
                                    val appear = ((collapse - 0.45f) / 0.35f).coerceIn(0f, 1f)
                                    alpha = appear
                                    val scale = 0.86f + 0.14f * appear
                                    scaleX = scale
                                    scaleY = scale
                                } else {
                                    // 进入窗口（前 50%）ease-out 淡入并从 92% 缩放弹出，落定干净无过冲。
                                    val enterProgress = (merge.value / 0.5f).coerceIn(0f, 1f)
                                    val eased = EaseOutCubic.transform(enterProgress)
                                    // One complementary handoff prevents both glass shells from
                                    // brightening the same region during merge and reversal.
                                    alpha = if (glassEnabled) {
                                        resolveLinkedDockGlassHandoffProgress(merge.value)
                                    } else eased
                                    val scale = 0.92f + 0.08f * eased
                                    scaleX = scale
                                    scaleY = scale
                                }
                            }
                            .then(
                                if (couiNativeCapsule) {
                                    Modifier.couiDockSpotLight(homeElasticState, isDarkTheme)
                                } else Modifier
                            )
                            .then(
                                if (phase != LinkedDockPhase.Expanded) {
                                    Modifier.clickable(role = Role.Button) { expand() }
                                } else {
                                    Modifier.clearAndSetSemantics {}
                                }
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        // ColorOS 17：球的外壳就是收窄后的 COUI 原生胶囊，这里绝不能再叠一层，
                        // 否则深色下会露出第二圈发白的内壳。
                        if (!couiNativeCapsule) {
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .biliPaiFloatingDockShell(
                                        backdrop = backdrop,
                                        interactionLayerBlock = homeElasticState.layerBlock.takeIf { homeGlassMotion },
                                        interactionState = homeElasticState.takeIf { homeGlassMotion },
                                        containerColor = containerColor,
                                        pressProgress = 0f,
                                        shape = shape,
                                        enabled = glassEnabled,
                                        blurEnabled = blurEnabled,
                                        hazeState = hazeState,
                                        liquidGlassTuning = liquidGlassTuning,
                                    )
                            )
                        }
                        AppIcon(
                            imageVector = if (iconStyle == SharedFloatingBottomBarIconStyle.MIUIX) {
                                resolveHomeNavigationBarIcon(firstItem, currentItem == firstItem)
                            } else resolveMaterialBottomBarIcon(firstItem, currentItem == firstItem),
                            contentDescription = "$firstLabel，展开底栏",
                            tint = navigationColor,
                            // COUI17 的形变已经由外层 appElasticPress 加在整颗球上，
                            // 这里再叠一层就会把图标放大两次。
                            modifier = if (homeGlassMotion && !couiNativeCapsule) {
                                Modifier.graphicsLayer(homeElasticState.layerBlock)
                            } else {
                                Modifier
                            },
                        )
                    }
                }
                if (nowPlayingSlot != null) {
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                alpha = resolveAudioNowPlayingPresenceAlpha(presenceProgressProvider())
                                // 首次出现：从下方上滑落位，弹簧 overshoot 时越过终点
                                // 再回落即为轻微回弹；非入场（退出/静止）不附加位移。
                                translationY = if (!slotEntering.value || reduceMotion) {
                                    0f
                                } else {
                                    (1f - presence.value) *
                                        AUDIO_NOW_PLAYING_PRESENCE_ENTER_SLIDE_DP.dp.toPx()
                                }
                            }
                            .layout { measurable, _ ->
                                // 小横条槽宽度真实随 morph/presence 变化，是唯一保留
                                // 每帧测量的子树；读进度只触发本槽自身的重新测量。
                                val geometry = resolveLinkedDockGeometry(
                                    width = maximumWidth,
                                    button = button,
                                    barHeight = barHeight,
                                    gap = gap,
                                    hasAudio = true,
                                    searchEnabled = searchEnabled,
                                    mergeProgress = merge.value,
                                    searchProgress = search.value,
                                    verticalGap = verticalGap,
                                    presenceProgress = slotGeometryPresenceProvider(),
                                    expandedAudioWidth = stripExpandedWidth,
                                    expandedAudioX = stripExpandedX,
                                    compactAudioLeft = compactGroup.audioLeft,
                                    compactAudioRight = compactGroup.audioRight,
                                )
                                // 槽高与导航行同为 barHeight：两行胶囊圆角（percent=50）
                                // 严格一致，不会因槽更矮而出现更小的圆角。
                                val placeable = measurable.measure(
                                    Constraints.fixed(geometry.audioWidth, barHeight)
                                )
                                layout(geometry.audioWidth, barHeight) {
                                    placeable.placeRelative(0, 0)
                                }
                            }
                            .offset {
                                val geometry = resolveLinkedDockGeometry(
                                    width = maximumWidth,
                                    button = button,
                                    barHeight = barHeight,
                                    gap = gap,
                                    hasAudio = true,
                                    searchEnabled = searchEnabled,
                                    mergeProgress = merge.value,
                                    searchProgress = search.value,
                                    verticalGap = verticalGap,
                                    presenceProgress = slotGeometryPresenceProvider(),
                                    expandedAudioWidth = stripExpandedWidth,
                                    expandedAudioX = stripExpandedX,
                                    compactAudioLeft = compactGroup.audioLeft,
                                    compactAudioRight = compactGroup.audioRight,
                                )
                                // 槽高已与导航行同为 barHeight：展开时槽底贴导航行上方
                                // verticalGap 处；收合时槽与导航行完全重合（圆心同中心）。
                                IntOffset(
                                    geometry.audioX,
                                    navRowY - verticalGap - barHeight +
                                        ((barHeight + verticalGap) * merge.value).roundToInt(),
                                )
                            }
                            .onGloballyPositioned { coordinates ->
                                stripShellBounds = coordinates.boundsInParent()
                            },
                    ) {
                        androidx.compose.runtime.CompositionLocalProvider(
                            com.android.purebilibili.core.ui.LocalVisualWorkAllowed provides
                                (com.android.purebilibili.core.ui.LocalVisualWorkAllowed.current && hasAudio),
                            // 播放条的按压形变交给底栏这一份，融合球才能读到同一组数值。
                            com.android.purebilibili.feature.audio.screen
                                .LocalAudioNowPlayingElasticState provides stripElasticState,
                        ) {
                            nowPlayingSlot.invoke(
                                Modifier.fillMaxSize(),
                                mergeProgressProvider,
                                searchProgressProvider,
                                stripSurfaceProgressProvider,
                                if (shouldExpandPlaybackFromSearch(phase, hasAudio)) {
                                    {
                                        if (!transition.isRunning) {
                                            focusManager.clearFocus()
                                            keyboardController?.hide()
                                            updatePhase(LinkedDockPhase.Playback)
                                        }
                                    }
                                } else {
                                    null
                                },
                                nowPlayingLayoutStable,
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            // Geometry remains bounded; the spring's overshoot is visible at the edge.
                            val overshoot = search.value - search.value.coerceIn(0f, 1f)
                            translationX = overshoot * 24.dp.toPx()
                        }
                        .offset {
                            val geometry = resolveLinkedDockGeometry(
                                width = maximumWidth,
                                button = button,
                                barHeight = barHeight,
                                gap = gap,
                                hasAudio = hasAudio,
                                searchEnabled = searchEnabled,
                                mergeProgress = merge.value,
                                searchProgress = search.value,
                                verticalGap = verticalGap,
                            )
                            IntOffset(
                                resolveLinkedDockSearchX(
                                    maximumWidth = maximumWidth,
                                    navigationWidth = navWidth,
                                    searchWidth = geometry.searchWidth,
                                    button = button,
                                    gap = gap,
                                    mergeProgress = merge.value,
                                    searchProgress = search.value,
                                ),
                                controlRowY + (controlHeight - searchVisualHeight()) / 2,
                            )
                        }
                        .layout { measurable, _ ->
                            val geometry = resolveLinkedDockGeometry(
                                width = maximumWidth,
                                button = button,
                                barHeight = barHeight,
                                gap = gap,
                                hasAudio = hasAudio,
                                searchEnabled = searchEnabled,
                                mergeProgress = merge.value,
                                searchProgress = search.value,
                                verticalGap = verticalGap,
                            )
                            val placeable = measurable.measure(
                                Constraints.fixed(geometry.searchWidth, searchVisualHeight())
                            )
                            layout(geometry.searchWidth, searchVisualHeight()) {
                                placeable.placeRelative(0, 0)
                            }
                        },
                ) {
                    if (searchEnabled) {
                        val searchElasticState = rememberAppElasticPressState(
                            feedbackStrength = com.android.purebilibili.core.ui.components.APP_SEARCH_ELASTIC_FEEDBACK_STRENGTH,
                        )
                        val searchGlassMotion = glassEnabled && backdrop != null
                        // The shell and hit target are siblings. Deform their common
                        // parent so glass, icon and pointer feedback stay together.
                        Box(
                            Modifier
                                .fillMaxSize()
                                .appElasticPress(
                                    enabled = phase != LinkedDockPhase.Search,
                                    state = searchElasticState,
                                    transformInBackdrop = searchGlassMotion,
                                )
                        ) {
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .biliPaiFloatingDockShell(
                                        backdrop = backdrop,
                                        interactionLayerBlock = searchElasticState.layerBlock.takeIf { searchGlassMotion },
                                        interactionState = searchElasticState.takeIf { searchGlassMotion },
                                        containerColor = containerColor,
                                        pressProgress = 0f,
                                        shape = shape,
                                        enabled = glassEnabled,
                                        blurEnabled = blurEnabled,
                                        hazeState = hazeState,
                                        liquidGlassTuning = liquidGlassTuning,
                                    )
                            )
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .then(if (searchGlassMotion) Modifier.graphicsLayer(searchElasticState.layerBlock) else Modifier)
                                    .clip(shape)
                                    .then(
                                        if (phase != LinkedDockPhase.Search) {
                                            Modifier.clickable(role = Role.Button) {
                                                searchHaptic(com.android.purebilibili.core.util.HapticType.LIGHT)
                                                phaseBeforeSearch = phase
                                                pendingUserImeRequest = true
                                                updatePhase(LinkedDockPhase.Search)
                                            }
                                        } else Modifier
                                    )
                            ) {
                                BiliPaiBottomBarSearchVisualContent(
                                    expanded = phase == LinkedDockPhase.Search ||
                                        phase == LinkedDockPhase.Compact,
                                    query = query,
                                    onQueryChange = { query = it },
                                    onSubmit = {
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                        val keyword = query.trim()
                                        query = ""
                                        if (keyword.isBlank()) {
                                            onSearchClick()
                                        } else {
                                            onSearchKeywordSubmit(keyword)
                                            pendingUserImeRequest = false
                                            updatePhase(
                                                resolveLinkedDockPhaseOnSearchDismiss(
                                                    hasAudio = hasAudio,
                                                    previousPhase = phaseBeforeSearch,
                                                )
                                            )
                                        }
                                    },
                                    contentColor = contentColor,
                                    accentColor = accentColor,
                                    iconScale = identityIconScaleProvider,
                                    fieldAlpha = searchProgressProvider,
                                    interactive = phase == LinkedDockPhase.Search,
                                    iconStyle = iconStyle,
                                    glassSearchVisuals = glassEnabled,
                                    pendingUserImeRequest = pendingUserImeRequest,
                                    onUserImeRequestConsumed = { pendingUserImeRequest = false },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 底栏的融合球表面：导航胶囊（收起时就是那颗球）和播放条是同一块胶体上的两颗粒子。
 *
 * 粒子取两者当帧真实外框，形变取各自 [AppElasticPressState] 的实时数值 —— 于是按压或拖动
 * 那颗球时，拉伸与位移发生在胶体轮廓上，而不是只有手指下的一层高光在动。
 *
 * 形变数值只在这里读取，重组范围因此收敛到这一块表面：收起态静止时它不会跟着底栏一起重组。
 */
@Composable
private fun DockMeltBallSurface(
    ballBounds: Rect,
    ballInsetPx: Float,
    stripBounds: Rect,
    ballElastic: AppElasticPressState,
    stripElastic: AppElasticPressState,
    stripAlpha: Float,
    progress: Float,
    extension: Dp,
    containerColor: Color,
    backdrop: Backdrop?,
    blurRadius: Float,
    containerAlpha: Float,
    blurColors: List<io.github.suqi8.coui.kmp.blur.BlendColorEntry>? = null,
    continuousCurve: Boolean = false,
    rimColor: Color,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    // 导航槽位左右各留一圈 COUI 内缩，可见胶囊比槽位窄；粒子要贴着可见轮廓。
    val ball = ballBounds.run { Rect(left + ballInsetPx, top, right - ballInsetPx, bottom) }
    if (ball.isEmpty || stripBounds.isEmpty) return
    val ballTranslation = ballElastic.translation
    val ballScaleX = ballElastic.scaleX
    val ballScaleY = ballElastic.scaleY
    val stripTranslation = stripElastic.translation
    val stripScaleX = stripElastic.scaleX
    val stripScaleY = stripElastic.scaleY
    // 按住或拖动任何一颗球时，两块轮廓的场域一起向外扩：两颗之间真的拉出一条颈，
    // 就是「融在一起」的那一下；松手回到 4dp，静止时仍然是两颗分开的球。
    val meltPull = maxOf(ballElastic.pressProgress, stripElastic.pressProgress).coerceIn(0f, 1f)
    val pullExtension = extension + (MELT_PULL_EXTENSION_DP * meltPull).dp
    MetaBallSurface(
        particles = listOf(
            MetaBallParticle(
                centerX = with(density) { (ball.center.x + ballTranslation.x).toDp() },
                centerY = with(density) { (ball.center.y + ballTranslation.y).toDp() },
                width = with(density) { ball.width.toDp() },
                height = with(density) { ball.height.toDp() },
                scaleX = ballScaleX,
                scaleY = ballScaleY,
            ),
            MetaBallParticle(
                centerX = with(density) { (stripBounds.center.x + stripTranslation.x).toDp() },
                centerY = with(density) { (stripBounds.center.y + stripTranslation.y).toDp() },
                width = with(density) { stripBounds.width.toDp() },
                height = with(density) { stripBounds.height.toDp() },
                scaleX = stripScaleX,
                scaleY = stripScaleY,
                alpha = stripAlpha,
            ),
        ),
        modifier = modifier.graphicsLayer { alpha = progress.coerceIn(0f, 1f) },
        color = containerColor,
        backdrop = backdrop,
        blurRadius = blurRadius,
        containerAlpha = containerAlpha,
        blurColors = blurColors,
        continuousCurve = continuousCurve,
        extension = pullExtension,
        rimColor = rimColor,
        rimBottomScale = 1f,
    )
}

/**
 * 底栏那颗球的按压高光，直接复用 COUI 胶囊自己的聚光配方（同一套 shader、同一组参数、
 * 同一种混合模式），所以收起态的球和展开态的底栏是同一束光。
 *
 * 球是独立的一层，原生胶囊的 `couiTouchMotion` 收不到这里的触摸（球盖在上层赢得了命中测试），
 * 所以由这里把聚光补在球自己的节点上：位置取归一化的指尖坐标，半径/强度按按压进度生长，
 * 和 COUI 的 `couiSpotlightRadius` 一致。
 */
@Composable
private fun Modifier.couiDockSpotLight(
    state: AppElasticPressState,
    darkTheme: Boolean,
): Modifier {
    val density = LocalDensity.current
    val params = io.github.suqi8.coui.kmp.basic.SpotLightDefaults.opaqueMedium2(darkTheme)
    val progress = state.pressProgress.coerceIn(0f, 1f)
    val radius = with(density) {
        (params.startRadius + (params.endRadius - params.startRadius) * progress).toDp()
    }
    return this.couiSpotLight(
        position = state.highlightCenter,
        radius = radius,
        intensity = params.endIntensity * progress,
        lightHeight = with(density) { params.lightHeight.toDp() },
        ambientStrength = params.ambientStrength,
        diffuseStrength = params.diffuseStrength,
        noise = params.noise,
        dither = params.ditherN,
        blendMode = params.blendMode,
        clipShape = CircleShape,
    )
}
