package com.android.purebilibili.core.ui.renderer.coui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.ui.components.AppSegmentOption
import com.android.purebilibili.core.ui.components.resolveAppSegmentedSelectionIndex
import io.github.suqi8.coui.kmp.basic.ChipTabData
import io.github.suqi8.coui.kmp.basic.ChipTabGroup

/**
 * COUI 17 的标签行：ColorOS 的 `ChipTabGroup`，一个选项一颗胶囊。
 *
 * 与 Miuix 时期的分段块不同，选项宽度按文字自适应、行内横向滚动，选中态是
 * `surfaceContainerHighest` 实心胶囊加描边，未选中态只有一层 `container4` 底色。
 * 调用方的等宽 / 固定槽位参数在这里没有对应物，胶囊行按内容排布。
 */
@Composable
internal fun <T> AppCouiChipTabRow(
    options: List<AppSegmentOption<T>>,
    selectedValue: T,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    containerHorizontalPadding: Dp = 0.dp,
    onSelectionChange: (T) -> Unit,
) {
    if (options.isEmpty()) return
    val selectedIndex = resolveAppSegmentedSelectionIndex(options, selectedValue)
    val tabs = remember(options, enabled) {
        options.map { option -> ChipTabData(label = option.label, enabled = enabled) }
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = containerHorizontalPadding),
        contentAlignment = Alignment.CenterStart,
    ) {
        ChipTabGroup(
            tabs = tabs,
            selectedIndex = selectedIndex,
            onSelectedChange = { index ->
                if (enabled) {
                    options.getOrNull(index)?.let { onSelectionChange(it.value) }
                }
            },
        )
    }
}
