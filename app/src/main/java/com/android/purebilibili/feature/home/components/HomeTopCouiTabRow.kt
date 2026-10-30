// 文件路径: feature/home/components/HomeTopCouiTabRow.kt
package com.android.purebilibili.feature.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.theme.AppUiStyle
import io.github.suqi8.coui.kmp.basic.TabRowDefaults
import io.github.suqi8.coui.kmp.basic.TabRowWithContour

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
 * 首页顶部分类栏的 COUI 17 实现：使用带胶囊背景容器的 ColorOS 分段标签控件，
 * 页面只负责提供当前选中项和点击回调。
 */
@Composable
internal fun CouiHomeTopTabRow(
    categories: List<String>,
    selectedIndex: Int,
    height: Dp,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (categories.isEmpty()) return
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        val contourPadding = TabRowDefaults.TabRowWithContourPadding
        // 段宽由控件按容器宽度和文字自然宽度分配：不传外部最小宽度，否则多分类会溢出容器。
        TabRowWithContour(
            tabs = categories,
            selectedTabIndex = selectedIndex.coerceIn(0, categories.lastIndex),
            onTabSelected = onTabSelected,
            modifier = Modifier.fillMaxWidth(),
            // 默认 tabRowColors 是半透明容器（浅色 6% 黑 / 深色 12% 白），背后内容会透上来；
            // 顶栏要实心胶囊，用 ColorOS 设置页那套不透明容器色。
            colors = TabRowDefaults.opaqueTabRowColors(),
            height = height,
            // 指示器高度是行高减去上下 4dp 内缩，圆角取它的一半才是胶囊。
            cornerRadius = (height - contourPadding * 2) / 2,
            contourPadding = contourPadding,
            // 首页标签背后是 pager，拖拽选段只在松手时提交，避免横滑期间反复翻页。
            commitOnRelease = true,
        )
    }
}
