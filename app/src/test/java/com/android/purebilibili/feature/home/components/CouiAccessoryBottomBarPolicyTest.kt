package com.android.purebilibili.feature.home.components

import io.github.suqi8.coui.kmp.basic.BottomFloatingRedDot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CouiAccessoryBottomBarPolicyTest {

    @Test
    fun fiveItems_mapToStartBallThreeCapsuleItemsAndEndBall() {
        val slots = resolveCouiAccessoryBottomBarSlots(
            listOf(
                BottomNavItem.HOME,
                BottomNavItem.DYNAMIC,
                BottomNavItem.HISTORY,
                BottomNavItem.LISTEN_VIDEO,
                BottomNavItem.PROFILE,
            )
        )

        assertEquals(BottomNavItem.HOME, slots.start)
        assertEquals(
            listOf(BottomNavItem.DYNAMIC, BottomNavItem.HISTORY, BottomNavItem.LISTEN_VIDEO),
            slots.capsule,
        )
        assertEquals(BottomNavItem.PROFILE, slots.end)
    }

    @Test
    fun customOrder_followsLeftToRightPositions() {
        val slots = resolveCouiAccessoryBottomBarSlots(
            listOf(BottomNavItem.PROFILE, BottomNavItem.HOME, BottomNavItem.DYNAMIC)
        )

        assertEquals(BottomNavItem.PROFILE, slots.start)
        assertEquals(listOf(BottomNavItem.HOME), slots.capsule)
        assertEquals(BottomNavItem.DYNAMIC, slots.end)
    }

    @Test
    fun fewerThanThreeItems_stayInOneCapsule() {
        val items = listOf(BottomNavItem.HOME, BottomNavItem.PROFILE)
        val slots = resolveCouiAccessoryBottomBarSlots(items)

        assertNull(slots.start)
        assertEquals(items, slots.capsule)
        assertNull(slots.end)
    }

    @Test
    fun dynamicReminder_mapsCountAndDotModes() {
        assertEquals(BottomFloatingRedDot.Count(12), resolveCouiBottomBarRedDot(BottomNavItem.DYNAMIC, 12))
        assertEquals(
            BottomFloatingRedDot.Dot,
            resolveCouiBottomBarRedDot(BottomNavItem.DYNAMIC, BOTTOM_BAR_DYNAMIC_BADGE_DOT),
        )
        assertNull(resolveCouiBottomBarRedDot(BottomNavItem.DYNAMIC, 0))
        assertNull(resolveCouiBottomBarRedDot(BottomNavItem.HOME, 12))
    }
}
