package com.android.purebilibili.feature.home.components

import com.android.purebilibili.core.ui.AppContentColors

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.snap
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.ui.components.AppIcon
import com.android.purebilibili.core.ui.components.appElasticPress
import com.android.purebilibili.core.ui.components.rememberAppElasticPressState
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
        val zeroProgressProvider = remember { { 0f } }
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
                verticalGap = 4.dp.roundToPx()
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
            val reservedSearchWidth = if (searchEnabled) button + gap else 0
            val expandedNavigationWidth = preferredNavigationWidth.coerceAtMost(
                (maximumWidth - reservedSearchWidth).coerceAtLeast(0)
            )
            val navWidth = expandedNavigationWidth.coerceAtMost(maximumWidth)
            val navigationX = resolveLinkedDockNavigationX(
                maximumWidth = maximumWidth,
                navigationWidth = navWidth,
                button = button,
                gap = gap,
                searchEnabled = searchEnabled,
            )
            // 小横条展开态与底栏整簇（导航胶囊 + 搜索圆钮）同宽同起点：
            // 两行胶囊长度一致、左右边缘对齐。
            val bottomBarClusterWidth = (navWidth + reservedSearchWidth).coerceAtMost(maximumWidth)

            // Glass keeps the navigation/backdrop mounted across both endpoints. At the compact
            // endpoint skip placement so the hidden row neither draws nor intercepts touches.
            // Native toolbars retain their original composition and motion behavior.
            val collapsedAtRest = phase != LinkedDockPhase.Expanded && !transition.isRunning

            val isCoui17 = LocalAppUiStyle.current == AppUiStyle.COUI17
            val isDarkTheme = com.android.purebilibili.core.ui.isAppInDarkTheme()
            val couiSmallHighlight = rememberGlassHighlight(GlassContainer.Small, backdrop)
            val ballSize = button
            // ColorOS 17 的材质完全由 COUI 原生胶囊（BottomFloatingNavigationBar）承担：
            // 不再叠加任何自绘外壳或融合球背景，收缩时只让胶囊自身收窄成圆，
            // 玻璃采样始终落在真实几何上，不会出现"多套一层宽背景"或"一动就失去模糊"。
            val couiNativeCapsule = isCoui17
            // COUI 胶囊在窄屏上左右各留 16dp 外边距，收窄到「球」时必须把这段算进去，
            // 否则胶囊永远收不成正圆。
            val couiCapsuleHorizontalInset =
                BottomFloatingNavigationBarDefaults.PaddingHorizontal * 2
            val ballRowWidth = with(density) { (ballSize.toDp() + couiCapsuleHorizontalInset).roundToPx() }
            val collapsedNavWidth = if (couiNativeCapsule) ballRowWidth else navWidth
            // 播放条与导航胶囊是两个独立元素，不参与任何融合球合并。
            val stripSurfaceProgressProvider: () -> Float = zeroProgressProvider

            Box(
                modifier = Modifier.size(
                    with(density) { maximumWidth.toDp() },
                    with(density) { containerHeight.toDp() },
                )
            ) {
                // ColorOS 17：收拢态的那颗球就是收窄后的 COUI 原生胶囊本身。
                // 如果在这里把它整个卸载，胶囊会先缩成圆再"闪一下消失"，球也就丢了玻璃材质。
                // 所以 COUI17 永远保留这一行，只让它的宽度收成球径。
                if (glassEnabled || !collapsedAtRest || couiNativeCapsule) {
                    Box(
                        modifier = Modifier
                            .offset {
                                // 底栏固定在原位，绝不随滚动上移/位移。
                                IntOffset(navigationX, navRowY)
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
                            LocalLinkedDockShellSuppressed provides false,
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
                val homeElasticState = rememberAppElasticPressState(
                    visualSize = with(density) { ballSize.toDp() },
                )
                val homeGlassMotion = glassEnabled && backdrop != null
                val homeDragEnabled = phase != LinkedDockPhase.Expanded &&
                    com.android.purebilibili.core.ui.LocalComponentMotionEnabled.current &&
                    com.android.purebilibili.core.ui.LocalAppThemeConfig.current.globalDragFeedbackEnabled
                var homeDragActive by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .offset {
                            // ColorOS 17 下这颗球就是收窄后的 COUI 原生胶囊本身，
                            // 圆心必须与胶囊圆心严格重合；其它皮肤仍在最左侧。
                            val x = if (couiNativeCapsule) {
                                navigationX + with(density) {
                                    BottomFloatingNavigationBarDefaults.PaddingHorizontal.roundToPx()
                                }
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
                            modifier = if (homeGlassMotion) Modifier.graphicsLayer(homeElasticState.layerBlock) else Modifier,
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
                                    expandedAudioWidth = bottomBarClusterWidth,
                                    expandedAudioX = navigationX,
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
                                    expandedAudioWidth = bottomBarClusterWidth,
                                    expandedAudioX = navigationX,
                                )
                                // 槽高已与导航行同为 barHeight：展开时槽底贴导航行上方
                                // verticalGap 处；收合时槽与导航行完全重合（圆心同中心）。
                                IntOffset(
                                    geometry.audioX,
                                    navRowY - verticalGap - barHeight +
                                        ((barHeight + verticalGap) * merge.value).roundToInt(),
                                )
                            },
                    ) {
                        androidx.compose.runtime.CompositionLocalProvider(
                            com.android.purebilibili.core.ui.LocalVisualWorkAllowed provides
                                (com.android.purebilibili.core.ui.LocalVisualWorkAllowed.current && hasAudio),
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
