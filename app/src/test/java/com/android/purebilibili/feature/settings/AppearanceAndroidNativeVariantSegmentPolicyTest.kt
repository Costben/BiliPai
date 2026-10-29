package com.android.purebilibili.feature.settings

import com.android.purebilibili.core.theme.AppUiStyle
import kotlin.test.Test
import kotlin.test.assertEquals

class AppearanceAndroidNativeVariantSegmentPolicyTest {

    @Test
    fun uiStyleSegmentOptions_keepAndroidStylesInStableOrder() {
        val options = resolveThemeSelectionOptions(
            material3Label = "Material 3",
            couiLabel = "COUI 17",
        )

        assertEquals(
            listOf(AppUiStyle.MATERIAL3, AppUiStyle.COUI17),
            options.map { it.value }
        )
        assertEquals(
            listOf("Material 3", "COUI 17"),
            options.map { it.label }
        )
    }
}
