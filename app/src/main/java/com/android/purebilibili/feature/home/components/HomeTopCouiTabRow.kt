// 文件路径: feature/home/components/HomeTopCouiTabRow.kt
package com.android.purebilibili.feature.home.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.android.purebilibili.core.theme.AppUiStyle
import com.android.purebilibili.core.ui.components.AppCouiContourTabRow

/**
 * COUI 17 顶部分类栏只在纯文字模式下接管：ColorOS 分段标签负责等宽分配、
 * 滑动胶囊、按压反馈与拖拽选段，图文 / 纯图标模式仍走自绘图标管线。
 */
internal fun shouldUseCouiHomeTopTabRow(
    uiStyle: AppUiStyle,
    liquidGlassEnabled: Boolean,
    skinPlainStyle: Boolean,
    hasSkinStickerIcons: Boolean,
    showIcon: Boolean,
    showText: Boolean,
    forceMaterialUnderline: Boolean,
): Boolean = uiStyle == AppUiStyle.COUI17 &&
    !liquidGlassEnabled &&
    !skinPlainStyle &&
    !hasSkinStickerIcons &&
    showText &&
    !showIcon &&
    !forceMaterialUnderline

/**
 * 首页顶部分类栏的 COUI 17 实现：与动态页共用 [AppCouiContourTabRow] 的
 * 胶囊底色 + 选中框几何，页面只负责提供当前选中项和点击回调。
 */
@Composable
internal fun CouiHomeTopTabRow(
    categories: List<String>,
    selectedIndex: Int,
    height: Dp,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCouiContourTabRow(
        tabs = categories,
        selectedIndex = selectedIndex,
        height = height,
        onTabSelected = onTabSelected,
        modifier = modifier,
    )
}
