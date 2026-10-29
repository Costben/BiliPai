package com.android.purebilibili.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.android.purebilibili.R
import io.github.suqi8.coui.kmp.basic.BasicComponent
import io.github.suqi8.coui.kmp.basic.Card
import io.github.suqi8.coui.kmp.basic.HorizontalDivider
import io.github.suqi8.coui.kmp.basic.Icon
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
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            CouiDonatePreference(onClick = onDonateClick)
        }
    }
}

@Composable
private fun CouiSettingsSearchEntry(onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        BasicComponent(
            title = stringResource(R.string.settings_search_placeholder),
            titleColor = io.github.suqi8.coui.kmp.basic.BasicComponentDefaults.summaryColor(),
            startAction = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = COUITheme.colorScheme.onSurfaceSecondary,
                    modifier = Modifier.size(20.dp),
                )
            },
            onClick = onClick,
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
