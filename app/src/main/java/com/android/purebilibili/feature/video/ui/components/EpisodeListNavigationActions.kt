package com.android.purebilibili.feature.video.ui.components

import com.android.purebilibili.core.ui.AppIcons

import com.android.purebilibili.core.ui.AppContentColors

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.ui.components.AppIcon
import com.android.purebilibili.core.ui.components.AppIconButton

/** Shared collection / multipart list navigation controls. */
@Composable
internal fun RowScope.EpisodeListNavigationActions(
    onTopClick: () -> Unit,
    onBottomClick: () -> Unit,
    onCurrentClick: () -> Unit,
    hasItems: Boolean = true,
    hasCurrent: Boolean = true,
) {
    AppIconButton(onClick = onTopClick, enabled = hasItems) {
        AppIcon(AppIcons.Outlined.VerticalAlignTop, "跳至顶部", tint = AppContentColors.secondaryText(), modifier = Modifier.size(20.dp))
    }
    AppIconButton(onClick = onBottomClick, enabled = hasItems) {
        AppIcon(AppIcons.Outlined.VerticalAlignBottom, "跳至底部", tint = AppContentColors.secondaryText(), modifier = Modifier.size(20.dp))
    }
    AppIconButton(onClick = onCurrentClick, enabled = hasCurrent) {
        AppIcon(AppIcons.Outlined.MyLocation, "定位当前播放", tint = AppContentColors.secondaryText(), modifier = Modifier.size(20.dp))
    }
}
