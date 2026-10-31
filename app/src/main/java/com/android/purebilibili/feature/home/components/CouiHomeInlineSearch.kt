package com.android.purebilibili.feature.home.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import com.android.purebilibili.core.ui.isAppInDarkTheme
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
import io.github.suqi8.coui.kmp.basic.TopAppBarDefaults
import io.github.suqi8.coui.kmp.icon.COUIIcons
import io.github.suqi8.coui.kmp.icon.basic.Search
import io.github.suqi8.coui.kmp.icon.extended.Back
import io.github.suqi8.coui.kmp.icon.extended.Clear

/**
 * 首页顶栏内联搜索：点搜索键后从右侧向左展开一条透明搜索框，
 * 左边缘与前排头像左缘对齐；回车或点联想词才带着关键词进入搜索页。
 */
@Composable
internal fun CouiHomeInlineSearchRow(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSubmit: (String) -> Unit,
    rowHeight: Dp,
    horizontalPadding: Dp,
    contentColor: Color,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
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

    val progress by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = androidx.compose.animation.core.tween(
            durationMillis = 280,
            easing = androidx.compose.animation.core.CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f),
        ),
        label = "CouiHomeInlineSearchProgress",
    )

    LaunchedEffect(expanded) {
        if (expanded) {
            // 稍等动画展开一部分后再聚焦并弹键盘，体验更平滑
            kotlinx.coroutines.delay(120)
            focusRequester.requestFocus()
            keyboard?.show()
        } else {
            query = ""
            focusManager.clearFocus()
            keyboard?.hide()
            viewModel.dismissSuggestions()
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
        androidx.compose.foundation.layout.BoxWithConstraints(
            modifier = modifier
                .fillMaxWidth()
                .height(rowHeight)
                // 展开时拦截整个顶栏范围点击，防止穿透到底层原顶栏元素
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                ) {},
        ) {
            val contentWidth = maxWidth - horizontalPadding * 2
            val ballSize = TopAppBarDefaults.ActionItemMinWidth // 40.dp
            val spacing = TopAppBarDefaults.ActionMenuItemSpacing // 10.dp
            val maxSearchWidth = (contentWidth - ballSize - spacing).coerceAtLeast(ballSize)
            val currentSearchWidth = androidx.compose.ui.unit.lerp(ballSize, maxSearchWidth, progress)

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

                val isDark = isAppInDarkTheme()
                val capsuleShape = androidx.compose.foundation.shape.RoundedCornerShape(ballSize / 2)

                // 右侧搜索框：从右侧圆形（40dp）原地向左平滑拉长为 COUI 17 原生高光透明胶囊搜索框
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(currentSearchWidth)
                        .height(ballSize)
                        .dropShadow(
                            shape = capsuleShape,
                            shadow = Shadow(
                                radius = 8.dp,
                                color = Color.Black,
                                alpha = if (isDark) 0.22f else 0.08f,
                            ),
                        )
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(
                                    if (isDark) Color.White.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.55f),
                                    if (isDark) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.25f),
                                )
                            ),
                            shape = capsuleShape,
                        )
                        .border(
                            width = 1.25.dp,
                            brush = Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = if (isDark) 0.55f else 0.85f),
                                    Color.White.copy(alpha = if (isDark) 0.18f else 0.32f),
                                    Color.White.copy(alpha = if (isDark) 0.06f else 0.14f),
                                )
                            ),
                            shape = capsuleShape,
                        ),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AppIcon(
                            imageVector = COUIIcons.Basic.Search,
                            contentDescription = null,
                            tint = contentColor.copy(alpha = 0.75f),
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .graphicsLayer {
                                    alpha = ((progress - 0.35f) / 0.65f).coerceIn(0f, 1f)
                                },
                        ) {
                            if (query.isEmpty()) {
                                AppText(
                                    text = "搜索视频、番剧、UP主",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = contentColor.copy(alpha = 0.55f),
                                    maxLines = 1,
                                )
                            }
                            BasicTextField(
                                value = query,
                                onValueChange = { query = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester),
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = contentColor,
                                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                                ),
                                cursorBrush = SolidColor(contentColor),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = { submit(query) }),
                            )
                        }
                        if (query.isNotEmpty() && progress > 0.7f) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable(
                                        interactionSource = interaction,
                                        indication = null,
                                    ) { query = "" },
                                contentAlignment = Alignment.Center,
                            ) {
                                AppIcon(
                                    imageVector = COUIIcons.Clear,
                                    contentDescription = "清空",
                                    tint = contentColor.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
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
                    AppSurfaceTokens.surfaceContainer(),
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
