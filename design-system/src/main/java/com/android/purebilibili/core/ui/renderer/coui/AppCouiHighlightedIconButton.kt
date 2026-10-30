package com.android.purebilibili.core.ui.renderer.coui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.takeOrElse
import com.android.purebilibili.core.ui.components.AppIconButtonColors
import com.android.purebilibili.core.ui.components.LocalAppActionContentColor
import io.github.suqi8.coui.kmp.basic.HighlightedIconButton
import io.github.suqi8.coui.kmp.theme.LocalContentColor as CouiLocalContentColor

/**
 * ColorOS 17 顶栏工具按钮：40dp 圆形径向填充、边缘描边与外侧光晕，按下时整体缩放。
 * 光晕画在节点之外，调用链上不能裁剪。
 */
@Composable
internal fun AppCouiHighlightedIconButton(
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    colors: AppIconButtonColors?,
    content: @Composable () -> Unit,
) {
    val neutralContentColor = LocalAppActionContentColor.current.takeOrElse { CouiLocalContentColor.current }
    val contentColor = if (enabled) {
        colors?.contentColor?.takeOrElse { neutralContentColor } ?: neutralContentColor
    } else {
        colors?.disabledContentColor?.takeOrElse { neutralContentColor.copy(alpha = 0.38f) }
            ?: neutralContentColor.copy(alpha = 0.38f)
    }
    HighlightedIconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
    ) {
        CompositionLocalProvider(
            CouiLocalContentColor provides contentColor,
            androidx.compose.material3.LocalContentColor provides contentColor,
            LocalAppActionContentColor provides contentColor,
        ) { content() }
    }
}
