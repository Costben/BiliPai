package com.android.purebilibili.feature.video.ui.components

import com.android.purebilibili.core.ui.AppIcons

import com.android.purebilibili.core.ui.components.appActionClickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.android.purebilibili.core.store.FullscreenActionCountSettings
import com.android.purebilibili.core.ui.ContainerLevel
import com.android.purebilibili.core.ui.AppContentColors
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.core.ui.components.AppIcon
import com.android.purebilibili.core.ui.components.AppListItem
import com.android.purebilibili.core.ui.components.AppSurface
import com.android.purebilibili.core.ui.components.AppSwitch
import com.android.purebilibili.core.ui.components.AppText
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.*
import com.android.purebilibili.core.ui.LocalAppThemeConfig


/** 只在同窗玻璃设置容器实际接管后移除内容的原生实色底板。 */
internal val LocalPlayerGlassSettingsSurface = compositionLocalOf { false }

internal fun playerNativeMenuConfig(config: com.android.purebilibili.core.ui.AppThemeConfig) =
    config.copy(nativeMiuixPopupsEnabled = false)
/** 播放器原生面板跟随应用皮肤：保留原生菜单结构，只关闭玻璃菜单的独立 Miuix 弹窗。 */
@Composable
internal fun PlayerNativeMenuTheme(content: @Composable () -> Unit) {
    val config = LocalAppThemeConfig.current
    CompositionLocalProvider(
        LocalAppThemeConfig provides playerNativeMenuConfig(config),
        content = content,
    )
}

@Composable
internal fun PlayerNativeSettingsHost(
    onDismiss: () -> Unit,
    bottomPaddingDp: Int = 0,
    isFullscreen: Boolean = false,
    playerBounds: androidx.compose.ui.unit.IntRect? = null,
    content: @Composable () -> Unit,
) {
    val windowSize = androidx.compose.ui.platform.LocalWindowInfo.current.containerSize
    val config = LocalConfiguration.current
    val density = androidx.compose.ui.platform.LocalDensity.current
    val sidePanel = isFullscreen && windowSize.width > windowSize.height &&
        LocalPlayerPanelViewport.current != null
    val policy = resolveDanmakuSettingsPanelLayoutPolicy(
        isFullscreen = false,
        screenWidthDp = config.screenWidthDp,
        screenHeightDp = config.screenHeightDp,
    )
    val safeWindow = playerPanelSafeWindowBounds()
    val menuWidth = with(density) {
        if (sidePanel) {
            resolvePlayerPanelBounds(
                safeWindow, playerBounds, isFullscreen, landscape = true,
                maxSideWidth = 500.dp.roundToPx(),
            ).width.toDp()
        } else {
            // 原生外层有左右各 12dp 留白；两种材质共用可见面板宽度。
            (policy.maxWidthDp.dp - 24.dp).coerceAtMost(
                (safeWindow.width.toDp() - 32.dp).coerceAtLeast(1.dp),
            ).coerceAtLeast(1.dp)
        }
    }
    val glassMaxHeight = (config.screenHeightDp - 32).coerceAtLeast(1).dp
    // 玻璃只接管外壳与触发按钮开合，不再单独收窄主菜单及子页。
    if (com.android.purebilibili.core.ui.components.AppGlassMenu(
            expanded = true,
            onDismissRequest = onDismiss,
            title = "更多操作",
            placement = if (com.android.purebilibili.core.ui.components.LocalAppGlassMenuSource.current?.isAvailable == true) {
                com.android.purebilibili.core.ui.components.AppGlassMenuPlacement.ANCHOR_END
            } else if (isFullscreen) com.android.purebilibili.core.ui.components.AppGlassMenuPlacement.END
            else com.android.purebilibili.core.ui.components.AppGlassMenuPlacement.CENTER,
            minWidth = menuWidth,
            maxWidth = menuWidth,
            maxHeight = glassMaxHeight,
            interactiveContent = true,
            showScrollbar = false,
            useAvailableHeight = true,
        ) {
            PlayerNativeMenuTheme {
                CompositionLocalProvider(LocalPlayerGlassSettingsSurface provides true) {
                    Box(Modifier.width(menuWidth).heightIn(max = glassMaxHeight)) { content() }
                }
            }
        }) return
    // 播放器提供避让宿主时，横屏更多操作与选集共用侧面板；其他入口保留原有弹窗。
    if (sidePanel) {
        PlayerNativeMenuTheme {
            PlayerDetailPanel("更多操作", isFullscreen, playerBounds, onDismiss) { requestDismiss ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(45.dp)
                        .padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AppText("更多操作", fontSize = 16.sp)
                    Spacer(Modifier.weight(1f))
                    PanelAction(AppIcons.Outlined.Close, "关闭", requestDismiss)
                }
                content()
            }
        }
        return
    }
    PlayerNativeMenuTheme {
        DanmakuSettingsPanelHost(false, policy, onDismiss) {
            AppSurface(
                modifier = Modifier
                    .padding(12.dp)
                    .padding(bottom = bottomPaddingDp.dp)
                    .width(menuWidth)
                    .heightIn(max = policy.maxHeightDp.dp),
                color = AppSurfaceTokens.surface(),
                shape = AppShapes.container(ContainerLevel.Card),
            ) { content() }
        }
    }
}

