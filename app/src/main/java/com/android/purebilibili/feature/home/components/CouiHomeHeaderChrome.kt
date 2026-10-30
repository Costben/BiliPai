package com.android.purebilibili.feature.home.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.suqi8.coui.kmp.basic.AppBarBlurOverlay
import io.github.suqi8.coui.kmp.basic.HighlightedIconButton
import io.github.suqi8.coui.kmp.basic.Icon
import io.github.suqi8.coui.kmp.basic.LocalEdgeToEdge
import io.github.suqi8.coui.kmp.blur.Backdrop

/**
 * 首页顶栏的 ColorOS 17 材质：渐变模糊从状态栏顶端最强，越过分类栏底边后渐隐。
 * 首页信息流在顶栏下方滚动，因此声明 edge-to-edge 让材质采样其下内容。
 */
@Composable
internal fun CouiHomeHeaderMaterial(
    height: Dp,
    backdrop: Backdrop?,
) {
    CompositionLocalProvider(LocalEdgeToEdge provides true) {
        AppBarBlurOverlay(
            modifier = Modifier
                .fillMaxWidth()
                .height(height),
            backdrop = backdrop,
        )
    }
}

/** 顶栏右侧工具按钮：COUI 的 40dp 圆形光影按钮，按下反馈由按钮自身的光晕与缩放承担。 */
@Composable
internal fun CouiHomeTopActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HighlightedIconButton(
        onClick = onClick,
        modifier = modifier.semantics { this.contentDescription = contentDescription },
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
        )
    }
}
