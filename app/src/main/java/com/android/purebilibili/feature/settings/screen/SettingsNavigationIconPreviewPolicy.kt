package com.android.purebilibili.feature.settings

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.vectorResource
import com.android.purebilibili.R
import com.android.purebilibili.core.ui.AppSemanticIconFamily
import io.github.suqi8.coui.kmp.icon.COUIIcons
import io.github.suqi8.coui.kmp.icon.extended.*

private enum class SettingsNavigationIconRole {
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

internal fun resolveSettingsNavigationPreviewMaterialSymbolResource(
    tabId: String,
    selected: Boolean = false,
): Int = when (tabId.trim().uppercase()) {
    "HOME", "RECOMMEND" -> if (selected) R.drawable.ms_home_fill_24 else R.drawable.ms_home_24
    "DYNAMIC" -> if (selected) R.drawable.ms_notifications_fill_24 else R.drawable.ms_notifications_none_24
    "STORY" -> if (selected) R.drawable.ms_play_circle_fill_24 else R.drawable.ms_play_circle_outline_24
    "HISTORY" -> if (selected) R.drawable.ms_history_fill_24 else R.drawable.ms_history_24
    "LISTEN_VIDEO" -> if (selected) R.drawable.ms_library_music_fill_24 else R.drawable.ms_library_music_24
    "PROFILE", "FOLLOW" -> if (selected) R.drawable.ms_person_fill_24 else R.drawable.ms_person_24
    "FAVORITE" -> if (selected) R.drawable.ms_collections_bookmark_fill_24 else R.drawable.ms_collections_bookmark_24
    "LIVE" -> if (selected) R.drawable.ms_live_tv_fill_24 else R.drawable.ms_live_tv_24
    "WATCHLATER", "WATCH_LATER" -> if (selected) R.drawable.ms_watch_later_fill_24 else R.drawable.ms_watch_later_24
    "SETTINGS" -> if (selected) R.drawable.ms_settings_fill_24 else R.drawable.ms_settings_24
    "PLUGINS" -> if (selected) R.drawable.ms_extension_fill_24 else R.drawable.ms_extension_24
    "POPULAR" -> if (selected) R.drawable.ms_trending_up_fill_24 else R.drawable.ms_trending_up_24
    "ANIME" -> if (selected) R.drawable.ms_collections_bookmark_fill_24 else R.drawable.ms_collections_bookmark_24
    "GAME" -> if (selected) R.drawable.ms_sports_esports_fill_24 else R.drawable.ms_sports_esports_24
    "PARTITION" -> if (selected) R.drawable.ms_grid_view_fill_24 else R.drawable.ms_grid_view_24
    "KNOWLEDGE" -> if (selected) R.drawable.ms_lightbulb_fill_24 else R.drawable.ms_lightbulb_24
    "TECH" -> if (selected) R.drawable.ms_smart_toy_fill_24 else R.drawable.ms_smart_toy_24
    "SUBSCRIPTIONS" -> R.drawable.ms_rss_feed_24
    else -> if (selected) R.drawable.ms_home_fill_24 else R.drawable.ms_home_24
}

/** Uses the active native icon family so the navigation preview matches the runtime chrome. */
@Composable
internal fun resolveSettingsNavigationPreviewIcon(
    tabId: String,
    iconFamily: AppSemanticIconFamily,
    selected: Boolean = false,
): ImageVector {
    val role = when (tabId.trim().uppercase()) {
        "HOME", "RECOMMEND" -> SettingsNavigationIconRole.HOME
        "DYNAMIC" -> SettingsNavigationIconRole.DYNAMIC
        "STORY" -> SettingsNavigationIconRole.STORY
        "HISTORY" -> SettingsNavigationIconRole.HISTORY
        "LISTEN_VIDEO" -> SettingsNavigationIconRole.LISTEN_VIDEO
        "PROFILE" -> SettingsNavigationIconRole.PROFILE
        "FAVORITE" -> SettingsNavigationIconRole.FAVORITE
        "LIVE" -> SettingsNavigationIconRole.LIVE
        "WATCHLATER", "WATCH_LATER" -> SettingsNavigationIconRole.WATCH_LATER
        "SETTINGS" -> SettingsNavigationIconRole.SETTINGS
        "PLUGINS" -> SettingsNavigationIconRole.PLUGINS
        "FOLLOW" -> SettingsNavigationIconRole.FOLLOW
        "POPULAR" -> SettingsNavigationIconRole.POPULAR
        "ANIME" -> SettingsNavigationIconRole.ANIME
        "GAME" -> SettingsNavigationIconRole.GAME
        "PARTITION" -> SettingsNavigationIconRole.PARTITION
        "KNOWLEDGE" -> SettingsNavigationIconRole.KNOWLEDGE
        "TECH" -> SettingsNavigationIconRole.TECH
        "SUBSCRIPTIONS" -> SettingsNavigationIconRole.SUBSCRIPTIONS
        else -> SettingsNavigationIconRole.HOME
    }

    return when (iconFamily) {
        AppSemanticIconFamily.MIUIX -> when (role) {
            SettingsNavigationIconRole.HOME -> if (selected) COUIIcons.Medium.Home else COUIIcons.Home
            SettingsNavigationIconRole.DYNAMIC -> if (selected) COUIIcons.Medium.Messages else COUIIcons.Messages
            SettingsNavigationIconRole.STORY -> if (selected) COUIIcons.Medium.Recording else COUIIcons.Recording
            SettingsNavigationIconRole.HISTORY -> if (selected) COUIIcons.Medium.Recent else COUIIcons.Recent
            SettingsNavigationIconRole.LISTEN_VIDEO -> if (selected) COUIIcons.Medium.Music else COUIIcons.Music
            SettingsNavigationIconRole.PROFILE -> COUIIcons.ContactsCircle
            SettingsNavigationIconRole.FAVORITE -> if (selected) COUIIcons.FavoritesFill else COUIIcons.Favorites
            SettingsNavigationIconRole.LIVE -> if (selected) COUIIcons.Medium.RecordingTape else COUIIcons.RecordingTape
            SettingsNavigationIconRole.WATCH_LATER -> if (selected) COUIIcons.Medium.WorldClock else COUIIcons.WorldClock
            SettingsNavigationIconRole.SETTINGS -> if (selected) COUIIcons.Medium.Settings else COUIIcons.Settings
            SettingsNavigationIconRole.PLUGINS -> if (selected) COUIIcons.FolderFill else COUIIcons.Folder
            SettingsNavigationIconRole.FOLLOW -> COUIIcons.Contacts
            SettingsNavigationIconRole.POPULAR -> COUIIcons.TopDownloads
            SettingsNavigationIconRole.ANIME -> if (selected) COUIIcons.FavoritesFill else COUIIcons.Favorites
            SettingsNavigationIconRole.GAME -> COUIIcons.Store
            SettingsNavigationIconRole.PARTITION -> if (selected) COUIIcons.Medium.GridView else COUIIcons.GridView
            SettingsNavigationIconRole.KNOWLEDGE -> COUIIcons.Notes
            SettingsNavigationIconRole.TECH -> COUIIcons.MindMap
            SettingsNavigationIconRole.SUBSCRIPTIONS -> ImageVector.vectorResource(R.drawable.ms_rss_feed_24)
        }
        AppSemanticIconFamily.MATERIAL -> ImageVector.vectorResource(
            resolveSettingsNavigationPreviewMaterialSymbolResource(tabId, selected)
        )
    }
}
