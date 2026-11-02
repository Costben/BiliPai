package com.android.purebilibili.feature.home.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.lerp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.purebilibili.core.ui.AppContentColors
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.AppSpacingTokens
import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.core.ui.ContainerLevel
import com.android.purebilibili.core.ui.components.AppIcon
import com.android.purebilibili.core.ui.components.AppText
import com.android.purebilibili.feature.search.SearchViewModel
import io.github.suqi8.coui.kmp.basic.InputField
import io.github.suqi8.coui.kmp.basic.TopAppBarDefaults
import io.github.suqi8.coui.kmp.icon.COUIIcons
import io.github.suqi8.coui.kmp.icon.basic.Search
import io.github.suqi8.coui.kmp.icon.extended.Back

/**
 * 首页顶栏内联搜索：点搜索键后从搜索键原位向右展开成一条透明搜索框，
 * 右缘停在设置按钮左侧，设置按钮原地保留；回车或点联想词才带着关键词进入搜索页。
 */
@Composable
internal fun CouiHomeInlineSearchRow(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSubmit: (String) -> Unit,
    rowHeight: Dp,
    horizontalPadding: Dp,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val interaction = remember { MutableInteractionSource() }

    fun submit(keyword: String) {
        val trimmed = keyword.trim()
        if (trimmed.isEmpty()) return
        keyboard?.hide()
        focusManager.clearFocus()
        onSubmit(trimmed)
        onExpandedChange(false)
    }

    val progress by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = if (expanded) {
            tween(durationMillis = 280, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f))
        } else {
            // 收起比展开快一点、起始就动，缩回去的过程才看得见。
            tween(durationMillis = 220, easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f))
        },
        label = "CouiHomeInlineSearchProgress",
    )

    LaunchedEffect(expanded) {
        if (expanded) {
            // 焦点由库内部的 InputField 负责；这里只兜底把键盘弹出来，
            // 避免展开到一半输入法才出现。
            kotlinx.coroutines.delay(150)
            keyboard?.show()
        } else {
            // 清空与文字淡出交给 InputField 自己做，收起时不会突然跳成提示语。
            focusManager.clearFocus()
            keyboard?.hide()
            viewModel.dismissSuggestions()
            kotlinx.coroutines.delay(320)
            query = ""
        }
    }
    LaunchedEffect(query, expanded) {
        if (expanded) viewModel.onQueryChange(query)
    }
    DisposableEffect(viewModel) {
        onDispose { viewModel.dismissSuggestions() }
    }
    BackHandler(enabled = expanded) { onExpandedChange(false) }

    if (progress > 0.001f) {
        BoxWithConstraints(
            modifier = modifier
                .fillMaxWidth()
                .height(rowHeight),
        ) {
            val contentWidth = maxWidth - horizontalPadding * 2
            val actionWidth = TopAppBarDefaults.ActionItemMinWidth // 40.dp
            val spacing = TopAppBarDefaults.ActionMenuItemSpacing // 10.dp
            // 搜索条占「搜索按钮」那一格：右侧让出设置按钮的宽度和间距，
            // 它就从搜索键的位置向右长成一条，设置按钮原地不动。
            val trailingReserve = actionWidth + spacing
            val maxSearchWidth = (contentWidth - trailingReserve - actionWidth - spacing)
                .coerceAtLeast(actionWidth)
            // 库里的搜索框本身不带宽度动画，这里用同一个 progress 让它随展开/收起收放。
            val currentSearchWidth = lerp(actionWidth, maxSearchWidth, progress)

            // 只拦截搜索区这一段的点击，防止穿透到底层原顶栏元素；
            // 设置按钮那一格留出来，展开搜索时它仍然是活的。
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = trailingReserve)
                    .clickable(
                        interactionSource = interaction,
                        indication = null,
                    ) {},
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = horizontalPadding),
            ) {
                // 左侧返回按钮：同步变成标准的 40dp 高亮圆球
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .graphicsLayer {
                            alpha = progress
                            scaleX = 0.5f + 0.5f * progress
                            scaleY = 0.5f + 0.5f * progress
                        },
                ) {
                    CouiHomeTopActionButton(
                        icon = COUIIcons.Back,
                        contentDescription = "返回",
                        onClick = { onExpandedChange(false) },
                    )
                }

                // 搜索框直接用 COUI 原生 InputField：胶囊、描边、外发光、清空键、
                // 焦点与键盘行为全部由前端库自己绘制；这里只给它一个随进度收放的宽度。
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .offset(x = -trailingReserve)
                        .width(currentSearchWidth)
                        .height(actionWidth),
                ) {
                    InputField(
                        query = query,
                        onQueryChange = { query = it },
                        onSearch = { submit(it) },
                        expanded = expanded,
                        onExpandedChange = { if (!it) onExpandedChange(false) },
                        modifier = Modifier.fillMaxSize(),
                        label = if (expanded) "搜索视频、UP主..." else "",
                    )
                }
            }
        }
    }

    if (!expanded || query.isBlank()) return
    val suggestions = state.suggestions
    androidx.compose.ui.window.Popup(
        alignment = Alignment.TopStart,
        offset = androidx.compose.ui.unit.IntOffset(0, with(androidx.compose.ui.platform.LocalDensity.current) { rowHeight.roundToPx() }),
        properties = androidx.compose.ui.window.PopupProperties(
            focusable = false,
            usePlatformDefaultWidth = false,
            clippingEnabled = false,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacingTokens.Large)
                .background(
                    // Popup 底下没有可叠的父层：COUI 的容器色在深色下是半透明白，
                    // 直接铺会透出下面的信息流，先合成到画布色再画。
                    run {
                        val container = AppSurfaceTokens.surfaceContainer()
                        if (container.alpha < 1f) {
                            com.android.purebilibili.core.theme.opaqueCompositeOver(
                                container,
                                AppSurfaceTokens.background(),
                            )
                        } else {
                            container
                        }
                    },
                    AppShapes.container(ContainerLevel.Card),
                )
                .padding(vertical = AppSpacingTokens.Small),
        ) {
            when {
                suggestions.isEmpty() -> InlineSearchHint("按回车搜索「${query.trim()}」")
                else -> suggestions.forEach { suggestion ->
                    InlineSearchSuggestionRow(
                        text = suggestion.keyword,
                        onClick = { submit(suggestion.keyword) },
                    )
                }
            }
        }
    }
}

@Composable
private fun InlineSearchSuggestionRow(
    text: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(
                horizontal = AppSpacingTokens.Large,
                vertical = AppSpacingTokens.Medium,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacingTokens.Medium),
    ) {
        AppIcon(
            imageVector = COUIIcons.Basic.Search,
            contentDescription = null,
            tint = AppContentColors.secondaryText(),
            modifier = Modifier.size(AppSpacingTokens.Large + AppSpacingTokens.Micro),
        )
        AppText(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = AppContentColors.primaryText(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun InlineSearchHint(text: String) {
    AppText(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = AppContentColors.secondaryText(),
        modifier = Modifier.padding(
            horizontal = AppSpacingTokens.Large,
            vertical = AppSpacingTokens.Medium + AppSpacingTokens.Micro,
        ),
    )
}
