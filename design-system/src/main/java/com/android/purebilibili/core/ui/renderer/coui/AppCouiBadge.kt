package com.android.purebilibili.core.ui.renderer.coui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.unit.dp
import io.github.suqi8.coui.kmp.squircle.squircleBackground
import io.github.suqi8.coui.kmp.theme.COUITheme

// COUI 的 Badge 是 count 驱动（<=0 画点，>0 画数字胶囊），而门面契约要求渲染任意 content，
// 所以这里按同一套几何手绘：无 content 画点，有 content 画胶囊。
@Composable
internal fun AppCouiBadge(
    modifier: Modifier,
    containerColor: Color,
    contentColor: Color,
    content: @Composable (RowScope.() -> Unit)?,
) {
    val resolvedContainer = containerColor.takeOrElse { COUITheme.colorScheme.error }
    val resolvedContent = contentColor.takeOrElse { COUITheme.colorScheme.onError }
    if (content == null) {
        Box(
            modifier = modifier
                .size(AppCouiBadgeDotSize)
                .squircleBackground(color = resolvedContainer, cornerRadius = AppCouiBadgeDotSize / 2),
        )
    } else {
        Row(
            modifier = modifier
                .defaultMinSize(minWidth = AppCouiBadgeHeight, minHeight = AppCouiBadgeHeight)
                .squircleBackground(color = resolvedContainer, cornerRadius = AppCouiBadgeCornerRadius)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CompositionLocalProvider(LocalContentColor provides resolvedContent) {
                content()
            }
        }
    }
}

private val AppCouiBadgeDotSize = 6.dp
private val AppCouiBadgeHeight = 16.dp
private val AppCouiBadgeCornerRadius = 8.dp
