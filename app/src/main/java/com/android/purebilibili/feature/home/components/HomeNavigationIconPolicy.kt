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
private fun resolveMiuixNavigationFilledResource(role: HomeNavigationIconRole): Int? = when (role) {
    HomeNavigationIconRole.HOME -> R.drawable.ms_home_fill_24
    HomeNavigationIconRole.DYNAMIC -> R.drawable.bp_nav_community_filled_24
    HomeNavigationIconRole.STORY, HomeNavigationIconRole.ANIME -> R.drawable.ic_home_nav_story_filled
    HomeNavigationIconRole.HISTORY -> R.drawable.ms_history_fill_24
    HomeNavigationIconRole.LISTEN_VIDEO -> R.drawable.bp_nav_music_filled_24
    HomeNavigationIconRole.PROFILE -> R.drawable.ic_home_nav_profile_filled
    HomeNavigationIconRole.FAVORITE -> R.drawable.bp_nav_favorite_filled_24
    HomeNavigationIconRole.LIVE -> R.drawable.ic_home_nav_live_filled
    HomeNavigationIconRole.WATCH_LATER -> R.drawable.ms_watch_later_fill_24
    HomeNavigationIconRole.SETTINGS -> null
    HomeNavigationIconRole.PLUGINS -> R.drawable.bp_nav_folder_filled_24
    HomeNavigationIconRole.FOLLOW -> R.drawable.ms_person_fill_24
    HomeNavigationIconRole.POPULAR -> R.drawable.ms_trending_up_fill_24
    HomeNavigationIconRole.GAME -> R.drawable.bp_nav_store_filled_24
    HomeNavigationIconRole.PARTITION -> R.drawable.ms_grid_view_fill_24
    HomeNavigationIconRole.KNOWLEDGE -> R.drawable.bp_nav_notes_filled_24
    HomeNavigationIconRole.TECH -> R.drawable.bp_nav_palette_filled_24
    HomeNavigationIconRole.SUBSCRIPTIONS -> R.drawable.ms_rss_feed_24
}

@Composable
internal fun resolveMiuixBottomNavigationIcon(
    item: BottomNavItem,
    selected: Boolean,
): ImageVector = if (item == BottomNavItem.LIVE && !selected) {
    ImageVector.vectorResource(R.drawable.ic_home_nav_live)
} else {
    resolveMiuixPreferredHomeNavigationIcon(item.name, selected = selected)
}

@Composable
private fun resolveMiuixHomeNavigationIcon(
    role: HomeNavigationIconRole,
    selected: Boolean,
): ImageVector {
    if (selected) {
        val filledResource = resolveMiuixNavigationFilledResource(role)
        if (filledResource != null) return ImageVector.vectorResource(filledResource)
    }
    return when (role) {
        HomeNavigationIconRole.HOME -> ImageVector.vectorResource(R.drawable.bp_nav_home_outline_24)
        HomeNavigationIconRole.DYNAMIC -> COUIIcons.Light.Community
        HomeNavigationIconRole.STORY -> COUIIcons.Light.Play
        HomeNavigationIconRole.HISTORY -> ImageVector.vectorResource(R.drawable.bp_nav_history_outline_24)
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
