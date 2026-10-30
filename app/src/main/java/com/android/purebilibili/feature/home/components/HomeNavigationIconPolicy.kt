package com.android.purebilibili.feature.home.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.android.purebilibili.R
import io.github.suqi8.coui.kmp.icon.COUIIcons
import io.github.suqi8.coui.kmp.icon.extended.Community
import io.github.suqi8.coui.kmp.icon.extended.Contacts
import io.github.suqi8.coui.kmp.icon.extended.ContactsCircle
import io.github.suqi8.coui.kmp.icon.extended.Favorites
import io.github.suqi8.coui.kmp.icon.extended.Folder
import io.github.suqi8.coui.kmp.icon.extended.GridView
import io.github.suqi8.coui.kmp.icon.extended.Home
import io.github.suqi8.coui.kmp.icon.extended.Music
import io.github.suqi8.coui.kmp.icon.extended.Notes
import io.github.suqi8.coui.kmp.icon.extended.Play
import io.github.suqi8.coui.kmp.icon.extended.Recent
import io.github.suqi8.coui.kmp.icon.extended.Recording
import io.github.suqi8.coui.kmp.icon.extended.Settings
import io.github.suqi8.coui.kmp.icon.extended.Stopwatch
import io.github.suqi8.coui.kmp.icon.extended.Store
import io.github.suqi8.coui.kmp.icon.extended.Theme
import io.github.suqi8.coui.kmp.icon.extended.TopDownloads

private enum class HomeNavigationIconRole {
    HOME,
    DYNAMIC,
    STORY,
    HISTORY,
    LISTEN_VIDEO,
    PROFILE,
    FAVORITE,
    LIVE,
    WATCH_LATER,
    SETTINGS,
    PLUGINS,
    FOLLOW,
    POPULAR,
    ANIME,
    GAME,
    PARTITION,
    KNOWLEDGE,
    TECH,
    SUBSCRIPTIONS,
}

internal enum class HomeNavigationIconSource {
    MIUIX,
}

private fun resolveHomeNavigationIconRole(tabId: String): HomeNavigationIconRole = when (tabId.trim().uppercase()) {
    "HOME", "RECOMMEND" -> HomeNavigationIconRole.HOME
    "DYNAMIC" -> HomeNavigationIconRole.DYNAMIC
    "STORY" -> HomeNavigationIconRole.STORY
    "HISTORY" -> HomeNavigationIconRole.HISTORY
    "LISTEN_VIDEO" -> HomeNavigationIconRole.LISTEN_VIDEO
    "PROFILE" -> HomeNavigationIconRole.PROFILE
    "FAVORITE" -> HomeNavigationIconRole.FAVORITE
    "LIVE" -> HomeNavigationIconRole.LIVE
    "WATCHLATER", "WATCH_LATER" -> HomeNavigationIconRole.WATCH_LATER
    "SETTINGS" -> HomeNavigationIconRole.SETTINGS
    "PLUGINS" -> HomeNavigationIconRole.PLUGINS
    "FOLLOW" -> HomeNavigationIconRole.FOLLOW
    "POPULAR" -> HomeNavigationIconRole.POPULAR
    "ANIME" -> HomeNavigationIconRole.ANIME
    "GAME" -> HomeNavigationIconRole.GAME
    "PARTITION" -> HomeNavigationIconRole.PARTITION
    "KNOWLEDGE" -> HomeNavigationIconRole.KNOWLEDGE
    "TECH" -> HomeNavigationIconRole.TECH
    "SUBSCRIPTIONS" -> HomeNavigationIconRole.SUBSCRIPTIONS
    else -> HomeNavigationIconRole.HOME
}

internal fun resolveMiuixPreferredHomeNavigationIconSource(
    tabId: String,
): HomeNavigationIconSource {
    resolveHomeNavigationIconRole(tabId)
    return HomeNavigationIconSource.MIUIX
}

/**
 * 首页底栏、侧栏和顶部分区的唯一图标入口。
 */
@Composable
internal fun resolveMiuixPreferredHomeNavigationIcon(
    tabId: String,
    selected: Boolean = false,
): ImageVector {
    val role = resolveHomeNavigationIconRole(tabId)
    return resolveMiuixHomeNavigationIcon(role, selected)
}

