package com.android.purebilibili.navigation3

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.rememberNavigationEventState
import com.android.purebilibili.feature.settings.screen.settingsHasPersistentPanes
import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.core.ui.globalWallpaperAwareBackground
import com.android.purebilibili.feature.home.shouldRenderGlobalHomeWallpaperBackdrop
import com.android.purebilibili.core.store.HomeWallpaperEffectScope
import com.android.purebilibili.core.ui.LocalGlobalWallpaperBackdropVisible
import com.android.purebilibili.core.ui.LocalSharedTransitionEnabled
import com.android.purebilibili.core.ui.LocalVideoDetailNavigationTransitionScope
import com.android.purebilibili.core.ui.transition.LocalClickToPlayEnabled
import com.android.purebilibili.core.ui.transition.LocalDynamicImagePreviewTextVisible
import com.android.purebilibili.core.ui.transition.LocalDynamicCommentPreviewVisible
import com.android.purebilibili.core.ui.transition.LocalVideoCardSharedElementSourceRoute
import com.android.purebilibili.core.ui.transition.LocalMiuixVideoCardTransitionState
import com.android.purebilibili.core.ui.transition.LocalVideoCardTransitionBackgroundState
import com.android.purebilibili.core.ui.transition.MiuixVideoCardTransitionState
import com.android.purebilibili.core.ui.transition.VideoCardTransitionBackgroundState
import com.android.purebilibili.core.ui.transition.VideoCardTransitionExposure
import com.android.purebilibili.core.ui.transition.LocalVideoCardTransitionClock
import com.android.purebilibili.core.ui.transition.LocalPredictiveBackBackgroundState
import com.android.purebilibili.core.ui.transition.PredictiveBackBackgroundState
import com.android.purebilibili.core.ui.transition.VideoCardTransitionClock
import com.android.purebilibili.core.ui.transition.VideoCardTransitionHostDepthLayer
import com.android.purebilibili.core.ui.transition.rememberVideoCardTransitionSnapshotHandle
import com.android.purebilibili.core.ui.transition.resolveVideoCardTransitionExposure
import com.android.purebilibili.core.ui.transition.resolveVideoHeroMotionSpec
import com.android.purebilibili.core.ui.transition.VideoCardTransitionBackgroundPhase
import com.android.purebilibili.core.ui.transition.VideoCardTransitionSettleState
import com.android.purebilibili.core.ui.transition.VideoCardTransitionDiagnostics
import com.android.purebilibili.core.ui.transition.LocalVideoSharedTransitionSpeedSettings
import com.android.purebilibili.core.ui.transition.resolvePredictiveBackGestureBlurProgress
import com.android.purebilibili.core.ui.transition.shouldReleaseHostOwnedDepthLayer
import com.android.purebilibili.core.ui.transition.shouldUseHostOwnedVideoCardTransitionSnapshot
import com.android.purebilibili.core.ui.adaptive.MotionTier
import com.android.purebilibili.core.util.CardPositionManager
import com.android.purebilibili.navigation3.predictiveback.VideoCardBackCompletionPolicy
import com.android.purebilibili.navigation3.predictiveback.VideoCardMorphDriver
import com.android.purebilibili.navigation3.predictiveback.BiliPaiPredictiveBackAnimationStyle
import com.android.purebilibili.navigation3.predictiveback.BiliPaiPredictiveBackExitDirection
import com.android.purebilibili.navigation3.predictiveback.MIUIX_PREDICTIVE_BACK_DEFAULT_MAX_PROGRESS_PERCENT
import com.android.purebilibili.navigation3.predictiveback.biliPaiMiuixNavTransition
import com.android.purebilibili.navigation3.predictiveback.miuixVideoCardNavTransition
import com.android.purebilibili.navigation3.predictiveback.MiuixVideoCardContentScale
import com.android.purebilibili.navigation3.predictiveback.resolveMiuixVideoCardContentScaleForSourceLayout
import com.android.purebilibili.navigation3.predictiveback.MiuixVideoCardTransitionProgress
import com.android.purebilibili.navigation3.predictiveback.MiuixVideoCardCoveredShapeCache
import com.android.purebilibili.navigation3.predictiveback.resolveMiuixVideoCardCoveredCornerPx
import com.android.purebilibili.navigation3.predictiveback.resolveMiuixVideoCardCoveredScale
import com.android.purebilibili.navigation3.predictiveback.resolveMiuixVideoCardBackingColor
import com.android.purebilibili.navigation3.predictiveback.shouldUseMiuixPredictiveBackProgress
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.nav.core.NavBackStack
import top.yukonga.miuix.kmp.nav.core.NavCornerClipMode
import top.yukonga.miuix.kmp.nav.core.NavDisplay
import top.yukonga.miuix.kmp.nav.core.NavDisplayEffects
import top.yukonga.miuix.kmp.nav.core.LocalNavTransitionScope
import top.yukonga.miuix.kmp.nav.transition.NavSwipeDirection

internal class BiliPaiProgrammaticBackDispatcher {
    private var callback: (() -> Unit)? = null

    fun register(callback: () -> Unit) {
        this.callback = callback
    }

    fun unregister(callback: () -> Unit) {
        if (this.callback === callback) this.callback = null
    }

    fun dispatch(): Boolean {
        val action = callback ?: return false
        action()
        return true
    }
}

