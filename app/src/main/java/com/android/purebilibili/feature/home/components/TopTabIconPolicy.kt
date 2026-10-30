package com.android.purebilibili.feature.home.components

import com.android.purebilibili.core.ui.AppIcons

import androidx.compose.ui.graphics.vector.ImageVector
import com.android.purebilibili.feature.home.HomeCategory

/**
 * 顶部栏目固定使用 COUI 图标（Regular 字重）。
 *
 * 顶部 Tab 通过紧凑的圆角选中背景表达状态，因此图标本身不随选中状态切换填充版本。
 * 底部导航仍使用独立的 Miuix 图标策略。
 */
internal fun resolveTopTabIcon(categoryKey: String): ImageVector {
    val normalizedKey = categoryKey.trim()
    val category = HomeCategory.entries.firstOrNull { entry ->
        entry.name.equals(normalizedKey, ignoreCase = true) || entry.label == normalizedKey
    }

    return when (category) {
        HomeCategory.RECOMMEND -> AppIcons.Rounded.Home
        HomeCategory.FOLLOW -> AppIcons.Rounded.People
        HomeCategory.POPULAR -> AppIcons.Rounded.Whatshot
        HomeCategory.ANIME -> AppIcons.Rounded.CollectionsBookmark
        HomeCategory.LIVE -> AppIcons.Rounded.LiveTv
        HomeCategory.GAME -> AppIcons.Rounded.SportsEsports
        HomeCategory.KNOWLEDGE -> AppIcons.Rounded.School
        HomeCategory.TECH -> AppIcons.Rounded.Memory
        null -> AppIcons.Rounded.GridView
    }
}