/** Top tabs and bottom navigation share the same selected glyph for the same role. */
@Composable
private fun resolveMiuixNavigationFilledIcon(role: HomeNavigationIconRole): ImageVector? = when (role) {
    HomeNavigationIconRole.HOME -> COUIIcons.Medium.Home
    HomeNavigationIconRole.DYNAMIC -> COUIIcons.Medium.Community
    HomeNavigationIconRole.STORY, HomeNavigationIconRole.ANIME -> COUIIcons.Medium.Play
    HomeNavigationIconRole.HISTORY -> COUIIcons.Medium.Recent
    HomeNavigationIconRole.LISTEN_VIDEO -> COUIIcons.Medium.Music
    HomeNavigationIconRole.PROFILE -> COUIIcons.Medium.ContactsCircle
    HomeNavigationIconRole.FAVORITE -> COUIIcons.Medium.Favorites
    HomeNavigationIconRole.LIVE -> COUIIcons.Medium.Recording
    HomeNavigationIconRole.WATCH_LATER -> COUIIcons.Medium.Stopwatch
    HomeNavigationIconRole.SETTINGS -> null
    HomeNavigationIconRole.PLUGINS -> COUIIcons.Medium.Folder
    HomeNavigationIconRole.FOLLOW -> COUIIcons.Medium.Contacts
    HomeNavigationIconRole.POPULAR -> COUIIcons.Medium.TopDownloads
    HomeNavigationIconRole.GAME -> COUIIcons.Medium.Store
    HomeNavigationIconRole.PARTITION -> COUIIcons.Medium.GridView
    HomeNavigationIconRole.KNOWLEDGE -> COUIIcons.Medium.Notes
    HomeNavigationIconRole.TECH -> COUIIcons.Medium.Theme
    HomeNavigationIconRole.SUBSCRIPTIONS -> ImageVector.vectorResource(R.drawable.ms_rss_feed_24)
}

@Composable
internal fun resolveMiuixBottomNavigationIcon(
    item: BottomNavItem,
    selected: Boolean,
): ImageVector = resolveMiuixPreferredHomeNavigationIcon(item.name, selected = selected)

@Composable
private fun resolveMiuixHomeNavigationIcon(
    role: HomeNavigationIconRole,
    selected: Boolean,
): ImageVector {
    if (selected) {
        resolveMiuixNavigationFilledIcon(role)?.let { return it }
    }
    return when (role) {
        HomeNavigationIconRole.HOME -> COUIIcons.Light.Home
        HomeNavigationIconRole.DYNAMIC -> COUIIcons.Light.Community
        HomeNavigationIconRole.STORY -> COUIIcons.Light.Play
        HomeNavigationIconRole.HISTORY -> COUIIcons.Light.Recent
        HomeNavigationIconRole.LISTEN_VIDEO -> COUIIcons.Light.Music
        HomeNavigationIconRole.PROFILE -> COUIIcons.Light.ContactsCircle
        HomeNavigationIconRole.FAVORITE -> COUIIcons.Light.Favorites
        HomeNavigationIconRole.LIVE -> COUIIcons.Light.Recording
        HomeNavigationIconRole.WATCH_LATER -> COUIIcons.Light.Stopwatch
        HomeNavigationIconRole.SETTINGS -> if (selected) COUIIcons.Demibold.Settings else COUIIcons.Light.Settings
        HomeNavigationIconRole.PLUGINS -> COUIIcons.Light.Folder
        HomeNavigationIconRole.FOLLOW -> COUIIcons.Light.Contacts
        HomeNavigationIconRole.POPULAR -> COUIIcons.Light.TopDownloads
        HomeNavigationIconRole.ANIME -> COUIIcons.Light.Play
        HomeNavigationIconRole.GAME -> COUIIcons.Light.Store
        HomeNavigationIconRole.PARTITION -> COUIIcons.Light.GridView
        HomeNavigationIconRole.KNOWLEDGE -> COUIIcons.Light.Notes
        HomeNavigationIconRole.TECH -> COUIIcons.Light.Theme
        HomeNavigationIconRole.SUBSCRIPTIONS -> ImageVector.vectorResource(R.drawable.ms_rss_feed_24)
    }
}
