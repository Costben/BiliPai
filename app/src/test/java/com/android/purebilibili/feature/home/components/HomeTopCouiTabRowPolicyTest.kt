package com.android.purebilibili.feature.home.components

import com.android.purebilibili.core.theme.AppUiStyle
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeTopCouiTabRowPolicyTest {

    private fun shouldUse(
        uiStyle: AppUiStyle = AppUiStyle.COUI17,
        liquidGlassEnabled: Boolean = false,
        skinPlainStyle: Boolean = false,
        hasSkinStickerIcons: Boolean = false,
        showIcon: Boolean = false,
        showText: Boolean = true,
        forceMaterialUnderline: Boolean = false,
    ): Boolean = shouldUseCouiHomeTopTabRow(
        uiStyle = uiStyle,
        liquidGlassEnabled = liquidGlassEnabled,
        skinPlainStyle = skinPlainStyle,
        hasSkinStickerIcons = hasSkinStickerIcons,
        showIcon = showIcon,
        showText = showText,
        forceMaterialUnderline = forceMaterialUnderline,
    )

    @Test
    fun `coui text-only top tabs use the native segment row`() {
        assertTrue(shouldUse())
    }

    @Test
    fun `other skins keep their own renderers`() {
        assertFalse(shouldUse(uiStyle = AppUiStyle.MIUIX))
        assertFalse(shouldUse(uiStyle = AppUiStyle.MATERIAL3))
    }

    @Test
    fun `liquid glass keeps the custom indicator pipeline`() {
        assertFalse(shouldUse(liquidGlassEnabled = true))
    }

    @Test
    fun `skin decorations keep the custom renderer`() {
        assertFalse(shouldUse(skinPlainStyle = true))
        assertFalse(shouldUse(hasSkinStickerIcons = true))
    }

    @Test
    fun `icon label modes fall back to the icon renderer`() {
        assertFalse(shouldUse(showIcon = true, showText = true))
        assertFalse(shouldUse(showIcon = true, showText = false))
        assertFalse(shouldUse(showIcon = false, showText = false))
    }

    @Test
    fun `forced material underline stays material`() {
        assertFalse(shouldUse(forceMaterialUnderline = true))
    }
}
