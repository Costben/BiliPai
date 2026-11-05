package com.android.purebilibili.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.suqi8.coui.kmp.basic.TabRowDefaults
import io.github.suqi8.coui.kmp.basic.TabRowWithContour
import io.github.suqi8.coui.kmp.theme.COUITheme

/** [TabRowWithContour] 的段内水平内边距（COUI `SegmentPaddingHorizontal`）。 */
private val ContourSegmentPadding = 12.dp

/** 放不下时允许把段内边距收到的最小值，再小文字就贴到段边界了。 */
private val ContourMinSegmentPadding = 4.dp

/** 收紧段内边距时给文字留的余量，避免量宽与绘制字宽差一像素就退化成省略号。 */
private val ContourLabelGuard = 1.dp

private data class ContourGeometry(
    val contentWidth: Dp,
    val segmentPadding: Dp,
    val segmentMinWidth: Dp,
)

/**
 * COUI 17 顶部分类栏：整行一条实心胶囊底色，选中项是叠在底色上的滑动胶囊。
 * 首页与动态页共用这一份几何，两处顶栏不再各画一套。
 */
@Composable
fun AppCouiContourTabRow(
    tabs: List<String>,
    selectedIndex: Int,
    height: Dp,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (tabs.isEmpty()) return
    val contourPadding = TabRowDefaults.TabRowWithContourPadding
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    // 与控件内部的测量保持一致：COUI 用选中态外观（body2 + Medium）量段宽。
    val labelFontSize = COUITheme.textStyles.body2.fontSize
    val measureStyle = remember(labelFontSize) {
        TextStyle(
            fontSize = labelFontSize,
            fontWeight = FontWeight.Medium,
        )
    }
    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        val slotWidth = maxWidth
        val bounded = constraints.hasBoundedWidth
        val geometry = remember(tabs, slotWidth, bounded, density, measureStyle, textMeasurer) {
            val labelWidths = tabs.map { label ->
                with(density) {
                    textMeasurer.measure(
                        text = AnnotatedString(label),
                        style = measureStyle,
                        maxLines = 1,
                    ).size.width.toDp()
                }
            }
            resolveContourGeometry(
                labelWidths = labelWidths,
                slotWidth = if (bounded) slotWidth else Dp.Infinity,
                contourPadding = contourPadding,
            )
        }
        // 控件把容器铺满收到的宽度，但段宽只取自然宽度，铺满会留下一段右侧空白。
        // 内容放得下时先收窄父层的最大宽度，让胶囊像首页那样按内容收口并居中。
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally),
        ) {
            Box(modifier = Modifier.widthIn(max = geometry.contentWidth)) {
                TabRowWithContour(
                    tabs = tabs,
                    selectedTabIndex = selectedIndex.coerceIn(0, tabs.lastIndex),
                    onTabSelected = onTabSelected,
                    modifier = Modifier.fillMaxWidth(),
                    // 默认 tabRowColors 是半透明容器（浅色 6% 黑 / 深色 12% 白），背后内容会透上来；
                    // 顶栏要实心胶囊，用 ColorOS 设置页那套不透明容器色。
                    colors = TabRowDefaults.opaqueTabRowColors(),
                    minWidth = geometry.segmentMinWidth,
                    height = height,
                    // 指示器高度是行高减去上下内缩，圆角取它的一半才是胶囊。
                    cornerRadius = (height - contourPadding * 2) / 2,
                    contourPadding = contourPadding,
                    segmentPadding = geometry.segmentPadding,
                    // 标签背后是 pager，拖拽选段只在松手时提交，避免横滑期间反复翻页。
                    commitOnRelease = true,
                )
            }
        }
    }
}

/**
 * 决定胶囊宽度与段内边距。
 *
 * 默认几何（每段至少 52dp、段内边距 12dp）放得下时按内容收口，和首页一致。
 * 放不下时若照旧交给控件滚动，最后一段会被容器边缘切掉、右侧留一截空底色；
 * 改成收紧段内边距并去掉段宽下限，让各段等宽铺满，标签仍然居中不折字。
 */
private fun resolveContourGeometry(
    labelWidths: List<Dp>,
    slotWidth: Dp,
    contourPadding: Dp,
): ContourGeometry {
    val preferredMinWidth = TabRowDefaults.TabRowWithContourMinWidth
    val preferredContentWidth = labelWidths.fold(contourPadding * 2) { total, labelWidth ->
        total + maxOf(labelWidth + ContourSegmentPadding * 2, preferredMinWidth)
    }
    if (preferredContentWidth <= slotWidth) {
        return ContourGeometry(
            contentWidth = preferredContentWidth,
            segmentPadding = ContourSegmentPadding,
            segmentMinWidth = preferredMinWidth,
        )
    }
    val slotPerSegment = (slotWidth - contourPadding * 2) / labelWidths.size
    val fittedPadding = (slotPerSegment - labelWidths.max()) / 2 - ContourLabelGuard
    return ContourGeometry(
        contentWidth = slotWidth,
        segmentPadding = fittedPadding.coerceIn(ContourMinSegmentPadding, ContourSegmentPadding),
        segmentMinWidth = 0.dp,
    )
}