@Composable
internal fun BiliPaiNavDisplayHost(
    backStack: SnapshotStateList<BiliPaiNavKey>,
    activeMainHostRoute: String? = null,
    cardTransitionEnabled: Boolean = true,
    videoTransitionRealtimeBlurEnabled: Boolean = false,
    isLightBackground: Boolean = false,
    reduceMotion: Boolean = false,
    videoSharedTransitionDurationMillis: Int,
    videoCardClock: VideoCardTransitionClock,
    predictiveBackAnimationStyle: BiliPaiPredictiveBackAnimationStyle =
        BiliPaiPredictiveBackAnimationStyle.MIUIX,
    predictiveBackExitDirection: BiliPaiPredictiveBackExitDirection =
        BiliPaiPredictiveBackExitDirection.ALWAYS_RIGHT,
    miuixTransitionBlurEnabled: Boolean = true,
    miuixPredictiveBackMaxProgressPercent: Int =
        MIUIX_PREDICTIVE_BACK_DEFAULT_MAX_PROGRESS_PERCENT,
    videoSharedReturnGestureFollowEnabled: Boolean = true,
    videoSharedReturnGestureTranslationEnabled: Boolean = true,
    videoReturnContentFollowProgressEnabled: Boolean = true,
    settingsContainerMorphEnabled: Boolean = true,
    settingsContainerRealtimeBlurEnabled: Boolean = false,
    sourceMetadata: BiliPaiNavSourceMetadata,
    programmaticBackDispatcher: BiliPaiProgrammaticBackDispatcher,
    preferWholeCardReturn: Boolean = false,
    onBack: () -> Unit,
    onOpenCardContainer: (BiliPaiNavKey) -> Unit = { backStack.add(it) },
    onPrepareVideoCardSharedReturn: () -> Boolean = { false },
    pendingRelatedReturnCount: Int = 0,
    relatedReturnRequestId: Long = 0L,
    onRelatedVideoDetailReturned: (Long, Long) -> Boolean = { _, _ -> false },
    videoSourceGeneration: Long = 0L,
    modifier: Modifier = Modifier,
    content: @Composable (BiliPaiNavKey) -> Unit,
) = BoxWithConstraints(modifier = modifier) {
    val density = LocalDensity.current.density
    val hostBounds = if (constraints.hasBoundedWidth && constraints.hasBoundedHeight) {
        Rect(0f, 0f, constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat())
    } else null
    // Resolve geometry once in host-local px. Never independently retime individual layers.
    val heroMotion = remember(sourceMetadata.sourceBounds, hostBounds, density,
        videoSharedTransitionDurationMillis, reduceMotion) {
        resolveVideoHeroMotionSpec(videoSharedTransitionDurationMillis,
            sourceMetadata.sourceBounds, hostBounds, density, reduceMotion)
    }
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val visualGuard = com.android.purebilibili.core.ui.performance.LocalRuntimeVisualGuard.current
    // 在图层阶段读取运行时预算，降级不重建整棵导航树；玻璃与原生路径共用此保护。
    val navBlurMotionTierProvider = remember(visualGuard) {
        {
            val decision = visualGuard.value
            if (decision.forceLowBlurBudget) MotionTier.Reduced else decision.effectiveMotionTier
        }
    }
    val speedSettings = LocalVideoSharedTransitionSpeedSettings.current
    val diagnosticConfiguration by rememberUpdatedState(
        "speed=${speedSettings.speed} custom_duration=${speedSettings.customDurationMillis} " +
            "realtime_blur=$videoTransitionRealtimeBlurEnabled gesture_follow=$videoSharedReturnGestureFollowEnabled " +
            "gesture_translation=$videoSharedReturnGestureTranslationEnabled " +
            "predictive_style=$predictiveBackAnimationStyle reduced_motion=$reduceMotion",
    )
    val stackSnapshot = backStack.toList()
    val settingsPersistentPanes = settingsHasPersistentPanes()
    val settingsContainerSources = remember { com.android.purebilibili.feature.settings.SettingsContainerSources() }
    val settingsContainerEnabled = settingsContainerMorphEnabled && !settingsPersistentPanes && !reduceMotion
    // 颜色按当前主题读取，不能以调色动画的每一帧重建容器转场。
    val settingsContainerPageColor = rememberUpdatedState(AppSurfaceTokens.groupedListContainer())
    val settingsContainerSnapshot = settingsContainerSources.sources.toMap()
    val availableSettingsContainerTransitions = remember(
        settingsContainerSnapshot, settingsContainerRealtimeBlurEnabled, navBlurMotionTierProvider,
    ) {
        settingsContainerSnapshot.mapValues { (_, source) ->
            SettingsContainerTransition(
                source, pageColor = { settingsContainerPageColor.value },
                realtimeBlurEnabled = settingsContainerRealtimeBlurEnabled,
                blurMotionTierProvider = navBlurMotionTierProvider,
            )
        }
    }
    val settingsContainerTransitions = if (!settingsContainerEnabled) emptyMap() else
        availableSettingsContainerTransitions.filterKeys { target ->
            isSettingsContainerBoundary(
                enabled = settingsContainerEnabled,
                backStack = stackSnapshot,
                target = target,
                owner = settingsContainerSnapshot[target]?.owner,
            )
        }
    LaunchedEffect(stackSnapshot, settingsContainerSnapshot.keys) {
        // 离场 entry 保留自己的转场对象；目录中清除旧来源，搜索直达不能复用上次点击。
        settingsContainerSources.sources.keys.retainAll(stackSnapshot.toSet())
    }
    val liveContainerSources = remember { com.android.purebilibili.feature.settings.SettingsContainerSources() }
    val liveContainerEnabled = cardTransitionEnabled && !reduceMotion &&
        com.android.purebilibili.core.ui.LocalComponentMotionEnabled.current
    val liveContainerSnapshot = liveContainerSources.sources.toMap()
    val liveContainerPageColor = rememberUpdatedState(
        com.android.purebilibili.feature.live.resolveLiveBiliPaiRoomColorTokens().baseBackgroundColor,
    )
    val webContainerPageColor = rememberUpdatedState(AppSurfaceTokens.surface())
    val articleContainerPageColor = rememberUpdatedState(AppSurfaceTokens.background())
    val liveContainerIsLightBackground = rememberUpdatedState(isLightBackground)
    val availableLiveContainerTransitions = remember(
        liveContainerSnapshot, videoSharedTransitionDurationMillis,
        videoTransitionRealtimeBlurEnabled, settingsContainerRealtimeBlurEnabled, navBlurMotionTierProvider,
    ) {
        liveContainerSnapshot.mapValues { (target, source) ->
            val articleTarget = target is BiliPaiNavKey.ArticleDetail || target is BiliPaiNavKey.DynamicDetail
            val generalContainer = target is BiliPaiNavKey.Web || target is BiliPaiNavKey.Space || target is BiliPaiNavKey.BangumiDetail
            SettingsContainerTransition(
                source = source,
                pageColor = { when {
                    target is BiliPaiNavKey.BangumiPlayer -> androidx.compose.ui.graphics.Color.Black
                    articleTarget -> articleContainerPageColor.value
                    generalContainer -> webContainerPageColor.value
                    else -> liveContainerPageColor.value
                } },
                durationMillis = if (generalContainer) SETTINGS_CONTAINER_DURATION_MILLIS
                    else videoSharedTransitionDurationMillis.coerceAtLeast(1),
                // 专栏使用视频卡片的导航时钟、曲线与景深，正文仍保持自己的页面底色。
                videoCardMotionEnabled = articleTarget,
                cardBackgroundEffectsEnabled = articleTarget || target is BiliPaiNavKey.Live || target is BiliPaiNavKey.LiveAreaDetail,
                isolateContentDrawing = target is BiliPaiNavKey.Live,
                useSharedSnapshotBlur = target is BiliPaiNavKey.Live,
                realtimeBlurEnabled = if (generalContainer) settingsContainerRealtimeBlurEnabled
                    else videoTransitionRealtimeBlurEnabled,
                isLightBackground = { liveContainerIsLightBackground.value },
                blurMotionTierProvider = navBlurMotionTierProvider,
            )
        }
    }
    val liveContainerTransitions = if (!liveContainerEnabled) emptyMap() else
        availableLiveContainerTransitions.filterKeys { target ->
            (target is BiliPaiNavKey.BangumiPlayer || target is BiliPaiNavKey.Live || target is BiliPaiNavKey.LiveAreaDetail || target is BiliPaiNavKey.ArticleDetail || target is BiliPaiNavKey.DynamicDetail || target is BiliPaiNavKey.Web || target is BiliPaiNavKey.Space || target is BiliPaiNavKey.BangumiDetail) && isSettingsContainerBoundary(
                enabled = true,
                backStack = stackSnapshot,
                target = target,
                owner = liveContainerSnapshot[target]?.owner,
            )
        }
    LaunchedEffect(stackSnapshot, liveContainerSnapshot.keys) {
        liveContainerSources.sources.keys.retainAll(stackSnapshot.toSet())
    }
    val currentKey = stackSnapshot.lastOrNull()
    val latestOnBack by rememberUpdatedState(onBack)
    val latestPrepareReturn by rememberUpdatedState(onPrepareVideoCardSharedReturn)
    val latestRelatedReturn by rememberUpdatedState(onRelatedVideoDetailReturned)
    val latestPreferWholeCardReturn by rememberUpdatedState(preferWholeCardReturn)
    val latestReturnContentFollowProgress by rememberUpdatedState(videoReturnContentFollowProgressEnabled)
    val cardMorphMode = resolveBiliPaiVideoCardMorphMode(
        cardTransitionEnabled = cardTransitionEnabled,
        reduceMotion = reduceMotion,
        sourceRoute = sourceMetadata.sourceRoute,
        hasUsableSourceBounds = sourceMetadata.sourceBounds
            ?.let { it.width > 1f && it.height > 1f } == true,
    )
    val cardMorphAvailable = cardMorphMode != BiliPaiVideoCardMorphMode.NONE
    var relatedReturnTransitionObserved by remember(videoSourceGeneration) { mutableStateOf(false) }
    val style = if (reduceMotion) {
        BiliPaiPredictiveBackAnimationStyle.NONE
    } else {
        predictiveBackAnimationStyle
    }
    val videoReturnAnimated = cardMorphAvailable || style != BiliPaiPredictiveBackAnimationStyle.NONE
    // 开合几何唯一的时钟。放在 performBack 之前：返回时要在**弹出同帧**起跑。
    val videoCardMorph = remember { VideoCardMorphDriver() }
    val hostCoroutineScope = rememberCoroutineScope()
    val performBack = remember(
        backStack,
        cardMorphAvailable,
        sourceMetadata.sourceRoute,
        sourceMetadata.sourceChromeSnapshot,
        videoCardMorph,
        hostCoroutineScope,
    ) {
        {
            val leavingKey = backStack.lastOrNull()
            if (leavingKey is BiliPaiNavKey.VideoDetail) {
                latestPrepareReturn()
                if (cardMorphAvailable) {
                    // 返回：播放页从整屏缩回那张卡，底下的列表卡原位透出来。
                    // 开与合是同一条曲线的两个方向，返回就是展开的倒放。
                    //
                    // 收缩必须在**弹出这一帧**起跑。放到弹出之后（等宿主的 LaunchedEffect）
                    // 会白丢一帧，而离场 entry 只活四百多毫秒。
                    //
                    // 只有在「返回到的是一张列表卡」时才由这一页自己收缩：如果底下是另一个
                    // 视频详情（相关推荐连点），那一页自己就是飞行页，几何归它管。
                    val returnsToCardSource = !isCardMorphDestinationNavKey(
                        backStack.getOrNull(backStack.lastIndex - 1),
                    )
                    if (returnsToCardSource) {
                        val epoch = videoCardMorph.beginReturn()
                        val startMorph = videoCardMorph.value().coerceIn(0f, 1f)
                        hostCoroutineScope.launch {
                            runCardMorphRamp(
                                durationMs = VIDEO_CARD_MORPH_OUT_MS,
                                from = startMorph,
                                to = 0f,
                            ) { value ->
                                videoCardMorph.writeReturn(epoch = epoch, value = value)
                            }
                            // 兜底：缩完了人还停在播放页，说明这次返回没被消费（被别处拦掉）。
                            // 不恢复的话页面会一直停在卡片大小上。
                            if (isCardMorphDestinationNavKey(backStack.lastOrNull())) {
                                videoCardMorph.settle()
                            }
                        }
                    }
                    videoCardClock.beginReturning(sourceMetadata.sourceRoute, videoCardClock.depthProgress())
                }
            }
            latestOnBack()
        }
    }

    DisposableEffect(programmaticBackDispatcher, performBack) {
        programmaticBackDispatcher.register(performBack)
        onDispose { programmaticBackDispatcher.unregister(performBack) }
    }

    val globalTransition = remember(
        style,
        predictiveBackExitDirection,
        isLightBackground,
        miuixTransitionBlurEnabled,
        miuixPredictiveBackMaxProgressPercent,
        navBlurMotionTierProvider,
    ) {
        biliPaiMiuixNavTransition(
            animation = style,
            exitDirection = predictiveBackExitDirection,
            isLightBackground = isLightBackground,
            miuixTransitionBlurEnabled = miuixTransitionBlurEnabled,
            miuixPredictiveBackMaxProgressPercent =
                miuixPredictiveBackMaxProgressPercent,
            blurMotionTierProvider = navBlurMotionTierProvider,
        )
    }
    val predictiveBackExcludedTransition = remember(
        globalTransition,
        style,
        predictiveBackExitDirection,
        isLightBackground,
        miuixTransitionBlurEnabled,
        navBlurMotionTierProvider,
    ) {
        if (shouldUseMiuixPredictiveBackProgress(style, enabled = true)) {
            biliPaiMiuixNavTransition(
                animation = BiliPaiPredictiveBackAnimationStyle.MIUIX,
                exitDirection = predictiveBackExitDirection,
                isLightBackground = isLightBackground,
                miuixTransitionBlurEnabled = miuixTransitionBlurEnabled,
                miuixPredictiveBackProgressEnabled = false,
                blurMotionTierProvider = navBlurMotionTierProvider,
            )
        } else {
            globalTransition
        }
    }
    // A restored parent session must not keep the departed child's scope at depth -1.
    val videoCardTransitionProgress = remember(
        sourceMetadata.sourceKey, videoSharedReturnGestureFollowEnabled, videoSharedReturnGestureTranslationEnabled,
    ) {
        MiuixVideoCardTransitionProgress(
            gestureTranslationEnabled = videoSharedReturnGestureTranslationEnabled,
            // 返回手势不再叠加 3D 抬升/倾斜姿态。那一刻整页已经按手势进度一路缩向来源卡，
            // 再叠一层绕手指支点的抬升，就会先向右上方飘一下再落回卡片，看起来是两段位移。
            gesturePoseEnabled = false,
        )
    }
    // 返回手势的振动交给系统（predictive back 自己会反馈），应用不再叠加一次。
    val navigationEventDispatcher = LocalNavigationEventDispatcherOwner.current?.navigationEventDispatcher
    LaunchedEffect(navigationEventDispatcher, videoCardTransitionProgress, cardMorphAvailable) {
        if (cardMorphAvailable &&
            (videoSharedReturnGestureFollowEnabled || videoSharedReturnGestureTranslationEnabled)
        ) {
            navigationEventDispatcher?.transitionState?.collect {
                videoCardTransitionProgress.gestureFollow.onTransitionState(it)
            }
        }
    }
    // 卡片形变开启时优先使用专属过渡，缺少来源时使用固定回退；关闭后使用当前页面过渡。
    val videoFallbackTransition = resolveVideoCardFallbackTransition(
        cardTransitionEnabled = cardTransitionEnabled,
        animationStyle = style,
        globalTransition = globalTransition,
        cardEnabledFallback = if (reduceMotion) globalTransition else
            top.yukonga.miuix.kmp.nav.transition.NavTransitions.MiuixDefault,
    )
    val observedVideoFallbackTransition = remember(
        videoFallbackTransition,
        videoCardTransitionProgress,
    ) {
        videoCardTransitionProgress.observe(videoFallbackTransition)
    }
    val returningProvider = remember(videoCardClock) {
        { videoCardClock.phase != VideoCardTransitionBackgroundPhase.OPENING }
    }
    val videoCardContentScale = resolveMiuixVideoCardContentScaleForSourceLayout(
        sourceLayout = sourceMetadata.sourceLayout,
        fullscreen = false,
    )
    val navCornerRadius = rememberDeviceCornerRadius(defaultRadius = 0.dp)
    val effectiveDeviceCornerDp = if (navCornerRadius > 0.dp) navCornerRadius else 32.dp
    val hostCoveredShapeCache = remember { MiuixVideoCardCoveredShapeCache() }
    // 预测返回松手但没退出（又弹回详情页）：整页立刻恢复满不透明。
    // 只在「刚从返回回来」时恢复，否则会把进场那段斜坡提前掐断。
    LaunchedEffect(cardMorphAvailable, videoCardClock, videoCardMorph) {
        if (!cardMorphAvailable) return@LaunchedEffect
        var previous: VideoCardTransitionBackgroundPhase? = null
        snapshotFlow { videoCardClock.phase }.collect { phase ->
            if (phase == VideoCardTransitionBackgroundPhase.HELD &&
                previous == VideoCardTransitionBackgroundPhase.RETURNING
            ) {
                videoCardMorph.settle()
            }
            previous = phase
        }
    }
    val videoCardTransition = remember(
        cardMorphAvailable,
        sourceMetadata.sourceBounds,
        sourceMetadata.sourceCornerDp,
        sourceMetadata.sourceLayout,
        sourceMetadata.sourceRoute,
        videoSharedTransitionDurationMillis,
        heroMotion,
        videoCardTransitionProgress,
        observedVideoFallbackTransition,
        videoCardContentScale,
        videoSharedReturnGestureFollowEnabled,
        videoSharedReturnGestureTranslationEnabled,
        effectiveDeviceCornerDp,
        sourceMetadata.sourceChromeSnapshot,
    ) {
        if (cardMorphAvailable) {
            miuixVideoCardNavTransition(
                sourceBounds = sourceMetadata.sourceBounds,
                sourceCornerDp = sourceMetadata.sourceCornerDp,
                durationMillis = videoSharedTransitionDurationMillis,
                fallback = observedVideoFallbackTransition,
                progress = videoCardTransitionProgress,
                contentScale = videoCardContentScale,
                gestureFollowEnabled = videoSharedReturnGestureFollowEnabled,
                // 返回路径固定：手指只驱动返回进度，不改变卡片轨迹。
                gestureTranslationEnabled = false,
                heroMotionSpec = heroMotion,
                returningProvider = returningProvider,
                deviceCornerDp = effectiveDeviceCornerDp,
                sourceLayout = sourceMetadata.sourceLayout,
                sourceRoute = sourceMetadata.sourceRoute,
                // 宿主自己的开合时钟：页面第一帧画出来之后从这里起跑。
                morphProvider = { videoCardMorph.value() },
            )
        } else {
            observedVideoFallbackTransition
        }
    }
    val fullscreenVideoCardTransition = remember(
        cardMorphAvailable,
        sourceMetadata.sourceBounds,
        sourceMetadata.sourceCornerDp,
        sourceMetadata.sourceLayout,
        sourceMetadata.sourceRoute,
        videoSharedTransitionDurationMillis,
        heroMotion,
        videoCardTransitionProgress,
        observedVideoFallbackTransition,
        videoSharedReturnGestureFollowEnabled,
        videoSharedReturnGestureTranslationEnabled,
        effectiveDeviceCornerDp,
        sourceMetadata.sourceChromeSnapshot,
    ) {
        if (cardMorphAvailable) {
            miuixVideoCardNavTransition(
                sourceBounds = sourceMetadata.sourceBounds,
                sourceCornerDp = sourceMetadata.sourceCornerDp,
                durationMillis = videoSharedTransitionDurationMillis,
                fallback = observedVideoFallbackTransition,
                progress = videoCardTransitionProgress,
                contentScale = MiuixVideoCardContentScale.CropCenter,
                gestureFollowEnabled = videoSharedReturnGestureFollowEnabled,
                // 返回路径固定：手指只驱动返回进度，不改变卡片轨迹。
                gestureTranslationEnabled = false,
                heroMotionSpec = heroMotion,
                returningProvider = returningProvider,
                deviceCornerDp = effectiveDeviceCornerDp,
                sourceLayout = sourceMetadata.sourceLayout,
                sourceRoute = sourceMetadata.sourceRoute,
                morphProvider = { videoCardMorph.value() },
            )
        } else {
            observedVideoFallbackTransition
        }
    }

    DisposableEffect(cardMorphAvailable, videoCardClock, videoCardTransitionProgress) {
        videoCardClock.bindNavigationDriver(
            if (cardMorphAvailable) ({ videoCardTransitionProgress.depthOrNull() }) else null,
        )
        onDispose { videoCardClock.bindNavigationDriver(null) }
    }
    // 卡片转场唯一逐帧驱动：详情 entry 的 transformEntry 会在同一帧挂多层 graphicsLayer，
    // 每层都读 visualDepth 且该函数会推进状态机。这里每帧只在这里打一次帧序号，
    // 让同帧内的重复读取走帧内记忆（见 MiuixVideoCardTransitionProgress.visualDepthMemoized），
    // 避免状态机同帧被推进多次。
    //
    // 只在**会动**的相位打拍：OPENING / RETURNING 才有逐帧插值。HELD 是稳态（用户正在看
    // 详情），此时不持有帧回调，避免长时间播放期间空转。帧序号跨帧单调递增，因此每次进入
    // 活动相位都从新序号开始，不会命中上一段的缓存。
    LaunchedEffect(cardMorphAvailable, videoCardTransitionProgress, videoCardClock) {
        if (!cardMorphAvailable) return@LaunchedEffect
        snapshotFlow {
            when (videoCardClock.phase) {
                VideoCardTransitionBackgroundPhase.OPENING,
                VideoCardTransitionBackgroundPhase.RETURNING,
                -> true
                // HELD 稳态一般不需要逐帧，但预测返回手势/取消恢复期间仍会逐帧变化，
                // 此时也要打拍，才能让同帧多层读走同一份采样。
                else -> videoCardTransitionProgress.isGestureInProgress() ||
                    videoCardClock.gestureRestoreInProgress
            }
        }.collectLatest { active ->
            if (!active) return@collectLatest
            while (true) {
                withFrameNanos { videoCardTransitionProgress.advanceTransitionFrame() }
            }
        }
    }
    var previousStack by remember { mutableStateOf(stackSnapshot) }
    LaunchedEffect(stackSnapshot, cardMorphAvailable, videoCardMorph) {
        val previous = previousStack
        previousStack = stackSnapshot
        if (!cardMorphAvailable) {
            videoCardClock.snapClearAndIdle()
            if (!stackSnapshot.any { it is BiliPaiNavKey.VideoDetail }) {
                CardPositionManager.clearNativeVideoCardLayers()
            }
            return@LaunchedEffect
        }
        val previousTop = previous.lastOrNull()
        val openedCardDestination = isCardMorphDestinationNavKey(currentKey) &&
            stackSnapshot.size > previous.size
        val returnedFromCardDestination = isCardMorphDestinationNavKey(previousTop) &&
            stackSnapshot.size < previous.size
        when {
            openedCardDestination -> {
                videoCardClock.beginOpeningIfNeeded(sourceMetadata.sourceRoute)
            }
            returnedFromCardDestination -> {
                videoCardClock.beginReturning(sourceMetadata.sourceRoute,
                    startDepth = videoCardClock.depthProgress())
                // 兜底。正常的按钮返回与手势提交已经在 performBack 里**弹出同帧**起跑了
                // 收缩，这里只补那些绕过 performBack 的弹出。绝不重复开一段：两段循环
                // 抢同一个 driver 会让进度被往回拉。
                if (!isCardMorphDestinationNavKey(stackSnapshot.lastOrNull()) &&
                    !videoCardMorph.isReturning()
                ) {
                    val epoch = videoCardMorph.beginReturn()
                    val startMorph = videoCardMorph.value().coerceIn(0f, 1f)
                    launch {
                        runCardMorphRamp(
                            durationMs = VIDEO_CARD_MORPH_OUT_MS,
                            from = startMorph,
                            to = 0f,
                        ) { value ->
                            videoCardMorph.writeReturn(epoch = epoch, value = value)
                        }
                    }
                }
            }
        }
    }
    LaunchedEffect(
        cardMorphAvailable,
        videoCardTransitionProgress,
        heroMotion,
        sourceMetadata.sourceKey,
        currentKey,
    ) {
        if (!cardMorphAvailable) return@LaunchedEffect
        fun finishCardReturn() {
            videoCardClock.landingFeedback.publish(
                sourceKey = sourceMetadata.sourceKey,
                sourceLayout = sourceMetadata.sourceLayout,
                committedReturn = videoCardClock.phase == VideoCardTransitionBackgroundPhase.RETURNING,
            )
            videoCardClock.followNavigationDriver(VideoCardTransitionSettleState.Idle)
            videoCardTransitionProgress.clear()
        }
        // Coarse states only: no frame-rate composition reads or competing fallback jobs.
        snapshotFlow { videoCardTransitionProgress.settleStateOrNull() }.collect { state ->
            if (state == null) {
                if (isCardMorphDestinationNavKey(currentKey)) return@collect
                if (videoCardClock.phase == VideoCardTransitionBackgroundPhase.IDLE) return@collect
                // A pop can dispose its final transition scope without a last Idle transform.
                // Give NavDisplay one frame to bind any outgoing scope, then release a retained
                // HELD/RETURNING phase once the card destination is no longer on top.
                withFrameNanos { }
                if (
                    videoCardTransitionProgress.settleStateOrNull() == null &&
                    !isCardMorphDestinationNavKey(currentKey)
                ) {
                    finishCardReturn()
                }
                return@collect
            }
            if (state == VideoCardTransitionSettleState.Idle) {
                // LiveNavTransitionScope reads the shared navigation presentation even after
                // its video entry leaves. Release it before another route reuses that driver.
                finishCardReturn()
            } else {
                videoCardClock.followNavigationDriver(state, videoCardTransitionProgress.releaseVelocity())
            }
            VideoCardTransitionDiagnostics.onMotionPhase(
                state, heroMotion, sourceMetadata.sourceLayout, diagnosticConfiguration,
            )
        }
    }

    // Retain click-time pixels for the entire detail round trip, including child pages and
    // cancelled back gestures. An Idle driver alone does not prove the owning entry has left.
    val sourceCardOwnerRetained = stackSnapshot.any { key ->
        key is BiliPaiNavKey.VideoDetail &&
            key.sourceRoute == sourceMetadata.sourceRoute &&
            sourceMetadata.sourceKey == "${key.sourceRoute}:${key.bvid}"
    }
    LaunchedEffect(videoCardClock, cardMorphAvailable, videoCardClock.phase, sourceCardOwnerRetained) {
        if (!cardMorphAvailable || sourceCardOwnerRetained ||
            videoCardClock.phase != VideoCardTransitionBackgroundPhase.IDLE
        ) {
            return@LaunchedEffect
        }
        val retiringLayer = CardPositionManager.lastClickedNativeCardLayer
        val retiringSourceKey = CardPositionManager.lastClickedVideoSourceKey
        withFrameNanos { }
        withFrameNanos { }
        if (videoCardClock.phase == VideoCardTransitionBackgroundPhase.IDLE &&
            CardPositionManager.lastClickedNativeCardLayer === retiringLayer &&
            CardPositionManager.lastClickedVideoSourceKey == retiringSourceKey
        ) {
            CardPositionManager.clearNativeVideoCardLayers()
        }
    }

    val videoCardSnapshotHandle = rememberVideoCardTransitionSnapshotHandle()
    val transitionMotionTier = if (reduceMotion) MotionTier.Reduced else MotionTier.Normal
    // Card depth blur and generic Miuix return blur have separate user controls.
    val effectiveVideoCardBlurEnabled = videoTransitionRealtimeBlurEnabled
    val videoCardProgressProvider = remember(
        cardMorphAvailable,
        videoCardClock,
        videoCardTransitionProgress,
    ) {
        {
            if (cardMorphAvailable) {
                videoCardTransitionProgress.depthOrNull() ?: videoCardClock.depthProgress()
            } else {
                videoCardClock.depthProgress()
            }
        }
    }
    val videoCardGestureProvider = remember(cardMorphAvailable, videoCardTransitionProgress) {
        { cardMorphAvailable && videoCardTransitionProgress.isGestureInProgress() }
    }
    val videoCardExposure = remember(
        videoCardClock,
        videoCardGestureProvider,
        videoCardTransitionProgress,
    ) {
        // Readable from composition or draw: only exposure boundaries propagate, never frames.
        derivedStateOf(structuralEqualityPolicy()) {
            val settleState = videoCardTransitionProgress.settleStateOrNull()
            val effectivePhase = when (settleState) {
                // Reveal the list slot on the same endpoint frame that removes the flying
                // entry; waiting for the clock collector can expose an empty slot for a frame.
                VideoCardTransitionSettleState.Idle -> VideoCardTransitionBackgroundPhase.IDLE
                VideoCardTransitionSettleState.AutoReturn -> VideoCardTransitionBackgroundPhase.RETURNING
                else -> videoCardClock.phase
            }
            val effectiveRestore = videoCardClock.gestureRestoreInProgress ||
                settleState == VideoCardTransitionSettleState.CancelRestore
            resolveVideoCardTransitionExposure(
                phase = effectivePhase,
                predictiveBackInProgress = videoCardGestureProvider(),
                gestureRestoreInProgress = effectiveRestore,
            )
        }
    }
    val videoCardExposureProvider = remember(videoCardExposure) { { videoCardExposure.value } }
    val effectiveVideoCardExposure = videoCardExposureProvider()
    // Ordinary related-video slides have no card clock. Read their actual navigation settle
    // instead of restoring the parent's morph while the child is still moving off screen.
    val fallbackReturnExposure by remember(videoCardTransitionProgress) {
        derivedStateOf {
            when (videoCardTransitionProgress.settleStateOrNull()) {
                VideoCardTransitionSettleState.AutoReturn -> VideoCardTransitionExposure.Returning
                VideoCardTransitionSettleState.CancelRestore -> VideoCardTransitionExposure.Restoring
                VideoCardTransitionSettleState.InteractiveSeek -> VideoCardTransitionExposure.BackPreview
                VideoCardTransitionSettleState.Idle,
                VideoCardTransitionSettleState.Held,
                null -> VideoCardTransitionExposure.Idle
                else -> VideoCardTransitionExposure.Opening
            }
        }
    }
    val relatedReturnExposure = if (cardMorphAvailable) effectiveVideoCardExposure else fallbackReturnExposure
    LaunchedEffect(
        pendingRelatedReturnCount,
        relatedReturnRequestId,
        videoSourceGeneration,
        relatedReturnExposure,
        videoReturnAnimated,
    ) {
        val restoreDecision = resolveRelatedReturnSourceRestoreDecision(
            restorePending = pendingRelatedReturnCount > 0,
            transitionObserved = relatedReturnTransitionObserved,
            transitionAnimated = videoReturnAnimated,
            exposure = relatedReturnExposure,
        )
        relatedReturnTransitionObserved = restoreDecision.transitionObserved
        if (restoreDecision.shouldRestore) {
            // The nested source geometry remains immutable through the complete predictive
            // settle. Only arm the parent's older session after the navigation driver is idle.
            if (latestRelatedReturn(videoSourceGeneration, relatedReturnRequestId)) {
                relatedReturnTransitionObserved = false
            }
        }
    }
    LaunchedEffect(effectiveVideoCardExposure) {
        // 新点击可在旧 Idle effect 执行前同步开启会话，不能清空刚接手的背景层。
        if (shouldReleaseHostOwnedDepthLayer(effectiveVideoCardExposure) &&
            shouldReleaseHostOwnedDepthLayer(videoCardExposureProvider()) &&
            videoCardClock.phase == VideoCardTransitionBackgroundPhase.IDLE
        ) {
            videoCardSnapshotHandle.releaseSession()
        }
    }
    val currentBackTarget = stackSnapshot.getOrNull(stackSnapshot.lastIndex - 1)
    // 只以真正会改变「被捕获数据」的值作为键。provider 一律是上面已 remember 的稳定引用，
    // 把它们放进键里会让任意一次重组都重建整个 state —— 下游模糊/缩放/圆角/scrim effect
    // 全部换新实例，本帧参数不连续，视觉上就是「闪一下」。state 只捕获引用，读值发生在
    // 下游 draw/graphicsLayer，因此不需要随 provider 重建。
    val transitionBackgroundState = remember(
        sourceMetadata.sourceKey,
        sourceMetadata.sourceRoute,
        sourceMetadata.sourceCornerDp,
        sourceMetadata.sourceBounds,
        videoCardSnapshotHandle,
        transitionMotionTier,
        isLightBackground,
        effectiveVideoCardBlurEnabled,
    ) {
        VideoCardTransitionBackgroundState(
            progressProvider = videoCardProgressProvider,
            sourceRouteProvider = { sourceMetadata.sourceRoute },
            sourceKeyProvider = { sourceMetadata.sourceKey },
            phaseProvider = { videoCardClock.phase },
            exposureProvider = videoCardExposureProvider,
            sourceCornerDpProvider = { sourceMetadata.sourceCornerDp },
            sourceBoundsProvider = { sourceMetadata.sourceBounds },
            snapshotHandle = videoCardSnapshotHandle,
            isReturnGestureInProgressProvider = videoCardGestureProvider,
            isGestureRestoreInProgressProvider = { videoCardClock.gestureRestoreInProgress },
            preferWholeCardReturnProvider = { latestPreferWholeCardReturn },
            returnContentFollowProgressEnabledProvider = { latestReturnContentFollowProgress },
            motionTierProvider = { transitionMotionTier },
            isLightBackgroundProvider = { isLightBackground },
            realtimeBlurEnabledProvider = { effectiveVideoCardBlurEnabled },
        )
    }
    val videoCardLayoutWidthProvider = remember(videoCardTransitionProgress) {
        {
            videoCardTransitionProgress.layoutWidthOr(
                fallback = 1f, // overwritten after first transformEntry bind
            )
        }
    }
    val videoCardLayoutHeightProvider = remember(videoCardTransitionProgress) {
        {
            videoCardTransitionProgress.layoutHeightOr(
                fallback = 1f,
            )
        }
    }
    val reportContentTopInsetPx = remember(videoCardTransitionProgress) {
        { insetPx: Float ->
            videoCardTransitionProgress.contentTopInsetPx = insetPx
        }
    }
    val miuixCardTransitionState = remember(
        cardMorphAvailable,
        heroMotion,
        videoCardProgressProvider,
        videoCardGestureProvider,
        videoCardLayoutWidthProvider,
        videoCardLayoutHeightProvider,
        reportContentTopInsetPx,
        sourceMetadata.sourceBounds,
        sourceMetadata.sourceCoverBounds,
        sourceMetadata.sourceInfoBounds,
        sourceMetadata.sourceLayout,
        sourceMetadata.sourceChromeSnapshot,
    ) {
        MiuixVideoCardTransitionState(
            enabled = cardMorphAvailable,
            motionSpec = heroMotion,
            progressProvider = videoCardProgressProvider,
            isGestureInProgressProvider = videoCardGestureProvider,
            layoutWidthProvider = videoCardLayoutWidthProvider,
            layoutHeightProvider = videoCardLayoutHeightProvider,
            sourceBoundsProvider = { sourceMetadata.sourceBounds },
            sourceCoverBoundsProvider = { sourceMetadata.sourceCoverBounds },
            sourceInfoBoundsProvider = { sourceMetadata.sourceInfoBounds },
            sourceLayout = sourceMetadata.sourceLayout,
            sourceChromeSnapshot = sourceMetadata.sourceChromeSnapshot,
            onReportContentTopInsetPx = reportContentTopInsetPx,
        )
    }
    // 恢复 0.2.2 的预测返回背景链路：目标返回页（栈前一 key）在预测返回手势中
    // 随手势进度模糊/消退，迁移到 Miuix 导航时该 provide 曾丢失。
    val predictiveBackBackgroundState = remember(
        currentKey,
        cardMorphAvailable,
        videoCardTransitionProgress,
        currentBackTarget,
        transitionMotionTier,
        isLightBackground,
        miuixTransitionBlurEnabled,
    ) {
        PredictiveBackBackgroundState(
            progressProvider = {
                val blurEnabled = miuixTransitionBlurEnabled
                if (!blurEnabled || !isCardMorphDestinationNavKey(currentKey)) {
                    0f
                } else {
                    videoCardTransitionProgress.gestureBackProgress()
                        ?.takeIf { cardMorphAvailable }
                        ?.let { resolvePredictiveBackGestureBlurProgress(it) }
                        ?: 0f
                }
            },
            targetKeyProvider = { currentBackTarget },
            motionTierProvider = { transitionMotionTier },
            isLightBackgroundProvider = { isLightBackground },
        )
    }

    val roundAllCorners = style == BiliPaiPredictiveBackAnimationStyle.AOSP ||
        style == BiliPaiPredictiveBackAnimationStyle.SCALE ||
        style == BiliPaiPredictiveBackAnimationStyle.CLASSIC
    // Video-card morph owns all four corners. Keeping NavDisplay's Leading clip enabled here
    // applies a second, device-radius clip only to the left edge and makes it visibly rounder
    // than the right edge during return.
    val videoCardMorphOwnsCorners = cardMorphAvailable && (
        isCardMorphDestinationNavKey(currentKey) ||
            effectiveVideoCardExposure == VideoCardTransitionExposure.Opening ||
            effectiveVideoCardExposure == VideoCardTransitionExposure.BackPreview ||
            effectiveVideoCardExposure == VideoCardTransitionExposure.Returning ||
            effectiveVideoCardExposure == VideoCardTransitionExposure.Restoring
    )
    val enableHostCornerClip = !videoCardMorphOwnsCorners
    // The retained source page already owns blur/scrim through the video-card depth layer.
    // Miuix's generic covered-entry dim can be resolved from the lower VideoDetail transition
    // during nested related-video navigation, which darkens that page a second time.
    val hostDimAmount = if (videoCardMorphOwnsCorners) 0f else 0.5f
    val backdropColor = AppSurfaceTokens.surface()
    val videoCardBackingColor = resolveMiuixVideoCardBackingColor(sourceMetadata.sourceRoute, backdropColor)
    val effects = remember(
        navCornerRadius,
        roundAllCorners,
        enableHostCornerClip,
        hostDimAmount,
        backdropColor,
        videoCardMorphOwnsCorners,
    ) {
        NavDisplayEffects(
            enableCornerClip = enableHostCornerClip,
            cornerClipRadius = if (roundAllCorners && navCornerRadius <= 0.dp) 32.dp else navCornerRadius,
            cornerClipMode = if (roundAllCorners) {
                NavCornerClipMode.All
            } else {
                NavCornerClipMode.Leading
            },
            dimAmount = hostDimAmount,
            // The fixed host backing must sit BELOW the retained source snapshot. A second
            // opaque backing inside NavDisplay would cover that snapshot on its preparation frame.
            backdropColor = if (videoCardMorphOwnsCorners) Color.Unspecified else backdropColor,
        )
    }
    // 全屏滑动返回默认关闭（仅系统边缘预测返回），可在设置中开启。
    // 开启后仅对列表/设置等纵向页面生效，播放器、详情、WebView 等
    // 横滑冲突页面始终禁用（见 BiliPaiNavEntryProvider）。
    val fullScreenSwipeBackEnabled by remember(context) { com.android.purebilibili.core.store.SettingsManager.getFullScreenSwipeBackEnabled(context) }
            .collectAsStateWithLifecycle(initialValue = false)
    val swipeBackDirection = if (fullScreenSwipeBackEnabled) {
        when (LocalLayoutDirection.current) {
            LayoutDirection.Rtl -> NavSwipeDirection.RightToLeft
            LayoutDirection.Ltr -> NavSwipeDirection.LeftToRight
        }
    } else {
        NavSwipeDirection.None
    }
    val interceptPredictiveBack =
        style == BiliPaiPredictiveBackAnimationStyle.NONE && backStack.size > 1
    val globalWallpaperVisible = LocalGlobalWallpaperBackdropVisible.current
    val clickToPlayEnabled by remember(context) { com.android.purebilibili.core.store.SettingsManager.getClickToPlay(context) }
        .collectAsStateWithLifecycle(
            initialValue = com.android.purebilibili.core.store.SettingsManager.getClickToPlaySync(LocalContext.current)
        )
    val dynamicImagePreviewTextVisible by remember(context) { com.android.purebilibili.core.store.SettingsManager.getDynamicImagePreviewTextVisible(context) }
        .collectAsStateWithLifecycle(initialValue = true)
    val dynamicCommentPreviewVisible by remember(context) { com.android.purebilibili.core.store.home.DynamicSettingsStore.observeCommentPreviewVisible(context) }
        .collectAsStateWithLifecycle(initialValue = false)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                settingsContainerSources.hostOrigin = coordinates.positionInRoot()
                settingsContainerSources.hostSize = coordinates.size
                liveContainerSources.hostOrigin = coordinates.positionInRoot()
                liveContainerSources.hostSize = coordinates.size
                videoCardTransitionProgress.gestureFollow.hostOriginInRoot = coordinates.positionInRoot()
                // System events are in screen pixels; card bounds and layers are host-local.
                val origin = coordinates.localToScreen(Offset.Zero)
                if (origin.isSpecified) {
                    videoCardTransitionProgress.gestureFollow.hostOriginOnScreen = origin
                }
            }
            .background(
                if (videoCardMorphOwnsCorners) {
                    videoCardBackingColor
                } else if (globalWallpaperVisible) {
                    Color.Transparent
                } else {
                    AppSurfaceTokens.groupedListContainer()
                }
            ),
    ) {
        VideoCardTransitionHostDepthLayer(
            enabled = cardMorphAvailable &&
                shouldUseHostOwnedVideoCardTransitionSnapshot(sourceMetadata.sourceRoute),
            snapshotHandle = videoCardSnapshotHandle,
            progressProvider = videoCardProgressProvider,
            phaseProvider = { videoCardClock.phase },
            exposureProvider = videoCardExposureProvider,
            isGestureRestoreInProgressProvider = { videoCardClock.gestureRestoreInProgress },
            motionTierProvider = { transitionMotionTier },
            isLightBackgroundProvider = { isLightBackground },
            realtimeBlurEnabledProvider = { effectiveVideoCardBlurEnabled },
            sourceBoundsProvider = { sourceMetadata.sourceBounds },
            // A detached source has no navigation entry to transform. Apply exactly the same
            // covered-page geometry to this fallback; inner snapshot effects remain unscaled.
            modifier = Modifier.graphicsLayer {
                val depth = videoCardProgressProvider()
                val reducedMotion = transitionMotionTier == MotionTier.Reduced
                val scale = resolveMiuixVideoCardCoveredScale(
                    depth = depth,
                    reducedMotion = reducedMotion,
                )
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin.Center
                clip = scale < 1f
                shape = hostCoveredShapeCache.resolve(resolveMiuixVideoCardCoveredCornerPx(
                    depth = depth,
                    reducedMotion = reducedMotion,
                    density = density,
                ))
            },
        )
        @Suppress("UNCHECKED_CAST")
        NavDisplay(
            backStack = backStack as NavBackStack,
            onBack = performBack,
            transition = globalTransition,
            effects = effects,
            backCompletionPolicy = if (cardMorphAvailable) VideoCardBackCompletionPolicy else null,
        ) {
            biliPaiNavEntries(
                swipeBackDirection = swipeBackDirection,
                settingsBackStack = stackSnapshot,
                settingsPersistentPanes = settingsPersistentPanes,
                settingsContainerTransitions = settingsContainerTransitions,
                settingsMorphFallback = if (settingsContainerEnabled) {
                    top.yukonga.miuix.kmp.nav.transition.NavTransitions.MiuixDefault
                } else null,
                liveContainerTransitions = liveContainerTransitions,
                activeMainHostRoute = activeMainHostRoute,
                pageTransition = globalTransition,
                predictiveBackExcludedTransition = predictiveBackExcludedTransition,
                videoCardTransition = videoCardTransition,
                fullscreenVideoCardTransition = fullscreenVideoCardTransition,
            ) { key ->
                // 返回尚未落位时可开始另一张卡片的转场。旧 entry 保留自身的文字、
                // 几何和进度驱动，不能因同样来自首页而接收新卡片的快照。
                var retainedCardTransition by remember(key) {
                    mutableStateOf(MiuixVideoCardTransitionState())
                }
                // 开合几何由这一层统一给出，第一次参与合成时定性、之后冻结：
                // 分支随重组来回切换会把 content 挪到另一个槽位，整页状态会被重建。
                // 听视频横条同样走这条几何——它没有自己的第二套曲线。
                val entryMorphEnabled = remember(key, cardMorphAvailable) {
                    cardMorphAvailable &&
                        cardTransitionEnabled &&
                        isCardMorphDestinationNavKey(key)
                }
                // 这一层第一帧真正绘制出来的时刻。展开从「看见」开始计时，不从「开始组合」
                // 开始——详情页首帧组合要一两百毫秒，那段时间按帧计时会被整段吞掉。
                val entryFirstDrawAt = remember(key) { longArrayOf(0L) }
                val entryCardTransitionState = resolveVideoCardEntryTransitionState(
                    key = key,
                    source = sourceMetadata,
                    current = miuixCardTransitionState,
                    retained = retainedCardTransition,
                )
                SideEffect { retainedCardTransition = entryCardTransitionState }
                // 开合期间的 entry 里，页面内部一律当作「已经全屏」：落地媒体、反算缩放、
                // 来源卡 chrome 这些补偿全部拿到恒等输入。整页几何（缩放 / 位移 / 窗口）
                // 全部由 entry 那一层 `graphicsLayer` 给出，页面自己只管画整屏。两套几何
                // 同时改同一个封面，画面才会被压扁。
                val providedCardTransitionState = if (entryMorphEnabled) {
                    remember(entryCardTransitionState) {
                        entryCardTransitionState.copy(progressProvider = { 1f })
                    }
                } else {
                    entryCardTransitionState
                }
                // Freeze this per entry so popping the key cannot remove its backing during exit.
                val opaqueVideoChild = remember(key) {
                    shouldUseOpaqueVideoChildBackground(key, stackSnapshot)
                }
                // 每个直播 entry 固定表面类型，返回动画或目录清理时不重新绑定播放器。
                val liveContainerEntry = remember(key) { liveContainerTransitions.containsKey(key) }
                BiliPaiMiuixNavEntry(
                    interceptPredictiveBack = interceptPredictiveBack,
                    onBack = performBack,
                ) {
                    CompositionLocalProvider(
                        com.android.purebilibili.core.ui.LocalNativeCardNavigate provides onOpenCardContainer,
                        com.android.purebilibili.core.ui.LocalNativeArticleCardMotionEnabled provides liveContainerEnabled,
                        com.android.purebilibili.feature.space.LocalShopContainerMotionEnabled provides
                            (liveContainerEnabled && settingsContainerMorphEnabled),
                        com.android.purebilibili.feature.live.LocalLiveCardContainerSources provides
                            (if (liveContainerEnabled) liveContainerSources else null),
                        com.android.purebilibili.feature.live.LocalLiveCardContainerOwner provides key,
                        com.android.purebilibili.feature.live.LocalLiveCardContainerActive provides liveContainerEntry,
                        com.android.purebilibili.feature.settings.LocalSettingsContainerSources provides
                            (if (settingsContainerEnabled) settingsContainerSources else null),
                        com.android.purebilibili.feature.settings.LocalSettingsContainerOwner provides key,
                        LocalVideoDetailNavigationTransitionScope provides LocalNavTransitionScope.current,
                        LocalGlobalWallpaperBackdropVisible provides
                            (globalWallpaperVisible && !opaqueVideoChild &&
                                shouldRenderGlobalHomeWallpaperBackdrop(
                                    effectScope = HomeWallpaperEffectScope.GLOBAL,
                                    currentRoute = key.toLegacyRoute(),
                                    mainHostTabRoute = activeMainHostRoute,
                                )),
                        LocalVideoCardSharedElementSourceRoute provides key.toLegacyRoute(),
                        LocalVideoCardTransitionClock provides videoCardClock,
                        LocalVideoCardTransitionBackgroundState provides transitionBackgroundState,
                        LocalMiuixVideoCardTransitionState provides providedCardTransitionState,
                        LocalPredictiveBackBackgroundState provides predictiveBackBackgroundState,
                        LocalClickToPlayEnabled provides clickToPlayEnabled,
                        LocalDynamicImagePreviewTextVisible provides dynamicImagePreviewTextVisible,
                        LocalDynamicCommentPreviewVisible provides dynamicCommentPreviewVisible,
                        LocalSharedTransitionEnabled provides cardTransitionEnabled,
                    ) {
                        ProvideMiuixNavViewModelApplicationExtras(application) {
                            if (entryMorphEnabled) {
                                VideoCardMorphClock(videoCardMorph, entryFirstDrawAt)
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    // 只做一件事：给「第一帧真正画出来」打时间戳。
                                    //
                                    // 展开的计时起点是「看见」，不是「开始组合」——详情页
                                    // 首帧组合要一两百毫秒，从组合开始计时的话，那段行程
                                    // 会被整段吞掉，看起来就是硬切。
                                    //
                                    // 几何与不透明度都不在这里：它们由导航层的
                                    // `transformEntry` 统一给出，页面从卡片矩形长到整屏。
                                    // 只写普通数组，不订阅任何状态。
                                    .then(
                                        if (entryMorphEnabled) {
                                            Modifier.graphicsLayer {
                                                if (entryFirstDrawAt[0] == 0L) {
                                                    entryFirstDrawAt[0] = System.nanoTime()
                                                }
                                            }
                                        } else {
                                            Modifier
                                        }
                                    )
                                    .then(
                                        if (opaqueVideoChild) {
                                            Modifier.background(
                                                AppSurfaceTokens.groupedListContainer().copy(alpha = 1f)
                                            )
                                        } else if (LocalGlobalWallpaperBackdropVisible.current) {
                                            Modifier.globalWallpaperAwareBackground()
                                        } else Modifier
                                    ),
                            ) {
                                content(key)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 播放页展开的时钟。
 *
 * 起点是这一层**第一帧真正绘制出来**的时刻（由 entry 的 `graphicsLayer` 打时间戳），
 * 不是开始组合的时刻，也不是导航驱动开始走的时刻：
 *
 * 1. 不能只挂在导航驱动上——驱动的行程从压栈那一帧开始算，而详情页首帧要一两百毫秒才
 *    画得出来。挂上去的话，页面第一次出现时行程已经走完，看到的就是硬切。
 * 2. 也不能从组合开始按帧数——实测那第一帧组合占掉主线程一百多毫秒，期间帧回调照发
 *    不误，整段行程被吞成一帧。录屏里看到的就是「一帧跳满」。
 * 3. 所以等第一帧画出来才开始计墙钟，按 [VIDEO_CARD_MORPH_IN_MS] 从 0 推到 1。
 *    第一帧进度已经是 0，所以展开一定从卡片大小开始。
 *
 * 这里只写 [VideoCardMorphDriver]，不自己画：几何与不透明度由导航层的 `transformEntry`
 * 在绘制阶段读取（写状态只让那层重绘，不重组）。
 */
@Composable
private fun VideoCardMorphClock(
    driver: VideoCardMorphDriver,
    firstDrawAt: LongArray,
) {
    // 每进一次导航栈换一个 token：同一张卡的重复重组不会把进度重新压回 0。
    val armToken = remember { Any() }
    // 只在这一层**第一次**组合时把进度压回 0。
    //
    // 不能每次重组都压：详情页可以从详情页里再点开（相关推荐），两个 entry 都挂着
    // 同一块时钟，被压在下面那个每次重组都会抢着 arm，把刚点开的那页按回卡片大小。
    // 新 entry 永远是压栈进来的，所以「第一次组合」等价于「它就是这次进场的那一页」。
    val armed = remember { booleanArrayOf(false) }
    // 组合落地即压回 0——必须早于本帧绘制，否则第一帧会整页闪一下。
    SideEffect {
        if (!armed[0]) {
            armed[0] = true
            driver.arm(armToken)
        }
    }
    LaunchedEffect(driver, armToken) {
        // 等这一层真的画出来。组合/测量/首次布局都不算。
        while (firstDrawAt[0] == 0L) withFrameNanos { }
        runCardMorphRamp(
            durationMs = VIDEO_CARD_MORPH_IN_MS,
            from = 0f,
            to = 1f,
        ) { value ->
            driver.writeEntry(armToken, value)
        }
    }
}

/**
 * 按帧推进一段开合，[write] 每帧收到一个 0..1 之间的进度值。
 *
 * 用**帧间隔累加**而不是「现在 − 起点」，并且给单帧间隔设上限
 * （[VIDEO_CARD_MORPH_MAX_STEP_MS]）。原因只有一个：播放页弹出/落位的那一下，
 * 主线程要释放播放器、重建被覆盖的页面，实测会停两百多毫秒没有任何一帧上屏。
 * 按墙钟算的话，那一停就把整段行程一次吃掉——录屏里看到的正是「停一下，然后直接
 * 换脸」。按帧累加 + 限幅之后，那段停顿只算一帧，展开仍然老老实实走满。
 */
private suspend fun runCardMorphRamp(
    durationMs: Float,
    from: Float,
    to: Float,
    write: (Float) -> Unit,
) {
    val duration = durationMs.coerceAtLeast(1f)
    var elapsedMs = 0f
    var previousFrameNanos = 0L
    while (true) {
        val frameNanos = withFrameNanos { it }
        if (previousFrameNanos != 0L) {
            val deltaMs = (frameNanos - previousFrameNanos) / 1_000_000f
            elapsedMs += deltaMs.coerceIn(0f, VIDEO_CARD_MORPH_MAX_STEP_MS)
        }
        previousFrameNanos = frameNanos
        val progress = (elapsedMs / duration).coerceIn(0f, 1f)
        write(from + (to - from) * progress)
        if (progress >= 1f) return
    }
}

/**
 * 展开时长。与 Cobo / c001apk 的卡片展开同一个量级（420ms）。
 *
 * 再短一点（240ms）在 120Hz 上只有十几个可见帧，配合「首帧组合吃掉一百多毫秒」的
 * 现实，看起来仍然像一次跳变；420ms 才够把「从卡片长到整屏」这件事读出来。
 */
internal const val VIDEO_CARD_MORPH_IN_MS = 420f

/** 返回收缩时长。比展开短：返回是回到用户已经熟悉的位置，快一点更跟手。 */
internal const val VIDEO_CARD_MORPH_OUT_MS = 240f

/**
 * 单帧最多推进多少毫秒。
 *
 * 一帧 16.7ms（60Hz）/ 8.3ms（120Hz）正常走完；超过这个量的一帧只按一帧算，
 * 用来吸收「主线程被卡住几百毫秒、下一帧时间戳一下子跳很远」的那种停顿。
 *
 * 返回方向尤其需要它：弹出那一帧实测要 40–90ms，按墙钟推的话两三帧就把 240ms 的
 * 收缩走完，观感还是硬切；按帧封顶之后，弹出瞬间那几帧慢，收缩就老老实实多走几帧，
 * 但绝不超时——离场 entry 只活四百多毫秒，超了就看不见缩回去了。
 */
internal const val VIDEO_CARD_MORPH_MAX_STEP_MS = 26f

@Composable
private fun BiliPaiMiuixNavEntry(
    interceptPredictiveBack: Boolean,
    onBack: () -> Unit,
    content: @Composable () -> Unit,
) {
    val navigationEventState = rememberNavigationEventState(NavigationEventInfo.None)
    NavigationBackHandler(
        state = navigationEventState,
        isBackEnabled = interceptPredictiveBack,
        onBackCompleted = onBack,
    )
    com.android.purebilibili.core.ui.animation.ProvideNavigationEntranceState(content)
}

@Composable
private fun ProvideMiuixNavViewModelApplicationExtras(
    application: Application,
    content: @Composable () -> Unit,
) {
    val navEntryOwner = LocalViewModelStoreOwner.current
    if (navEntryOwner == null) {
        content()
        return
    }
    val patchedOwner = remember(navEntryOwner, application) {
        buildMiuixNavViewModelStoreOwner(navEntryOwner, application)
    }
    CompositionLocalProvider(LocalViewModelStoreOwner provides patchedOwner) {
        content()
    }
}

private fun buildMiuixNavViewModelStoreOwner(
    navEntryOwner: ViewModelStoreOwner,
    application: Application,
): ViewModelStoreOwner {
    val defaultFactoryOwner = navEntryOwner as? HasDefaultViewModelProviderFactory
    val defaultCreationExtras = defaultFactoryOwner?.defaultViewModelCreationExtras
        ?: CreationExtras.Empty
    val patchedCreationExtras = MutableCreationExtras(defaultCreationExtras).apply {
        set(ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY, application)
    }
    return object : ViewModelStoreOwner, HasDefaultViewModelProviderFactory {
        override val viewModelStore = navEntryOwner.viewModelStore
        override val defaultViewModelProviderFactory =
            defaultFactoryOwner?.defaultViewModelProviderFactory
                ?: ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        override val defaultViewModelCreationExtras: CreationExtras = patchedCreationExtras
    }
}
