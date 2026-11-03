package com.android.purebilibili.feature.dynamic.components

import com.android.purebilibili.core.ui.AppSurfaceTokens
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.android.purebilibili.core.ui.components.appActionClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import io.github.suqi8.coui.kmp.basic.couiGlassSurface
import io.github.suqi8.coui.kmp.blur.Backdrop as MiuixBackdrop
import io.github.suqi8.coui.kmp.theme.COUITheme

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
    containerColor: Color = AppSurfaceTokens.surfaceContainerHigh(),
) {
    if (LocalAppUiStyle.current == AppUiStyle.COUI17) {
        CouiCommentEntryBar(onClick = onClick, backdrop = backdrop, modifier = modifier)
        return
    }

    val shape = resolveSharedBottomBarCapsuleShape()
    val textColor = resolveBottomInputBarPlaceholderTextColor(
        inputContainerColor = if (liquidGlassEnabled) {
            AppSurfaceTokens.surfaceContainer()
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
                    textStyle = COUITheme.textStyles.body1,
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
                textStyle = COUITheme.textStyles.footnote1,
            )
        }
    }
}

/**
 * COUI 皮肤下的评论入口：复用 ColorOS 搜索框胶囊，只把放大镜换成编辑图标。
 *
 * 输入框本身禁用编辑与聚焦，点击落到共享输入弹层，所以按压时不会先弹键盘再收回去。
 * COUI 的搜索框本身没有缩放或水波纹反馈，面板一出现就是它自己的上滑弹簧，这里保持一致。
 *
 * 材质由外层铺：`InputField` 自带的 backdrop 分支取的是顶栏预设（模糊半径 0），
 * 落在页面上只剩两层几乎看不见的混色，等于全透明，所以这里换成真正的玻璃层。
 */
@Composable
private fun CouiCommentEntryBar(
    onClick: () -> Unit,
    backdrop: MiuixBackdrop?,
    modifier: Modifier = Modifier,
) {
    val capsule = RoundedCornerShape(percent = 50)
    val containerColor = COUITheme.colorScheme.background
    val colors = SearchBarDefaults.searchBarColors(backgroundColor = Color.Transparent)
    Box(
        modifier = modifier
            .clip(capsule)
            .then(
                Modifier.couiGlassSurface(
                    backdrop = backdrop,
                    shape = capsule,
                    containerColor = containerColor,
                    fallback = Modifier.background(containerColor, capsule),
                    blurRadius = 20f,
                    containerAlpha = 0.72f,
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
    ) {
        AppCouiInputField(
            query = "",
            onQueryChange = {},
            onSearch = {},
            expanded = false,
            onExpandedChange = {},
            placeholder = "写评论",
            enabled = false,
            colors = colors,
            glowColor = null,
            backdrop = null,
            leadingIcon = {
                AppIcon(
                    imageVector = AppIcons.Outlined.Edit,
                    contentDescription = null,
                    tint = colors.iconColor,
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
    }
}
