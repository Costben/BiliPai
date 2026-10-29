package com.android.purebilibili.feature.settings

import com.android.purebilibili.core.theme.AppUiStyle
import com.android.purebilibili.core.ui.components.AppSegmentOption

// Miuix 皮肤已由 COUI 17 取代，选择器不再提供它；旧存档在读取层迁移。
internal fun resolveThemeSelectionOptions(
    material3Label: String,
    couiLabel: String,
): List<AppSegmentOption<AppUiStyle>> {
    return listOf(
        AppSegmentOption(AppUiStyle.MATERIAL3, material3Label),
        AppSegmentOption(AppUiStyle.COUI17, couiLabel),
    )
}