@Composable
internal fun PlayerSettingsRow(title: String, subtitle: String? = null, onClick: (() -> Unit)?) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .then(if (onClick != null) Modifier.appActionClickable(onClick = onClick) else Modifier)
            .heightIn(min = 48.dp).padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (title == "弹幕设置") {
            PiliPlusDanmakuSettingsIcon(size = 20.dp, tint = AppContentColors.secondaryText())
        } else {
        AppIcon(
            imageVector = when (title) {
                "稍后再看（添加 / 移出）" -> AppIcons.Outlined.WatchLater
                "查看笔记" -> AppIcons.Outlined.EditNote
                "离线缓存" -> AppIcons.Outlined.FolderZip
                "保存字幕" -> AppIcons.Outlined.Download
                "保存封面" -> AppIcons.Outlined.Image
                "定时关闭" -> AppIcons.Outlined.HourglassTop
                "播放地址" -> AppIcons.Outlined.Link
                "重载视频" -> AppIcons.Outlined.Refresh
                "画质增强" -> AppIcons.Outlined.StayCurrentLandscape
                "播放器音量" -> AppIcons.Outlined.VolumeUp
                "画面变换" -> AppIcons.Outlined.Flip
                "恢复默认" -> AppIcons.Outlined.Restore
                "CDN 设置" -> AppIcons.Outlined.Cloud
                "选择画质" -> AppIcons.Outlined.PlayCircle
                "选择音质" -> AppIcons.Outlined.Album
                "首选编码格式" -> AppIcons.Outlined.AvTimer
                "播完后行为" -> AppIcons.Outlined.Repeat
                "弹幕列表" -> AppIcons.Outlined.Comment
                "字幕设置" -> AppIcons.Outlined.Subtitles
                "加载字幕" -> AppIcons.Outlined.FileOpen
                "播放信息" -> AppIcons.Outlined.Info
                "举报" -> AppIcons.Outlined.ErrorOutline
                FullscreenActionCountSettings.TITLE -> ImageVector.vectorResource(com.android.purebilibili.R.drawable.ic_settings_action_counts_24)
                "返回" -> AppIcons.Outlined.ArrowBack
                else -> AppIcons.Outlined.Settings
            },
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = AppContentColors.secondaryText(),
        )
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            AppText(title, fontSize = 14.sp, color = AppContentColors.primaryText())
            if (!subtitle.isNullOrBlank()) AppText(
                subtitle, fontSize = 12.sp, color = AppContentColors.secondaryText(),
            )
        }
    }
}

@Composable
internal fun PlayerSettingsPopupRow(
    title: String,
    current: String,
    options: List<String>,
    enabledOptions: List<Boolean> = List(options.size) { true },
    onSelected: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    // 玻璃设置面板内展开选项，不再为内部行创建第二层玻璃弹窗。
    if (LocalPlayerGlassSettingsSurface.current) {
        Column(Modifier.fillMaxWidth()) {
            PlayerSettingsRow(title, current) { expanded = !expanded }
            if (expanded) {
                options.forEachIndexed { index, label ->
                    PlayerSettingsSelectionRow(
                        title = label,
                        selected = label == current,
                        enabled = enabledOptions.getOrElse(index) { true },
                        allowSelectedClick = true,
                        onClick = { expanded = false; if (label != current) onSelected(index) },
                    )
                }
            }
        }
        return
    }
    com.android.purebilibili.core.ui.components.AppGlassMenuAnchor(onOpen = { expanded = true }) {
        PlayerSettingsRow(title, current) { expanded = true }
        com.android.purebilibili.core.ui.components.AppDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEachIndexed { index, label ->
                com.android.purebilibili.core.ui.components.AppDropdownMenuItem(
                    text = { AppText(label, fontSize = 14.sp) },
                    enabled = enabledOptions.getOrElse(index) { true },
                    onClick = {
                        expanded = false
                        if (label != current) onSelected(index)
                    },
                    trailingIcon = if (label == current) ({ AppIcon(AppIcons.Outlined.Done, null) }) else null,
                )
            }
        }
    }
}

@Composable
internal fun PlayerSettingsSelectionRow(
    title: String,
    selected: Boolean,
    enabled: Boolean = true,
    subtitle: String? = null,
    allowSelectedClick: Boolean = false,
    onClick: () -> Unit,
) {
    AppListItem(
        headlineContent = { AppText(title, fontSize = 14.sp, color = AppContentColors.primaryText().copy(alpha = if (enabled) 1f else 0.38f)) },
        supportingContent = if (subtitle != null) ({ AppText(subtitle, fontSize = 12.sp) }) else null,
        trailingContent = { if (selected) AppIcon(AppIcons.Outlined.Done, null, tint = AppContentColors.accent()) },
        modifier = Modifier
            .fillMaxWidth()
            // 玻璃容器接管时透出父容器；其他场景保留原生列表底色。
            .background(
                if (LocalPlayerGlassSettingsSurface.current) Color.Transparent
                else AppSurfaceTokens.surface(),
            )
            .appActionClickable(enabled = enabled) { if (!selected || allowSelectedClick) onClick() },
    )
}

/** 播放器子菜单沿用普通菜单行的字号与留白，不使用全局设置的彩色图标底座。 */
@Composable
internal fun PlayerSettingsToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .heightIn(min = 48.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppText(
            text = title,
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            color = AppContentColors.primaryText(),
        )
        AppSwitch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}
