package com.android.purebilibili.feature.dynamic.components

import androidx.compose.foundation.background
import com.android.purebilibili.core.ui.components.appActionClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.theme.AppUiStyle
import com.android.purebilibili.core.theme.LocalAppUiStyle
import com.android.purebilibili.core.ui.AppContentColors
import com.android.purebilibili.core.ui.AppIcons
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.ContainerLevel
import com.android.purebilibili.core.ui.components.AppIcon
import com.android.purebilibili.core.ui.components.appElasticPress
import com.android.purebilibili.core.ui.renderer.coui.AppCouiInputField
import com.android.purebilibili.feature.home.components.BottomBarMatchedReusableLiquidDock
import com.android.purebilibili.feature.home.components.resolveFloatingDockGeometryScale
import com.android.purebilibili.feature.home.components.resolveSharedBottomBarCapsuleShape
import com.android.purebilibili.feature.video.ui.components.CommentEntryBarContent
import com.android.purebilibili.feature.video.ui.components.resolveBottomInputBarPlaceholderTextColor
import io.github.suqi8.coui.kmp.basic.SearchBarDefaults
import io.github.suqi8.coui.kmp.blur.Backdrop as MiuixBackdrop

/**
 * 动态评论输入入口。
 *
 * 只负责打开共享的 [com.android.purebilibili.feature.video.ui.components.CommentInputDialog]；
 * 动态不再保留可编辑的内联输入框。
 */
@Composable
internal fun DynamicCommentEntryBar(
    onClick: () -> Unit,
    liquidGlassEnabled: Boolean,
    backdrop: MiuixBackdrop?,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
) {
    if (LocalAppUiStyle.current == AppUiStyle.COUI17) {
        CouiCommentEntryBar(onClick = onClick, backdrop = backdrop, modifier = modifier)
        return
    }

    val shape = resolveSharedBottomBarCapsuleShape()
    val textColor = resolveBottomInputBarPlaceholderTextColor(
        inputContainerColor = if (liquidGlassEnabled) {
            MaterialTheme.colorScheme.surfaceContainer
        } else {
            containerColor
        },
        onSurfaceColor = AppContentColors.primaryText(),
        onSurfaceVariantColor = AppContentColors.secondaryText(),
    )

    if (liquidGlassEnabled) {
        BottomBarMatchedReusableLiquidDock(
            shape = shape,
            modifier = modifier
                .height(44.dp)
                .clip(shape),
            backdrop = backdrop,
            reuseEnabled = true,
            elasticFeedbackEnabled = true,
            drawShellLens = true,
            shellLensIntensity = resolveFloatingDockGeometryScale(44f),
        ) { liquidChromeActive ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (liquidChromeActive) Color.Transparent else containerColor)
                    .appActionClickable(role = Role.Button, onClick = onClick)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CommentEntryBarContent(
                    textColor = textColor,
                    iconSize = 20.dp,
                    iconSpacing = 6.dp,
                    textStyle = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    } else {
        Box(
            modifier = modifier
                .height(48.dp)
                .appElasticPress()
                .clip(AppShapes.container(ContainerLevel.Card))
                .background(containerColor)
                .appActionClickable(role = Role.Button, onClick = onClick)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            CommentEntryBarContent(
                textColor = textColor,
                iconSize = 18.dp,
                iconSpacing = 8.dp,
                textStyle = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

/**
 * COUI 皮肤下的评论入口：直接复用 ColorOS 搜索框胶囊，只把放大镜换成编辑图标。
 *
 * 输入框本身禁用编辑与聚焦，点击由覆盖层接管，落到共享输入弹层。
 */
@Composable
private fun CouiCommentEntryBar(
    onClick: () -> Unit,
    backdrop: MiuixBackdrop?,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.appElasticPress()) {
        AppCouiInputField(
            query = "",
            onQueryChange = {},
            onSearch = {},
            expanded = false,
            onExpandedChange = {},
            placeholder = "写评论",
            enabled = false,
            glowColor = null,
            backdrop = backdrop,
            leadingIcon = {
                AppIcon(
                    imageVector = AppIcons.Outlined.Edit,
                    contentDescription = null,
                    tint = SearchBarDefaults.searchBarColors().iconColor,
                    modifier = Modifier
                        .padding(
                            start = SearchBarDefaults.LeadingIconStartPadding,
                            end = SearchBarDefaults.LeadingIconEndPadding,
                        )
                        .size(20.dp),
                )
            },
            modifier = Modifier.fillMaxWidth(),
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .appActionClickable(role = Role.Button, onClick = onClick),
        )
    }
}
