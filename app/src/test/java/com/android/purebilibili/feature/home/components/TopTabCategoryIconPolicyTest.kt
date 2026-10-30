package com.android.purebilibili.feature.home.components

import com.android.purebilibili.core.ui.AppIcons
import kotlin.test.Test
import kotlin.test.assertEquals

class TopTabCategoryIconPolicyTest {

    @Test
    fun `top category tabs use COUI icons for every supported category`() {
        assertEquals(AppIcons.Rounded.Home, resolveTopTabIcon("RECOMMEND"))
        assertEquals(AppIcons.Rounded.People, resolveTopTabIcon("FOLLOW"))
        assertEquals(AppIcons.Rounded.Whatshot, resolveTopTabIcon("POPULAR"))
        assertEquals(AppIcons.Rounded.CollectionsBookmark, resolveTopTabIcon("ANIME"))
        assertEquals(AppIcons.Rounded.LiveTv, resolveTopTabIcon("LIVE"))
        assertEquals(AppIcons.Rounded.SportsEsports, resolveTopTabIcon("GAME"))
        assertEquals(AppIcons.Rounded.School, resolveTopTabIcon("KNOWLEDGE"))
        assertEquals(AppIcons.Rounded.Memory, resolveTopTabIcon("TECH"))
    }

    @Test
    fun `partition and unknown categories use the shared grid fallback`() {
        assertEquals(AppIcons.Rounded.GridView, resolveTopTabIcon("PARTITION"))
        assertEquals(AppIcons.Rounded.GridView, resolveTopTabIcon("未知栏目"))
    }
}
