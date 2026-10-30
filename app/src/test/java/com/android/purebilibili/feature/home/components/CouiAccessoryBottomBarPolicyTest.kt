package com.android.purebilibili.feature.home.components

import io.github.suqi8.coui.kmp.basic.BottomFloatingRedDot
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CouiAccessoryBottomBarPolicyTest {

    @Test
    fun `bottom bar keeps every item in one continuous capsule`() {
        val source = sourceText("CouiAccessoryBottomBar.kt")

        assertTrue(source.contains("BottomFloatingNavigationBar("))
        assertFalse(source.contains("BottomFloatingAccessoryNavigationBar"))
        assertFalse(source.contains("CouiBottomBarAccessory"))
        assertFalse(source.contains("resolveCouiAccessoryBottomBarSlots"))
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

    private fun sourceText(fileName: String): String = listOf(
        File("app/src/main/java/com/android/purebilibili/feature/home/components/$fileName"),
        File("src/main/java/com/android/purebilibili/feature/home/components/$fileName"),
    ).first { it.exists() }.readText()
}
