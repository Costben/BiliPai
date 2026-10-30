package com.android.purebilibili.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.android.purebilibili.R
import io.github.suqi8.coui.kmp.basic.Card
import io.github.suqi8.coui.kmp.basic.HorizontalDivider
import io.github.suqi8.coui.kmp.basic.Icon
import io.github.suqi8.coui.kmp.basic.SearchBarDefaults
import io.github.suqi8.coui.kmp.basic.Text
import io.github.suqi8.coui.kmp.basic.materialShadowEdge
import io.github.suqi8.coui.kmp.icon.COUIIcons
import io.github.suqi8.coui.kmp.icon.basic.Search
import io.github.suqi8.coui.kmp.preference.ArrowPreference
import io.github.suqi8.coui.kmp.theme.COUITheme

/**
 * COUI 17 皮肤下的设置首页。
 *
 * 只换"皮"不换逻辑：分类来源、文案与点击回调全部复用 SettingsScreen 的输入，
 * 所以 Material 3 / Miuix / COUI 17 跑的是同一份设置导航与状态。
 * 其余页面暂时仍走旧皮肤，这是迁移期的收敛点而不是最终形态。
 */
@Composable
internal fun CouiSettingsHomeContent(
    categories: List<SettingsRootCategory>,
    onCategoryClick: (SettingsRootCategory) -> Unit,
    onDonateClick: () -> Unit,
    onSearchOpen: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        CouiSettingsSearchEntry(onClick = onSearchOpen)
        Spacer(Modifier.height(12.dp))
        // COUI 的分割线从文字起点开始：内边距 16 + 图标 20 + 图标与文字间距 8。
        val dividerStartDp = 16 + (categories.firstOrNull()
            ?.let { rememberSettingsEntryVisual(it.searchTarget).iconSizeDp } ?: 20) + 8
        Card(modifier = Modifier.fillMaxWidth()) {
            categories.forEachIndexed { index, category ->
                val visual = rememberSettingsEntryVisual(category.searchTarget)
                ArrowPreference(
                    title = category.title,
                    summary = category.subtitle,
                    startAction = {
                        CouiSettingsEntryIcon(
                            icon = visual.icon,
                            contentDescription = null,
                            tint = COUITheme.colorScheme.primary,
                            sizeDp = visual.iconSizeDp,
                        )
                    },
                    onClick = { onCategoryClick(category) },
                )
                if (index != categories.lastIndex) {
                    HorizontalDivider(modifier = Modifier.padding(start = dividerStartDp.dp, end = 16.dp))
                }
            }
            HorizontalDivider(modifier = Modifier.padding(start = dividerStartDp.dp, end = 16.dp))
            CouiDonatePreference(onClick = onDonateClick)
        }
    }
}

/**
 * 设置首页的搜索入口。
 *
 * COUI 的搜索栏在收起态就是一颗胶囊：圆角等于半高，材质色与描边都来自
 * `SearchBarDefaults`。这里只画收起态外观，点击仍然进入搜索页，因此不像
 * `InputField` 那样接管焦点与键盘。
 */
@Composable
private fun CouiSettingsSearchEntry(onClick: () -> Unit) {
    val colors = SearchBarDefaults.searchBarColors()
    val stroke = SearchBarDefaults.searchBarStroke()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = SearchBarDefaults.InputFieldMinHeight)
            .clip(CircleShape)
            .background(color = colors.backgroundColor)
            .materialShadowEdge(stroke = stroke)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = COUIIcons.Basic.Search,
            contentDescription = null,
            tint = colors.iconColor,
            modifier = Modifier.padding(
                start = SearchBarDefaults.LeadingIconStartPadding,
                end = SearchBarDefaults.LeadingIconEndPadding,
            ),
        )
        Text(
            text = stringResource(R.string.settings_search_placeholder),
            color = colors.labelColor,
            style = TextStyle(fontSize = SearchBarDefaults.InputFieldFontSize),
        )
    }
}

@Composable
private fun CouiDonatePreference(onClick: () -> Unit) {
    val visual = rememberSettingsEntryVisual(SettingsSearchTarget.DONATE)
    val copy = settingsDestinationCopy(SettingsSearchTarget.DONATE)
    ArrowPreference(
        title = copy.title,
        summary = copy.summary,
        startAction = {
            CouiSettingsEntryIcon(
                icon = visual.icon,
                contentDescription = null,
                tint = COUITheme.colorScheme.primary,
                sizeDp = visual.iconSizeDp,
            )
        },
        onClick = onClick,
    )
}

@Composable
private fun CouiSettingsEntryIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
    contentDescription: String?,
    tint: Color,
    sizeDp: Int,
) {
    if (icon == null) {
        Spacer(Modifier.size(sizeDp.dp))
        return
    }
    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = tint,
        modifier = Modifier.size(sizeDp.dp),
    )
}
