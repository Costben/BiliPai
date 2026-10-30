package com.android.purebilibili.feature.home.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.purebilibili.core.ui.AppContentColors
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.AppSpacingTokens
import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.core.ui.ContainerLevel
import com.android.purebilibili.core.ui.MediaContrastPalette
import com.android.purebilibili.core.ui.components.AppIcon
import com.android.purebilibili.core.ui.components.AppText
import com.android.purebilibili.feature.search.SearchViewModel
import io.github.suqi8.coui.kmp.basic.InputField
import io.github.suqi8.coui.kmp.basic.SearchBarDefaults
import io.github.suqi8.coui.kmp.icon.COUIIcons
import io.github.suqi8.coui.kmp.icon.basic.Search

/**
 * 首页原地搜索：覆盖在首页之上，输入时直接展示联想词；
 * 回车或点联想词才带着关键词进入搜索页。
 */
@Composable
internal fun HomeInlineSearchOverlay(
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit,
    viewModel: SearchViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    fun submit(keyword: String) {
        val trimmed = keyword.trim()
        if (trimmed.isEmpty()) return
        keyboard?.hide()
        focusManager.clearFocus()
        onSubmit(trimmed)
        onDismiss()
    }

    LaunchedEffect(query) {
        viewModel.onQueryChange(query)
    }
    DisposableEffect(viewModel) {
        onDispose { viewModel.dismissSuggestions() }
    }
    BackHandler { onDismiss() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MediaContrastPalette.Scrim.copy(alpha = 0.32f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(
                    horizontal = AppSpacingTokens.Large,
                    vertical = AppSpacingTokens.Small,
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                ),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                InputField(
                    query = query,
                    onQueryChange = { query = it },
                    onSearch = { submit(it) },
                    expanded = true,
                    onExpandedChange = { expanded -> if (!expanded) onDismiss() },
                    modifier = Modifier.weight(1f),
                    label = "搜索视频、番剧、影视、UP主",
                    glowColor = SearchBarDefaults.causticGlowColor(),
                )
                Spacer(Modifier.width(AppSpacingTokens.Small))
                AppText(
                    text = "取消",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppContentColors.primaryText(),
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onDismiss,
                        )
                        .padding(
                            horizontal = AppSpacingTokens.ExtraSmall,
                            vertical = AppSpacingTokens.Small,
                        ),
                )
            }
            Spacer(Modifier.height(AppSpacingTokens.Small))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        AppSurfaceTokens.surfaceContainer(),
                        AppShapes.container(ContainerLevel.Card),
                    )
                    .padding(vertical = AppSpacingTokens.Small),
            ) {
                val suggestions = state.suggestions
                if (query.isNotBlank()) {
                    if (suggestions.isEmpty()) {
                        InlineSearchHint("按回车搜索「${query.trim()}」")
                    } else {
                        suggestions.forEach { suggestion ->
                            InlineSearchSuggestionRow(
                                text = suggestion.keyword,
                                onClick = { submit(suggestion.keyword) },
                            )
                        }
                    }
                } else {
                    val history = state.historyList
                    if (history.isNotEmpty()) {
                        InlineSearchSectionTitle("搜索历史")
                        history.take(4).forEach { item ->
                            InlineSearchSuggestionRow(
                                text = item.keyword,
                                icon = COUIIcons.Basic.Search,
                                onClick = { query = item.keyword },
                            )
                        }
                    }
                    val hot = state.hotList
                    if (hot.isNotEmpty()) {
                        InlineSearchSectionTitle("热门搜索")
                        hot.take(6).forEach { item ->
                            InlineSearchSuggestionRow(
                                text = item.title.ifBlank { item.keyword },
                                icon = COUIIcons.Basic.Search,
                                onClick = { submit(item.keyword) },
                            )
                        }
                    }
                    if (history.isEmpty() && hot.isEmpty()) {
                        InlineSearchHint("输入关键词，回车搜索")
                    }
                }
            }
        }
    }
}

@Composable
private fun InlineSearchSuggestionRow(
    text: String,
    onClick: () -> Unit,
    icon: ImageVector = COUIIcons.Basic.Search,
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
            imageVector = icon,
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
private fun InlineSearchSectionTitle(title: String) {
    AppText(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Medium,
        color = AppContentColors.secondaryText(),
        modifier = Modifier.padding(
            horizontal = AppSpacingTokens.Large,
            vertical = AppSpacingTokens.Small,
        ),
    )
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
