package com.android.purebilibili.feature.settings

import com.android.purebilibili.core.ui.AppIcons

import com.android.purebilibili.core.ui.AppContentColors

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.android.purebilibili.R
import com.android.purebilibili.core.ui.components.*
import io.github.suqi8.coui.kmp.theme.COUITheme

@Composable
internal fun SettingsSearchHistorySection(
    history: List<String>,
    onQueryClick: (String) -> Unit,
    onDelete: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            AppText(stringResource(R.string.settings_search_history_title), style = COUITheme.textStyles.headline1, modifier = Modifier.weight(1f))
            if (history.isNotEmpty()) {
                AppTextButton(onClick = onClear, modifier = Modifier.heightIn(min = 48.dp)) {
                    AppText(stringResource(R.string.settings_search_history_clear_all))
                }
            }
        }
        if (history.isEmpty()) {
            AppText(
                stringResource(R.string.settings_search_history_empty),
                style = COUITheme.textStyles.body1,
                color = AppContentColors.secondaryText(),
                modifier = Modifier.padding(vertical = 16.dp),
            )
        } else AppPreferenceGroup {
            history.forEachIndexed { index, query ->
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                            .clickable { onQueryClick(query) }.padding(horizontal = 16.dp, vertical = 12.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        AppText(query, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                    AppIconButton(onClick = { onDelete(query) }, modifier = Modifier.size(48.dp)) {
                        AppIcon(AppIcons.Rounded.Close, contentDescription = stringResource(R.string.settings_search_history_delete, query))
                    }
                }
                if (index != history.lastIndex) AppPreferenceDivider()
            }
        }
    }
}
